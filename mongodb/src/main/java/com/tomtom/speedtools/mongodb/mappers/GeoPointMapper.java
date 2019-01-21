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

package com.tomtom.speedtools.mongodb.mappers;

import com.tomtom.speedtools.geometry.GeoPoint;

public class GeoPointMapper extends EntityMapper<GeoPoint> {
    public final EntityType entityType = entityType(GeoPoint.class, "geoPoint");

    public final Field<Double> lat = doubleField("lat", "getLat", CONSTRUCTOR);
    public final Field<Double> lon = doubleField("lon", "getLon", CONSTRUCTOR);
    public final Field<Double> elevationMeters = doubleField("elevationMeters", "getElevationMeters", CONSTRUCTOR);
}
