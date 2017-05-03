/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

import com.tomtom.speedtools.objects.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapperRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(MapperRegistry.class);

    @Nonnull
    private final Map<Class<? extends Mapper<?>>, Mapper<?>> mappers =
            new HashMap<>();

    @Nonnull
    private final Map<Mapper<?>, List<EntityMapper<?>.SuperEntity>> subEntityMappers =
            new HashMap<>();

    @Nonnull
    private final Set<Mapper<?>> initializedMappers = new HashSet<>();

    /**
     * There can only be one sub mapper cache per MapperRegistry. This Map used to be a static member of EntityMapper
     * but it is moved to the MapperRegistry as a instance variable. This was to ensure that there could only be single
     * instances of a particular Mapper. <br/> During unit tests for each test a MapperRegistry was created and the
     * static member had Mappers registered from a old test. This caused mis matching of fields and led to unpredictable
     * behaviour.
     */
    @Nonnull
    private final ConcurrentHashMap<Tuple<Class<?>, Class<?>>, EntityMapper<?>>
            subMapperCache = new ConcurrentHashMap<>();

    // Static initialization of other mappers.
    {
        try {
            register(
                    new GeoAreaMapper(),
                    new GeoPointMapper(),
                    new GeoRectangleMapper(),
                    new GpsTraceMapper(),
                    new GpsTracePointMapper(),
                    new LocalDateMapper(),
                    new LocaleMapper(),
                    new LongMapper(),
                    new MoneyMapper(),
                    new URLMapper()
            );
        } catch (final SchemaException e) {
            LOG.error("Exception during initialization of mappers: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void register(@Nonnull final Mapper<?>... mappers) throws SchemaException {
        assert mappers != null;

        // Implicit mappers.
        final boolean addImplicitMappers = (mappers.length > 0) && this.mappers.isEmpty();

        // Pre-initialize entity mappers.
        for (final Mapper<?> mapper : mappers) {
            if (!initializedMappers.contains(mapper)) {
                if (mapper instanceof EntityMapper) {
                    final EntityMapper<?> entityMapper = (EntityMapper<?>) mapper;
                    entityMapper.preInitialize(this);
                }
                this.mappers.put((Class<? extends Mapper<?>>) mapper.getClass(), mapper);
            }
        }

        // Implicit mappers.
        if (addImplicitMappers) {
            register(
                    new BinaryMapper(),
                    new BooleanMapper(),
                    new CurrencyMapper(),
                    new DateTimeMapper(),
                    new DoubleMapper(),
                    new IntegerMapper(),
                    new ReferenceMapper<>(),
                    new StringMapper()
            );
        }

        // Initialize mappers.
        for (final Mapper<?> mapper : mappers) {
            if (!initializedMappers.contains(mapper)) {
                mapper.initialize(this);
            }
        }

        // Check super mappers.
        for (final Mapper<?> mapper : mappers) {
            if (!initializedMappers.contains(mapper) &&
                    (mapper instanceof EntityMapper)) {
                checkSuperMappers((EntityMapper<?>) mapper, new HashSet<>());
            }
        }

        // Build sub-entity map.
        for (final Mapper<?> mapper : mappers) {
            if (!initializedMappers.contains(mapper) &&
                    (mapper instanceof EntityMapper)) {
                final EntityMapper<?> entityMapper = ((EntityMapper<?>) mapper);
                for (final EntityMapper<?>.SuperEntity superEntity : entityMapper.getSuperEntities()) {
                    List<EntityMapper<?>.SuperEntity> subMappers =
                            subEntityMappers.get(superEntity.getSuperMapper());
                    if (subMappers == null) {
                        subMappers = new ArrayList<>();
                        subEntityMappers.put(superEntity.getSuperMapper(), subMappers);
                    }
                    subMappers.add(superEntity);
                }
            }
        }

        Collections.addAll(initializedMappers, mappers);
    }

    /**
     * Returns all registered mappers, in no particular order.
     *
     * @return Set of mappers.
     */
    @Nonnull
    public Set<Mapper<?>> getMappers() {
        return new HashSet<>(mappers.values());
    }

    /**
     * Returns the entity mapper for given entity type, if the schema contains one. MapperRegistry should be initialized
     * before use.
     *
     * @param mapperType Type of the mapper.
     * @param <M>        Type of the mapper.
     * @return Mapper for entity type.
     * @throws SchemaException When mapper type was not registered.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <M extends Mapper<?>> M getMapper(@Nonnull final Class<? extends M> mapperType) throws SchemaException {
        assert mapperType != null;
        final M mapper = (M) mappers.get(mapperType);
        if (mapper == null) {
            throw new SchemaException("Mapper not registered: " + mapperType.getSimpleName());
        }
        return mapper;
    }

    /**
     * Returns the entity mapper for given entity type, if the schema contains one. MapperRegistry should be initialized
     * before use.
     *
     * @param mapperType Type of the mapper.
     * @param <M>        Type of the mapper.
     * @return Mapper for entity type.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <M extends Mapper<?>> M findMapper(@Nonnull final Class<? extends M> mapperType) {
        assert mapperType != null;
        return (M) mappers.get(mapperType);
    }

    /**
     * Print a database schema of the registered mappers in HTML form.
     *
     * @return Html schema.
     */
    @Nonnull
    public String toHtml() {
        return new SchemaPrinter(this).printHtml();
    }

    /**
     * Returns the entity mapper for given entity type, if the schema contains one. MapperRegistry should be initialized
     * before use.
     *
     * @param mapperType Type of the mapper.
     * @param <M>        Type of the mapper.
     * @return Mapper for entity type.
     * @throws SchemaException When mapper type was not registered.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <M extends Mapper<?>> M getOrRegisterMapper(@Nonnull final Class<? extends M> mapperType) throws
            SchemaException {
        assert mapperType != null;
        M mapper = (M) mappers.get(mapperType);
        if (mapper == null) {
            try {
                mapper = mapperType.getConstructor().newInstance();
            } catch (final Exception e) {
                LOG.error("getOrRegisterMapper: mapper not registered, mapperType=" + mapperType.getSimpleName(), e);
                throw new SchemaException("Mapper not registered: " + mapperType.getSimpleName(), e);
            }
            register(mapper);
        }
        return mapper;
    }


    /**
     * Returns the direct sub-entity mappers of given mapper. Note that SuperEntity instances are returned, instead of
     * the actual mapper. This way, version constraints can be accessed. Use SuperEntity#getParent() to get the actual
     * mapper of the sub-entity.
     *
     * @param mapper Mapper to find sub-mappers for.
     * @return Sub-entity mapper super declarations.
     */
    @Nonnull
    List<EntityMapper<?>.SuperEntity> getSubEntities(@Nonnull final Mapper<?> mapper) {
        assert mapper != null;
        final List<EntityMapper<?>.SuperEntity> subMappers = subEntityMappers.get(mapper);
        if (subMappers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(subMappers);
    }

    /**
     * This methods grants access to a Map with sub mappers.
     *
     * @return subMapperCache with cached sub mappers.
     */
    @Nonnull
    Map<Tuple<Class<?>, Class<?>>, EntityMapper<?>> getSubMapperCache() {
        return subMapperCache;
    }

    /**
     * Checks super mappers. All super mappers should be registered. Performs cycle detection.
     *
     * @param mapper Mapper to check.
     * @param seen   Set to detect cycles.
     * @throws SchemaException Thrown when a schema error is found.
     */
    private void checkSuperMappers(@Nonnull final EntityMapper<?> mapper, @Nonnull final Set<EntityMapper<?>> seen) throws SchemaException {
        assert mapper != null;
        assert seen != null;

        // Cycle detection.
        if (!seen.add(mapper)) {
            throw new SchemaException(mapper, "Cycle detected at in super-entity hierarchy");
        }

        // Recurse over super entities.
        for (final EntityMapper<?>.SuperEntity superEntity : mapper.getSuperEntities()) {
            // Find registered mapper for super entity.
            final Mapper<?> superMapper = superEntity.getSuperMapper();

            // Recurse
            assert superMapper instanceof EntityMapper;
            checkSuperMappers((EntityMapper<?>) superMapper, seen);
        }
    }
}
