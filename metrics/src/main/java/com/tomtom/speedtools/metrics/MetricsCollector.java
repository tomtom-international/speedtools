/*
 * Copyright (C) 2015. TomTom International BV (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomtom.speedtools.metrics;

import com.tomtom.speedtools.metrics.MultiMetricsData.Period;
import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility class to collect metrics data. It keeps track of a sum, count and average for the given time period. Not all
 * values are maintained. Instead, the time period is divided in a number of entries, which constrains memory usage. The
 * trade-off for this reduced memory usage is that statistics are not reported for EXACTLY given time period, but
 * possibly for a slightly longer period.
 *
 * This class is thread-safe.
 */
@SuppressWarnings("SynchronizedMethod")
public class MetricsCollector implements MetricsData {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsCollector.class);

    @Nonnull
    private final Duration totalMetricDuration;
    @Nonnull
    private final Duration timeSlotDuration;
    @Nonnull
    private final ArrayDeque<MetricsTimeSlot> values;
    private float sum;
    private int count;
    private float sumSquares;

    @Nonnull
    public static MetricsCollector create(@Nonnull final Period interval) {
        assert interval != null;
        switch (interval) {

            case LAST_MONTH:
                return new MetricsCollector(Duration.standardDays(30), 30);

            case LAST_WEEK:
                return new MetricsCollector(Duration.standardDays(7), 24 * 7);

            case LAST_DAY:
                return new MetricsCollector(Duration.standardDays(1), 48);

            case LAST_HOUR:
                return new MetricsCollector(Duration.standardHours(1), 60);

            case LAST_MINUTE:
                return new MetricsCollector(Duration.standardMinutes(1), 30);
        }
        assert false;
        throw new IllegalStateException();
    }

    public MetricsCollector(@Nonnull final Duration totalMetricDuration, final int maxEntries) {
        assert totalMetricDuration != null;
        assert totalMetricDuration.isLongerThan(Duration.millis(1));
        assert maxEntries > 0;
        this.totalMetricDuration = totalMetricDuration;
        this.timeSlotDuration = Duration.millis(totalMetricDuration.getMillis() / maxEntries);
        this.values = new ArrayDeque<>();
    }

    /**
     * Retrieve the slots by which the metric is represented internally. Mainly used for serialization.
     *
     * @return Metric slots.
     */
    @Nonnull
    public synchronized List<MetricsTimeSlot> getSlots() {
        return new ArrayList<>(this.values);
    }

    /**
     * Set the slots by which the metric is represented internally. Existing data will be overwritten. Mainly used for
     * serialization.
     *
     * @param slots Metric slots.
     */
    public synchronized void setSlots(@Nonnull final Collection<MetricsTimeSlot> slots) {
        assert slots != null;
        this.values.clear();
        this.sum = 0.0f;
        this.count = 0;
        this.sumSquares = 0.0f;
        for (final MetricsTimeSlot slot : slots) {
            this.values.add(slot);
            this.sum += slot.sum;
            this.count += slot.count;
            this.sumSquares += slot.sumSquares;
        }
    }

    @Nonnull
    public Duration getTotalMetricDuration() {
        return totalMetricDuration;
    }

    /**
     * Adds a value to the collector.
     *
     * @param value Value to add.
     */
    public synchronized void addValueNow(final float value) {
        addValue(value, UTCTime.now());
    }

    /**
     * Adds a value to the collector. Use this method instead of {@link #addValueNow(float)} when now is determined by the
     * caller and should be aligned with other now-related functionality. Note that now should denote the current time
     * and cannot be an arbitrary time in the past or the future.
     *
     * @param value Value to add.
     * @param now   Current time, as determined by the caller.
     */
    public synchronized void addValue(final float value, @Nonnull final DateTime now) {

        assert now != null;

        // Prune old slots.
        prune(now);

        // Add the initial slot?
        if (values.isEmpty()) {
            values.add(new MetricsTimeSlot(now, value, value * value, value, value, 1));
        } else {
            // Add to new or existing slot.
            DateTime slotTime = values.getLast().startTime;
            DateTime slotEndTime = slotTime.plus(timeSlotDuration);

            // Skip values that are too old for last slot.
            if (now.isBefore(slotTime)) {
                //noinspection AccessToStaticFieldLockedOnInstance
                LOG.debug("Dropped metric data: {} at {}", value, now);
                return;
            }

            // Append new time slots until now is reached.
            while (slotEndTime.isBefore(now)) {
                values.add(new MetricsTimeSlot(slotEndTime, 0, 0, 0, 0, 0));
                slotTime = slotEndTime;
                slotEndTime = slotTime.plus(timeSlotDuration);
            }

            final MetricsTimeSlot slot = values.removeLast();
            values.addLast(slot.addValue(value));
        }
        this.sum += value;
        this.sumSquares += value * value;
        this.count += 1;
    }

    /**
     * Return the total number of metric data items added up until now since the duration (approx) of this metric.
     *
     * @return Total number of data items.
     */
    @Override
    public synchronized int getCount() {
        prune(UTCTime.now());
        return count;
    }

    /**
     * Return the sum of all metric data added up until now since the duration (approx) of this metric.
     *
     * @return Sum of metric data.
     */
    @Override
    public synchronized float getSum() {
        prune(UTCTime.now());
        return sum;
    }

    /**
     * Return the average of all metric data added up until now since the duration (approx) of this metric.
     *
     * @return Average of metric data, or {@link Float#NaN} if no data has been added.
     */
    @Override
    public synchronized float getAvg() {
        prune(UTCTime.now());
        if (count == 0) {
            return Float.NaN;
        }
        return sum / (float) count;
    }

    /**
     * Get standard deviation.
     *
     * @return The the sample standard deviation of all metric data added up until now since the duration (approx) of
     * this metric, or {@link Float#NaN} if no data has been added.
     */
    @Override
    public synchronized float getStdDev() {
        prune(UTCTime.now());
        if (count == 0) {
            return Float.NaN;
        }
        if (count == 1) {
            return 0;
        }
        assert count >= 2;
        final double dividend = (count * sumSquares) - (sum * sum);
        final int divider = count * (count - 1);
        assert divider != 0;
        final double quotient = dividend / divider;
        if (quotient < 0) {
            return 0;
        }
        //noinspection NumericCastThatLosesPrecision
        return (float) Math.sqrt(quotient);
    }

    /**
     * Get maximum.
     *
     * @return The maximum value of all metric data added up until now since the duration (approx) of this metric, or
     * {@link Float#NaN} if no data has been added.
     */
    @Override
    public synchronized float getMax() {
        prune(UTCTime.now());
        float max = Float.NaN;
        for (final MetricsTimeSlot value : values) {
            if (Float.isNaN(max)) {
                max = value.getMax();
            } else {
                max = Math.max(max, value.getMax());
            }
        }
        return max;
    }

    /**
     * Get minimum.
     *
     * @return The minimum value of all metric data added up until now since the duration (approx) of this metric, or
     * {@link Float#NaN} if no data has been added.
     */
    @Override
    public synchronized float getMin() {
        prune(UTCTime.now());
        float min = Float.NaN;
        for (final MetricsTimeSlot value : values) {
            if (Float.isNaN(min)) {
                min = value.getMin();
            } else {
                min = Math.min(min, value.getMin());
            }
        }
        return min;
    }

    private void prune(@Nonnull final DateTime now) {
        assert now != null;
        final DateTime earliest = now.minus(totalMetricDuration);
        while (!values.isEmpty() && values.getFirst().startTime.isBefore(earliest)) {
            final MetricsTimeSlot slot = values.removeFirst();
            this.sum -= slot.sum;
            this.count -= slot.count;
            this.sumSquares -= slot.sumSquares;
        }
    }
}
