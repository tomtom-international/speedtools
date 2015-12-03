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

package com.tomtom.speedtools.mongodb.mappers;

import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.gpstrace.GpsTracePoint;
import org.joda.time.DateTime;

public class GpsTracePointMapper extends EntityMapper<GpsTracePoint> {
    public final EntityType entityType = entityType(GpsTracePoint.class, "gpsTracePoint");

    public final Field<DateTime> time = dateTimeField("time", "getTime", CONSTRUCTOR);
    public final Field<GeoPoint> position = field("position", GeoPointMapper.class, "getPosition", CONSTRUCTOR);
}
