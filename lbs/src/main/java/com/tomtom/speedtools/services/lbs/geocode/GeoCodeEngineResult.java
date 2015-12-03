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

package com.tomtom.speedtools.services.lbs.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tomtom.speedtools.geometry.Geo;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.json.JsonRenderable;
import com.tomtom.speedtools.utils.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import static com.tomtom.speedtools.utils.StringUtils.trim;


/**
 * This class defines individual geocoding results in a TomTom LBS Geocoding response.
 *
 * See {@link GeoCodeEngine}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public final class GeoCodeEngineResult implements JsonRenderable {
    /**
     * The latitude of the found location. Always populated.
     */
    private float latitude = 0.0f;

    /**
     * The longitude of the found location. Always populated.
     */
    private float longitude = 0.0f;

    /**
     * A geohash of the result (@see http://en.wikipedia.org/wiki/Geohash).
     */
    @Nullable
    private String geoHash = "";

    /**
     * Shows the map the geocoded results come from.
     */
    @Nullable
    private String mapName = "";

    /**
     * Name of the result.
     */
    @Nullable
    private String name = "";

    /**
     * The type of result returned, also know as the Geocoding Accuracy Level (or GAL). The level of accuracy which we
     * have achieved for this geocoded result (house, street, city, etc.).
     */
    @Nullable
    private String type = "";

    /**
     * The number of the house found. Always populated when the GAL level is Address, Interpolated House Number, or POI
     * has a known house number.
     */
    @Nullable
    private String houseNumber = "";

    /**
     * The name of the street found. Always populated when the GAL level is Address, Interpolated House Number or POI.
     */
    @Nullable
    private String street = "";

    /**
     * The name of the city. Always populated when the GAL level is anything other than Country or State.
     */
    @Nullable
    private String city;

    /**
     * The name of the district found.
     */
    @Nullable
    private String district = "";

    /**
     * The name of the state. Always populated when the GAL level is anything other than Country.
     */
    @Nullable
    private String state = "";

    /**
     * The short or extended postcode. Populated whenever we have data for the found address (only for a type/GAL of
     * Address, House, Street and POI).
     */
    @Nullable
    private String postcode = "";

    /**
     * The name of the country. Always populated.
     */
    @Nullable
    private String country;

    /**
     * Country code, ISO 3 characters.
     */
    @Nullable
    private String countryISO3 = "";

    /**
     * The calculated address of the result, this may differ from the correct postal address. Always populated.
     */
    @Nullable
    private String formattedAddress = "";

    /**
     * Indicates if the house number should appear before or after the street name. If snp=S then the street name comes
     * first; and if snp=N then the house number comes first. Only present if the result type is house.
     */
    @Nullable
    private String snp = "";

    /**
     * The suggested horizontal extent, used by WBRP to calculate zoom level. If the result type/GAL is a house, street
     * or POI the value return will be 0.
     */
    private float widthMeters = 0.0f;

    /**
     * The suggested vertical extent, used by WBRP to calculate zoom level. If the result type/GAL is a house, street or
     * POI the value return will be 0.
     */
    private float heightMeters = 0.0f;

    /**
     * the importance of the result relative to any other results contained in the same response, with 1.0 being the
     * highest. At least 1 result in the response will have a score of 1.0 and multiple results within a response may
     * have the same score. Note: The score is not a measure of absolute accuracy, if a query only yields poor matches,
     * the "least poor" match will be given a score of 1.0.
     */
    private float score = 0.0f;

    /**
     * Default language.
     */
    @Nullable
    private String defaultLanguage = "";

    /**
     * Map version.
     */
    @Nullable
    private String mapVersion = "";

    @XmlElement(name = "city", required = false)
    @Nullable
    public String getCity() {
        return city;
    }

    public void setCity(@Nullable final String city) {
        this.city = trim(city);
    }

    @XmlElement(name = "district", required = false)
    @Nullable
    public String getDistrict() {
        return district;
    }

    public void setDistrict(@Nullable final String district) {
        this.district = trim(district);
    }

    @XmlElement(name = "country", required = false)
    @Nullable
    public String getCountry() {
        return country;
    }

    public void setCountry(@Nullable final String country) {
        this.country = trim(country);
    }

    @XmlElement(name = "countryISO3", required = false)
    @Nullable
    public String getCountryISO3() {
        return countryISO3;
    }

    public void setCountryISO3(@Nullable final String countryISO3) {
        this.countryISO3 = trim(countryISO3);
    }

    @XmlElement(name = "formattedAddress", required = false)
    @Nullable
    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(@Nullable final String formattedAddress) {
        this.formattedAddress = trim(formattedAddress);
    }

    @XmlElement(name = "geohash", required = false)
    @Nullable
    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(@Nullable final String geoHash) {
        this.geoHash = trim(geoHash);
    }

    public float getHeightMeters() {
        return heightMeters;
    }

    @XmlElement(name = "heightMeters", required = false)
    public void setHeightMeters(final float heightMeters) {
        this.heightMeters = heightMeters;
    }

    @XmlElement(name = "houseNumber", required = false)
    @Nullable
    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(@Nullable final String houseNumber) {
        this.houseNumber = trim(houseNumber);
    }

    @XmlElement(name = "latitude", required = true)
    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(final float latitude) {
        assert MathUtils.isBetween(latitude, -90.0, 90.0) : latitude;
        this.latitude = latitude;
    }

    @XmlElement(name = "longitude", required = true)
    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(final float longitude) {
        assert MathUtils.isBetween(longitude, -180.0, Geo.LON180) : longitude;
        this.longitude = longitude;
    }

    @XmlElement(name = "mapName", required = false)
    @Nullable
    public String getMapName() {
        return mapName;
    }

    public void setMapName(@Nullable final String mapName) {
        this.mapName = trim(mapName);
    }

    @XmlElement(name = "name", required = false)
    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = trim(name);
    }

    @XmlElement(name = "postcode", required = false)
    @Nullable
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(@Nullable final String postcode) {
        this.postcode = trim(postcode);
    }

    @XmlElement(name = "score", required = false)
    public float getScore() {
        return score;
    }

    public void setScore(final float score) {
        this.score = score;
    }

    @XmlElement(name = "snp", required = false)
    @Nullable
    public String getSnp() {
        return snp;
    }

    public void setSnp(@Nonnull final String snp) {
        assert snp != null;
        this.snp = trim(snp);
    }

    @XmlElement(name = "state", required = false)
    @Nullable
    public String getState() {
        return state;
    }

    public void setState(@Nullable final String state) {
        this.state = trim(state);
    }

    @XmlElement(name = "street", required = false)
    @Nullable
    public String getStreet() {
        return street;
    }

    public void setStreet(@Nullable final String street) {
        this.street = trim(street);
    }

    @XmlElement(name = "type", required = false)
    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable final String type) {
        this.type = trim(type);
    }

    @XmlElement(name = "widthMeters", required = false)
    public float getWidthMeters() {
        return widthMeters;
    }

    public void setWidthMeters(final float widthMeters) {
        this.widthMeters = widthMeters;
    }

    @XmlElement(name = "defaultLanguage", required = false)
    @Nullable
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(@Nullable final String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    @XmlElement(name = "mapVersion", required = false)
    @Nullable
    public String getMapVersion() {
        return mapVersion;
    }

    public void setMapVersion(@Nullable final String mapVersion) {
        this.mapVersion = mapVersion;
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
