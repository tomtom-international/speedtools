/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.services.lbs.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tomtom.speedtools.utils.StringUtils.nullToEmpty;
import static com.tomtom.speedtools.utils.StringUtils.trim;


/**
 * This class defines a top-level TomTom LBS Geocoding response.
 *
 * See {@link GeoCodeEngine}.
 */
@XmlRootElement(name = "geoResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public final class GeoCodeEngineResponse implements JsonRenderable {

    /**
     * Time it took to do the geo-coding, in msec.
     */
    private int durationMsec = 0;

    /**
     * Number of geocoding results. In case of an error, this field is 0.
     */
    private int count = 0;

    /**
     * Geocoder version (not API version).
     */
    @Nonnull
    private String apiVersion = "";

    /**
     * List of geocoding results. Possibly empty.
     */
    @Nonnull
    private List<GeoCodeEngineResult> geoCodeEngineResultList = new ArrayList<>();

    @XmlAttribute(name = "count", required = true)
    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        assert count >= 0;
        this.count = count;
    }

    @XmlAttribute(name = "duration", required = true)
    public int getDurationMsec() {
        return durationMsec;
    }

    public void setDurationMsec(final int durationMsec) {
        assert durationMsec >= 0;
        this.durationMsec = durationMsec;
    }

    @XmlAttribute(name = "version", required = true)
    @Nonnull
    public String getApiVersion() {
        assert apiVersion != null;
        return apiVersion;
    }

    public void setApiVersion(@Nullable final String apiVersion) {
        this.apiVersion = nullToEmpty(trim(apiVersion));
    }

    @XmlElement(name = "geoResult", required = false)
    @Nonnull
    public List<GeoCodeEngineResult> getGeoCodeEngineResultList() {
        return geoCodeEngineResultList;
    }

    public void setGeoCodeEngineResultList(@Nullable final List<GeoCodeEngineResult> geoCodeEngineResultList) {
        this.geoCodeEngineResultList = (geoCodeEngineResultList == null) ?
                Collections.<GeoCodeEngineResult>emptyList() : geoCodeEngineResultList;
    }

    /**
     * Provide a Json representation of a geo object.
     *
     * @return Json representation of the object.
     */
    @Override
    @Nonnull
    public String toJson() {
        return Json.toJson(this);
    }

    /**
     * Override of toString() function to provide a Json representation of a geo object.
     *
     * @return Json representation of the object.
     */
    @Override
    @Nonnull
    public String toString() {
        return Json.toStringJson(this);
    }
}

