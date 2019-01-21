/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.rest;

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import com.google.inject.Injector;
import com.tomtom.speedtools.guice.HasProperties;
import com.tomtom.speedtools.guice.InvalidPropertyValueException;
import com.tomtom.speedtools.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

/**
 * Standard supervisor strategies.
 */
public class SupervisorStrategies implements HasProperties {
    private static final Logger LOG = LoggerFactory.getLogger(SupervisorStrategies.class);

    @Nonnull
    private final OneForOneStrategy restartChildStrategy;
    @Nonnull
    private final OneForOneStrategy stopChildStrategy;

    private static final int MAX_RETRIES = 100;
    private static final int MAX_WITHIN_TIME_RANGE_SECS = 60 * 60 * 24; // 1 day.

    /**
     * Akka supervisor properties.
     *
     * @param injector            Injector.
     * @param maxNrOfRetries      Maximum number a child can be restarted within restartChildWithinTimeRangeSecs before
     *                            it dies. Must be &gt;= 0.
     * @param withinTimeRangeSecs If more than restartChildMaxNrOfRetries failures of a child occur within
     *                            restartChildWithinTimeRangeSecs, the child dies. Must be &gt;= 1.
     */
    @Inject
    public SupervisorStrategies(
            @Nonnull final Injector injector,
            @Named("SupervisorStrategies.RestartChild.maxNrOfRetries") final int maxNrOfRetries,
            @Named("SupervisorStrategies.RestartChild.withinTimeRangeSecs") final int withinTimeRangeSecs)
            throws InvalidPropertyValueException {
        assert injector != null;

        if (!MathUtils.isBetween(maxNrOfRetries, 0, MAX_RETRIES)) {
            throw new InvalidPropertyValueException("RestartChildStragy.maxNrOfRetries should be in range [0, " +
                    MAX_RETRIES + "].");
        }

        if (!MathUtils.isBetween(withinTimeRangeSecs, 1, MAX_WITHIN_TIME_RANGE_SECS)) {
            throw new InvalidPropertyValueException("RestartChildStragy.withinTimeRangeSecs should be in range [1, " +
                    MAX_WITHIN_TIME_RANGE_SECS + "].");
        }

        this.restartChildStrategy = new OneForOneStrategy(
                maxNrOfRetries,
                FiniteDuration.create(withinTimeRangeSecs, TimeUnit.SECONDS), (Throwable param) -> {

            assert param != null;
            if (param instanceof Error) {
                LOG.error("apply (restartChildStrategy): Error occurred in actor", param);
            }
            LOG.warn("apply (restartChildStrategy): Actor restarted (maxNrOfRetries=" +
                    maxNrOfRetries + ", " +
                    "withinTimeRangeSecs=" +
                    withinTimeRangeSecs + ')', param);
            return SupervisorStrategy.restart();
        }
        );

        this.stopChildStrategy = new OneForOneStrategy(0, Duration.Inf(), (Throwable param) -> {
            assert param != null;
            if (param instanceof Error) {
                LOG.error("apply (stopChildStrategy): Error occurred in actor", param);
            }
            LOG.warn("apply (stopChildStrategy): Actor stopped", param);
            return SupervisorStrategy.stop();
        }
        );
    }

    @Nonnull
    public OneForOneStrategy getRestartChildStrategy() {
        return restartChildStrategy;
    }

    @Nonnull
    public OneForOneStrategy getStopChildStrategy() {
        return stopChildStrategy;
    }
}
