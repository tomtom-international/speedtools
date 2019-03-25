/*
 * Copyright (C) 2012-2019, TomTom (http://tomtom.com).
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

import javax.annotation.Nonnull;

/**
 * Composite of metric data that collects metric data for each predefined period.
 */
public interface MultiMetricsData {

    enum Period {
        LAST_MONTH, LAST_WEEK, LAST_DAY, LAST_HOUR, LAST_MINUTE
    }

    @Nonnull
    MetricsData getMetricsData(Period period);

    @Nonnull
    MetricsData getLastMonth();

    @Nonnull
    MetricsData getLastWeek();

    @Nonnull
    MetricsData getLastDay();

    @Nonnull
    MetricsData getLastHour();

    @Nonnull
    MetricsData getLastMinute();
}
