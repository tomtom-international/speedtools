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

import com.tomtom.speedtools.time.UTCTime;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;

/**
 * Convenience class that wraps multiple calculators with different intervals.
 */
public class MultiMetricsCollector implements MultiMetricsData {
    private final MetricsCollector[] calculators = new MetricsCollector[Period.values().length];

    @Nonnull
    public static MultiMetricsCollector all() {
        return new MultiMetricsCollector(Period.values());
    }

    @Nonnull
    public static MultiMetricsCollector create(@Nonnull final Period... intervals) {
        assert intervals != null;
        return new MultiMetricsCollector(intervals);
    }

    private MultiMetricsCollector(@Nonnull final Period... intervals) {
        assert intervals != null;
        for (final Period interval : intervals) {
            calculators[interval.ordinal()] = MetricsCollector.create(interval);
        }
    }

    @Nonnull
    public MetricsCollector get(@Nonnull final Period interval) {
        assert interval != null;
        return calculators[interval.ordinal()];
    }

    /**
     * Adds a value to the managed metric collectors.
     *
     * @param value Value to add.
     */
    public void addValue(final float value) {
        addValueNow(value, UTCTime.now());
    }

    /**
     * Adds a value to the managed metric collectors. Use this method instead of {@link #addValue(float)} when now is
     * determined by the caller and should be aligned with other now-related functionality. Note that now should denote
     * the current time and cannot be an arbitrary time in the past or the future.
     *
     * @param value Value to add.
     * @param now   Current time, as determined by the caller.
     */
    public void addValueNow(final float value, @Nonnull final DateTime now) {
        for (final Period interval : Period.values()) {
            final MetricsCollector calculator = calculators[interval.ordinal()];
            if (calculator != null) {
                calculator.addValue(value, now);
            }
        }
    }

    /**
     * Return the total number of metric data items added to the collector with the given interval up until now since
     * the duration (approx) of this metric.
     *
     * @param interval Interval.
     * @return Total.
     */
    public int getCount(@Nonnull final Period interval) {
        assert interval != null;
        final MetricsCollector calculator = calculators[interval.ordinal()];
        if (calculator != null) {
            return calculator.getCount();
        }
        return 0;
    }

    /**
     * Return the sum of all metric data added to the collector with the given interval up until now since the duration
     * (approx) of this metric.
     *
     * @param interval Interval.
     * @return Sum of metric data.
     */
    public float getSum(@Nonnull final Period interval) {
        assert interval != null;
        final MetricsCollector calculator = calculators[interval.ordinal()];
        if (calculator != null) {
            return calculator.getSum();
        }
        return 0;
    }

    /**
     * Return the average of all metric data added to the collector with the given interval up until now since the
     * duration (approx) of this metric.
     *
     * @param interval Interval.
     * @return Average of metric data.
     */
    public float getAvg(@Nonnull final Period interval) {
        assert interval != null;
        final MetricsCollector calculator = calculators[interval.ordinal()];
        if (calculator != null) {
            return calculator.getAvg();
        }
        return 0;
    }

    @Override
    @Nonnull
    public MetricsData getMetricsData(final Period period) {
        return calculators[period.ordinal()];
    }

    @Nonnull
    @Override
    public MetricsData getLastMonth() {
        return calculators[Period.LAST_MONTH.ordinal()];
    }

    @Nonnull
    @Override
    public MetricsData getLastWeek() {
        return calculators[Period.LAST_WEEK.ordinal()];
    }

    @Nonnull
    @Override
    public MetricsData getLastDay() {
        return calculators[Period.LAST_DAY.ordinal()];
    }

    @Nonnull
    @Override
    public MetricsData getLastHour() {
        return calculators[Period.LAST_HOUR.ordinal()];
    }

    @Nonnull
    @Override
    public MetricsData getLastMinute() {
        return calculators[Period.LAST_MINUTE.ordinal()];
    }
}

