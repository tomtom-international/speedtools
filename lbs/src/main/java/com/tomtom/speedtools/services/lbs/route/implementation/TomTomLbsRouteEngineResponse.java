/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.services.lbs.route.implementation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.services.lbs.route.RouteEngineResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;


/**
 * This class defines a top-level TomTom LBS Router response.
 */
@XmlRootElement(namespace = "http://lbs.tomtom.com/services", name = "route")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public final class TomTomLbsRouteEngineResponse implements JsonRenderable {

    @Nullable
    private String version;
    @Nullable
    private RouteEngineSummaryForLbs summary;

    public TomTomLbsRouteEngineResponse() {
        version = null;
        summary = null;
    }

    @XmlElement(namespace = "http://lbs.tomtom.com/services", name = "summary", required = false)
    @Nullable
    public RouteEngineSummaryForLbs getSummary() {
        return summary;
    }

    public void setSummary(@Nullable final RouteEngineSummaryForLbs summary) {
        this.summary = summary;
    }

    @XmlAttribute(name = "version", required = false)
    @Nullable
    public String getVersion() {
        return version;
    }

    public void setVersion(@Nullable final String version) {
        this.version = version;
    }

    public boolean isValid() {
        return (summary != null) && summary.isValid();
    }

    @Nonnull
    public RouteEngineResponse convertToRouteEngineResponse() {
        assert summary != null;
        final RouteEngineResponse response = new RouteEngineResponse(
                summary.getTotalDelaySeconds(),
                summary.getTotalDistanceMeters(),
                summary.getTotalTimeSeconds());
        return response;
    }

    @Override
    @Nonnull
    public String toJson() {
        return Json.toJson(this);
    }

    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }

    /**
     * Nested summary class for LBS response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
    public static class RouteEngineSummaryForLbs implements JsonRenderable {

        private int totalDelaySeconds;
        private int totalDistanceMeters;
        private int totalTimeSeconds;

        public RouteEngineSummaryForLbs() {
            totalDelaySeconds = 0;
            totalDistanceMeters = 0;
            totalTimeSeconds = 0;
        }

        @XmlElement(namespace = "http://lbs.tomtom.com/services", name = "totalDelaySeconds", required = true)
        public int getTotalDelaySeconds() {
            return totalDelaySeconds;
        }

        public void setTotalDelaySeconds(final int totalDelaySeconds) {
            this.totalDelaySeconds = totalDelaySeconds;
        }

        @XmlElement(namespace = "http://lbs.tomtom.com/services", name = "totalDistanceMeters", required = true)
        public int getTotalDistanceMeters() {
            return totalDistanceMeters;
        }

        public void setTotalDistanceMeters(final int totalDistanceMeters) {
            this.totalDistanceMeters = totalDistanceMeters;
        }

        @XmlElement(namespace = "http://lbs.tomtom.com/services", name = "totalTimeSeconds", required = true)
        public int getTotalTimeSeconds() {
            return totalTimeSeconds;
        }

        public void setTotalTimeSeconds(final int totalTimeSeconds) {
            this.totalTimeSeconds = totalTimeSeconds;
        }

        public boolean isValid() {
            return (totalDelaySeconds >= 0) && (totalDistanceMeters >= 0) && (totalTimeSeconds >= 0);
        }

        @Override
        @Nonnull
        public String toJson() {
            return Json.toJson(this);
        }

        @Override
        @Nonnull
        public String toString() {
            return Json.toStringJson(this);
        }
    }
}
