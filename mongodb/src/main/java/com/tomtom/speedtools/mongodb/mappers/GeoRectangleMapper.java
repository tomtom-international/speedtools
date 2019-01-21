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
import com.tomtom.speedtools.geometry.GeoRectangle;

public class GeoRectangleMapper extends EntityMapper<GeoRectangle> {
    public final EntityType entityType = entityType(GeoRectangle.class);

    public final Field<GeoPoint> southWest = field("southWest", GeoPointMapper.class, "getSouthWest", CONSTRUCTOR);
    public final Field<GeoPoint> northEast = field("northEast", GeoPointMapper.class, "getNorthEast", CONSTRUCTOR);
}
