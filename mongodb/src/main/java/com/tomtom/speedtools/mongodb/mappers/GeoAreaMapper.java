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

package com.tomtom.speedtools.mongodb.mappers;

import com.mongodb.DBObject;
import com.tomtom.speedtools.geometry.GeoArea;
import com.tomtom.speedtools.geometry.GeoRectangle;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

public class GeoAreaMapper extends EntityMapper<GeoArea> {
    public final EntityType entityType = entityType(GeoArea.class, "geoArea");

    public final Field<Collection<GeoRectangle>> rects =
            collectionField("rects", GeoRectangleMapper.class, "pixelate", NO_SETTER);

    @Nonnull
    @Override
    protected GeoArea newInstance(
            @Nonnull final DBObject dbObject,
            @Nonnull final Set<EntityMapper<?>.Field<?>> initializedFields) throws MapperException {
        assert dbObject != null;
        assert initializedFields != null;

        final Collection<GeoRectangle> geoRectangles = rects.fromDbObject(dbObject);
        if ((geoRectangles == null) || geoRectangles.isEmpty()) {
            throw new MapperException("Cannot create empty GeoArea instance.");
        }

        final GeoArea result = GeoArea.fromAreas(geoRectangles);

        // We have set this field.
        initializedFields.add(rects);

        return result;
    }
}
