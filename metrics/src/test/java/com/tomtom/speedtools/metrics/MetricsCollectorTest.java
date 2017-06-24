/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class MetricsCollectorTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsCollectorTest.class);

    private static final long METRIC_DURATION_MSECS = 200;
    private static final int METRIC_NR_ENTRIES = 10;

    @Test
    public void testWithNoValuesAdded() {
        LOG.info("testWithNoValuesAdded");
        final MetricsCollector metricsCollector = new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS),
                METRIC_NR_ENTRIES);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 0, Float.NaN, Float.NaN, 0.0f, Float.NaN, Float.NaN);
    }

    @Test
    public void testWithOneValue() {
        LOG.info("testWithOneValue");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        final float value = 8.0f;
        metricsCollector.addValueNow(value);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 1, value, value, value, value, 0.0f);
    }

    @Test
    public void testPositiveAndNegativeValue() {
        LOG.info("testPositiveAndNegativeValue");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        final float positive = 8.0f;
        final float negative = -4.0f;
        metricsCollector.addValueNow(positive);
        metricsCollector.addValueNow(negative);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 2, negative, positive, 4.0f, 2.0f, 8.485281374f);
    }

    @Test
    public void testNegativeValues1() {
        LOG.info("testNegativeValues1");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        metricsCollector.addValueNow(-26.0f);
        metricsCollector.addValueNow(-27.0f);
        metricsCollector.addValueNow(-28.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 3, -28.0f, -26.0f, -81.0f, -27.0f, 1.0f);
    }

    @Test
    public void testNegativeValues2() {
        LOG.info("testNegativeValues2");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        metricsCollector.addValueNow(-28.0f);
        metricsCollector.addValueNow(-27.0f);
        metricsCollector.addValueNow(-26.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 3, -28.0f, -26.0f, -81.0f, -27.0f, 1.0f);
    }

    @Test
    public void testPositiveValues1() {
        LOG.info("testPositiveValues1");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        metricsCollector.addValueNow(26.0f);
        metricsCollector.addValueNow(27.0f);
        metricsCollector.addValueNow(28.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 3, 26.0f, 28.0f, 81.0f, 27.0f, 1.0f);
    }

    @Test
    public void testPositiveValues2() {
        LOG.info("testPositiveValues2");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        metricsCollector.addValueNow(28.0f);
        metricsCollector.addValueNow(27.0f);
        metricsCollector.addValueNow(26.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 3, 26.0f, 28.0f, 81.0f, 27.0f, 1.0f);
    }

    @Test
    public void testStandardDeviation() {
        LOG.info("testStandardDeviation");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 0, Float.NaN, Float.NaN, 0.0f, Float.NaN, Float.NaN);

        metricsCollector.addValueNow(2);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 1, 2, 2, 2, 2, 0.0f);

        metricsCollector.addValueNow(4);
        metricsCollector.addValueNow(6);
        metricsCollector.addValueNow(7);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 4, 2.0f, 7.0f, 19.0f, 4.75f, 2.217355783f);
    }

    @Test
    public void testValueThatBecomesOld() {
        LOG.info("testValueThatBecomesOld");

        final MetricsCollector metricsCollector =
                new MetricsCollector(Duration.millis(METRIC_DURATION_MSECS), METRIC_NR_ENTRIES);

        // Verify empty.
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 0, Float.NaN, Float.NaN, 0.0f, Float.NaN, Float.NaN);

        // Verify 1 data item.
        metricsCollector.addValueNow(1.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 1, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f);

        // Verify 2 data items.
        metricsCollector.addValueNow(2.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 2, 1.0f, 2.0f, 3.0f, 1.5f, 0.70710677f);

        // Time-out data.
        sleep(METRIC_DURATION_MSECS + 100);

        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 0, Float.NaN, Float.NaN, 0.0f, Float.NaN, Float.NaN);
        assertThat(metricsCollector.getStdDev(), is(Float.NaN));

        // Verify 1 data item.
        metricsCollector.addValueNow(3.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 1, 3.0f, 3.0f, 3.0f, 3.0f, 0.0f);

        // Verify 2 data items.
        metricsCollector.addValueNow(4.0f);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 2, 3.0f, 4.0f, 7.0f, 3.5f, 0.70710677f);

        // Time-out data.
        sleep(METRIC_DURATION_MSECS + 100);

        // Add two values and verify again including standard deviation to be zero.
        final DateTime newTime = UTCTime.now();
        metricsCollector.addValue(9.0f, newTime);
        metricsCollector.addValue(9.0f, newTime);
        verifyCollectorCountMinMaxSumAvgStdDev(metricsCollector, 2, 9.0f, 9.0f, 18.0f, 9.0f, 0.0f);
    }

    private static void verifyCollectorCountMinMaxSumAvgStdDev(
            @Nonnull final MetricsCollector metricsCollector,
            final int expectedCount,
            final float expectedMin, final float expectedMax, final float expectedSum,
            final float expectedAvg,
            final float expectedStdDev) {
        assertThat("Count", metricsCollector.getCount(), is(expectedCount));
        assertThat("Min", metricsCollector.getMin(), is(expectedMin));
        assertThat("Max", metricsCollector.getMax(), is(expectedMax));
        assertThat("Sum", metricsCollector.getSum(), is(expectedSum));
        assertThat("Avg", metricsCollector.getAvg(), is(expectedAvg));
        assertThat("StdDev", metricsCollector.getStdDev(), is(expectedStdDev));
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static void sleep(final long millis) {
        final DateTime start = UTCTime.now();
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            LOG.warn("Interrupted while sleeping");
        }
        if (UTCTime.now().minusMillis((int) millis).isBefore(start)) {
            Assert.fail("Did not sleep long enough.");
        }
    }
}
