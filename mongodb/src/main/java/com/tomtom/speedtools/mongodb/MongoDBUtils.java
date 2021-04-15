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

package com.tomtom.speedtools.mongodb;

import com.mongodb.BasicDBObject;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.geometry.GeoRectangle;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper.HasFieldName;
import com.tomtom.speedtools.mongodb.mappers.GeoPointMapper;
import com.tomtom.speedtools.mongodb.mappers.MapperException;

import javax.annotation.Nonnull;

/**
 * This class provides generic methods to operate on Mongo Collections.
 */
public final class MongoDBUtils {

    /**
     * The MONGO_PATH_SEPARATOR is the concatenation string used by Mongo to concatenate fields (the 'dot' notation).
     */
    public static final String MONGO_PATH_SEPARATOR = ".";

    /**
     * Geo boxes are not allowed to be empty in Mongo. MONGO_BOX_MINIMUM_SIZE indicate the minimum size.
     */
    private static final double MONGO_BOX_MINIMUM_SIZE = 0.0000000001;

    private MongoDBUtils() {
        // Prevent instantiation.
    }

    /**
     * Create a mongo path based on HasFieldName parts.
     *
     * @param parts The parts that make up the Mongo path. Cannot be empty.
     * @return A string representing the Mongo path.
     */
    @Nonnull
    public static String mongoPath(@Nonnull final HasFieldName... parts) {
        assert parts != null;

        final StringBuilder result = new StringBuilder();

        String sep = "";
        for (final HasFieldName field : parts) {
            result.append(sep).append(field.getFieldName());
            sep = MONGO_PATH_SEPARATOR;
        }

        return result.toString();
    }

    /**
     * This method will return a {@link BasicDBObject} representing a Mongo box. In case the {@link GeoRectangle} is
     * zero the area will be stretched slightly in order to create a valid object. Mongo does not allow zero sized
     * boxes.
     *
     * @param nonWrappedRect The rectangle that is the target for the box object. This rectangle may NOT be wrapped. The
     *                       caller must make sure this is the case. This can be done by invoking the {@link
     *                       GeoRectangle}.pixelate() function first, which may return multiple, non-wrapped rectangles.
     * @param geoPointMapper The mapper that knows how to map Java objects to Mongo.
     * @return A {@link BasicDBObject} representing a Mongo box.
     * @throws MapperException If the area could not be mapped.
     */
    public static BasicDBObject createBasicDBObjectFromRectangle(
            @Nonnull final GeoRectangle nonWrappedRect,
            @Nonnull final GeoPointMapper geoPointMapper) throws MapperException {
        assert nonWrappedRect != null;
        assert !nonWrappedRect.isWrapped();
        assert geoPointMapper != null;

        /**
         * Make sure the box covers a non-zero area. So, northing AND easting must be > 0.
         */
        final GeoPoint southWest = nonWrappedRect.getSouthWest();
        final double northing = Math.max(MONGO_BOX_MINIMUM_SIZE, nonWrappedRect.getNorthing());
        final double easting = Math.max(MONGO_BOX_MINIMUM_SIZE, nonWrappedRect.getEasting());
        assert northing > 0.0;
        assert easting > 0.0;

        final BasicDBObject box = new BasicDBObject("$box", new Object[]{
                geoPointMapper.toDb(southWest),
                geoPointMapper.toDb(southWest.
                        withLat(southWest.getLat() + northing).
                        withLon(southWest.getLon() + easting))});
        assert box != null;
        return box;
    }
}
