/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

/**
 * Interface to retrieve metrics data.
 */
public interface MetricsData {

    /**
     * Return the total number of metric data items added up until now since the duration (approx) of this metric.
     *
     * @return Total number of data items.
     */
    int getCount();

    /**
     * Return the sum of all metric data added up until now since the duration (approx) of this metric.
     *
     * @return Sum of metric data.
     */
    float getSum();

    /**
     * Return the average of all metric data added up until now since the duration (approx) of this metric.
     *
     * @return Average of metric data, or {@link Float#NaN} if no data has been added.
     */
    float getAvg();

    /**
     * Get standard deviation.
     *
     * @return The the sample standard deviation of all metric data added up until now since the duration (approx) of
     * this metric, or {@link Float#NaN} if no data has been added.
     */
    float getStdDev();

    /**
     * Get maximum.
     *
     * @return The maximum value of all metric data added up until now since the duration (approx) of this metric, or
     * {@link Float#NaN} if no data has been added.
     */
    float getMax();

    /**
     * Get minimum.
     *
     * @return The minimum value of all metric data added up until now since the duration (approx) of this metric, or
     * {@link Float#NaN} if no data has been added.
     */
    float getMin();
}
