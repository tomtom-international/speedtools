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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.mongodb.MongoDBKeyNames;
import com.tomtom.speedtools.objects.Tuple;
import com.tomtom.speedtools.utils.MathUtils;
import com.tomtom.speedtools.utils.SetUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public abstract class EntityMapper<T> extends Mapper<T> {
    private static final Logger LOG = LoggerFactory.getLogger(EntityMapper.class);

    @Nonnull
    private static final Set<String> predefinedFieldNames =
            new HashSet<>(Arrays.asList(MongoDBKeyNames.ID_KEY,
                    MongoDBKeyNames.DISCRIMINATOR_KEY, MongoDBKeyNames.LAST_MODIFIED_KEY, MongoDBKeyNames.VERSION_KEY));

    @Nonnull
    protected static final String CONSTRUCTOR = "{}__CONSTRUCTOR__{}";
    @Nonnull
    protected static final String NO_GETTER = "{}__NO_GETTER__{}";
    @Nonnull
    protected static final String NO_SETTER = "{}__NO_SETTER__{}";

    private boolean initialized = false;
    @Nullable
    private MapperRegistry mapperRegistry = null;
    @Nonnull
    private final List<EntityType> entityTypes = new ArrayList<>();
    @Nonnull
    private final List<CurrentVersion> currentVersions = new ArrayList<>();
    @Nonnull
    private final List<Field<?>> fields = new ArrayList<>();
    @Nonnull
    private final Map<String, Field<?>> fieldExtent = new LinkedHashMap<>();
    @Nonnull
    private final List<Field<?>> constructorFieldExtent = new ArrayList<>();
    @Nonnull
    private final List<SuperEntity> superEntities = new ArrayList<>();
    private int currentVersion = 0;
    @Nullable
    private Class<T> entityType = null;
    @Nonnull
    private String discriminator = "";
    @Nullable
    private Constructor<?> constructor = null;

    /**
     * Gets the current version of this mapper.  <p>Note that the corresponding mapperRegistry should be initialized
     * first.
     *
     * @return Current version.
     */
    public int getCurrentVersion() {
        assert mapperRegistry != null;
        return currentVersion;
    }

    /**
     * Converts a database object to an entity.
     *
     * Note that the corresponding mapperRegistry should be initialized first.
     *
     * @param dbValue Database object to convert to an entity.
     * @return Entity, or null (only) if the original object value was null.
     * @throws MapperException Thrown when there were mapping errors.
     */
    @Nullable
    @Override
    public T fromDb(@Nullable final Object dbValue) throws MapperException {
        assert mapperRegistry != null;
        assert initialized;
        if (dbValue == null) {
            return null;
        }
        if (dbValue instanceof DBObject) {
            final List<MapperError> errors = new LinkedList<>();
            final T entity = fromDb((DBObject) dbValue, errors);
            if (!errors.isEmpty()) {
                throw new MapperException(errors);
            }
            return entity;
        }
        throw new MapperException(this, "Nested DBObject expected, got: " + dbValue);
    }

    /**
     * Converts a database object to an entity. A null value can be returned, but will always cause an error to be added
     * to errors.  <p>Note that the corresponding mapperRegistry should be initialized first.
     *
     * @param <T2>     Subtype.
     * @param dbObject Database object to convert to an entity.
     * @param errors   Container for errors that might occur.
     * @return Entity, possibly null.
     */
    @Nullable
    public <T2 extends T> T fromDb(@Nonnull final DBObject dbObject, @Nonnull final List<MapperError> errors) {
        assert dbObject != null;
        assert errors != null;
        assert mapperRegistry != null;
        assert initialized;

        // Get database object version.
        final int version = getVersion(dbObject, errors);

        // Get polymorphic mapper.
        final EntityMapper<T2> mapper = getSubMapper(dbObject, version, errors);

        // Create an mapper of the correct type.
        final T2 entity;
        try {
            final Set<EntityMapper<?>.Field<?>> initializedFields =
                    new LinkedHashSet<>(SetUtils.hashSetSize(fieldExtent.size()));
            entity = mapper.newInstance(dbObject, initializedFields);

            // Use the mapper to map all fields that were not initialized by newInstance().
            return mapper.setAll(entity, dbObject, initializedFields, version, errors, true);
        } catch (final MapperException e) {
            errors.addAll(e.getMapperErrors());
            return null;
        }
    }

    /**
     * Merges a database object into an existing entity. Fields will be overwritten, collections will be replaced.
     * <p>Note that the corresponding mapperRegistry should be initialized first.
     *
     * @param entity   Entity to merge into.
     * @param dbObject Data object to read data from.
     * @throws MapperException Thrown when there were mapping errors.
     */
    public void fromDb(@Nonnull final T entity, @Nonnull final DBObject dbObject) throws MapperException {
        assert entity != null;
        assert dbObject != null;
        assert mapperRegistry != null;
        assert initialized;

        final List<MapperError> errors = new LinkedList<>();
        fromDb(entity, dbObject, errors);
        if (!errors.isEmpty()) {
            throw new MapperException(errors);
        }
    }

    /**
     * Merges a database object into an existing entity. Fields will be overwritten, collections will be replaced.
     * <p>Note that the corresponding mapperRegistry should be initialized first.
     *
     * @param <T2>     Subtype.
     * @param entity   Entity to merge into.
     * @param dbObject Data object to read data from.
     * @param errors   Container for errors.
     * @return Resulting entity.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <T2 extends T> T fromDb(
            @Nonnull final T entity,
            @Nonnull final DBObject dbObject,
            @Nonnull final List<MapperError> errors) {
        assert entity != null;
        assert dbObject != null;
        assert errors != null;
        assert mapperRegistry != null;
        assert initialized;

        // Get database object version.
        final int version = getVersion(dbObject, errors);

        // Get specific sub mapper.
        final EntityMapper<T2> mapper = getSubMapper(dbObject, version, errors);

        // Use this mapper to map all fields.
        final Set<EntityMapper<?>.Field<?>> initializedFields =
                new LinkedHashSet<>(SetUtils.hashSetSize(fieldExtent.size()));
        return mapper.setAll((T2) entity, dbObject, initializedFields, version, errors, true);
    }

    /**
     * Converts a database object to an entity.  <p>Note that the corresponding mapperRegistry should be initialized
     * first.
     *
     * @param value Entity to convert to a database object.
     * @return Database object.
     * @throws MapperException Thrown when there were mapping errors.
     */
    @Nullable
    @Override
    public DBObject toDb(@Nullable final T value) throws MapperException {
        assert mapperRegistry != null;
        assert initialized;

        if (value == null) {
            return null;
        }
        final List<MapperError> errors = new LinkedList<>();
        final DBObject dbObject = toDb(value, errors);
        if (!errors.isEmpty()) {
            throw new MapperException(errors);
        }
        return dbObject;
    }

    /**
     * Converts an entity to a DBObject. Errors are collected into the errors parameters, they will not stop the
     * conversion. The most specific mapper is search for, so a mapper can be used to convert an instance of a
     * sub-class.  <p>Note that the corresponding mapperRegistry should be initialized first.
     *
     * @param entity Entity to convert to a database object.
     * @param errors Collection to collect errors in.
     * @return Converted database object, will never be null.
     */
    @Nonnull
    public DBObject toDb(@Nonnull final T entity, @Nonnull final List<MapperError> errors) {
        assert entity != null;
        assert errors != null;
        assert mapperRegistry != null;
        assert initialized;

        final BasicDBObject dbObject = new BasicDBObject();
        toDb(entity, dbObject, errors);
        return dbObject;
    }

    /**
     * Converts an entity into a given dbObject. Mapped fields will be copied onto the dbObject. Pre-existing values
     * will not be removed from the dbObject.  <p>Note that the corresponding mapperRegistry should be initialized
     * first.
     *
     * @param entity   Entity to copy data from. May not be null.
     * @param dbObject Database object to copy data into. May not be null.
     * @param errors   List to collect errors in.
     */
    public void toDb(
            @Nonnull final T entity,
            @Nonnull final DBObject dbObject,
            @Nonnull final List<MapperError> errors) {
        assert entity != null;
        assert dbObject != null;
        assert errors != null;
        assert mapperRegistry != null;
        assert initialized;

        final EntityMapper<T> mapper = getSubMapper(entity.getClass());
        mapper.getAll(entity, dbObject, Integer.MAX_VALUE, errors);

        // Add discriminator value.
        if (!mapper.discriminator.isEmpty()) {
            dbObject.put(MongoDBKeyNames.DISCRIMINATOR_KEY, mapper.discriminator);
        }

    }

    /**
     * Super entities.
     *
     * @return Super entities.
     */
    @Nonnull
    public List<SuperEntity> getSuperEntities() {
        assert mapperRegistry != null;
        assert initialized;

        return Collections.unmodifiableList(superEntities);
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * is mapper initialized?
     *
     * @return is mapper initialized?
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Entity type.
     *
     * @return Entity type.
     */
    @Nullable
    public final Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Discriminator value.
     *
     * @return Discriminator value, trimmed, possibly "".
     */
    @Nonnull
    public final String getDiscriminator() {
        return discriminator;
    }

    /**
     * Returns the registered fields of this mapper.
     *
     * @return Fields.
     */
    @Nonnull
    public List<Field<?>> getFields() {
        return fields;
    }

    /**
     * Returns the registered fields of this mapper including fields in super entities.
     *
     * @return Fields.
     */
    @Nonnull
    public List<Field<?>> getFieldsIncludingSuperEntities() {
        final List<Field<?>> fields = new ArrayList<>(this.fields);

        for (final SuperEntity superEntity : superEntities) {
            @SuppressWarnings("unchecked")
            final List<Field<?>> superFields =
                    ((EntityMapper) superEntity.getSuperMapper()).getFieldsIncludingSuperEntities();
            fields.addAll(superFields);
        }

        return fields;
    }


    /**
     * Finds the most specific sub mapper based on the given entity type.
     *
     * @param <T2>       Subtype.
     * @param entityType Entity type to find mapper for.
     * @return Most specific sub-mapper.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public <T2 extends T> EntityMapper<T2> getSubMapper(
            @Nonnull final Class<?> entityType) {
        assert mapperRegistry != null;
        assert initialized;

        assert entityType != null;
        final Tuple<Class<?>, Class<?>> key = new Tuple<>(getClass(), entityType);
        EntityMapper<T2> subMapper = (EntityMapper<T2>) mapperRegistry.getSubMapperCache().get(key);

        // No sub-mapper cached, calculate.
        // This might happen multiple times, this is fine, the result
        // will always be the same.
        if (subMapper == null) {

            subMapper = findSubMapper(entityType);

            // If no specific subMapper is not found, use this.
            if (subMapper == null) {
                subMapper = (EntityMapper<T2>) this;
            }
            mapperRegistry.getSubMapperCache().put(key, subMapper);
        }

        return subMapper;
    }

    /**
     * Create a new entity type declaration.
     *
     * @param entityType Entity type.
     * @return Entity type instance.
     */
    @Nonnull
    protected EntityType entityType(@Nonnull final Class<T> entityType) {
        assert entityType != null;
        return new EntityType(entityType, "");
    }

    /**
     * Create a new entity type declaration.
     *
     * @param entityType    Entity type.
     * @param discriminator Discriminator value, trimmed, possibly "".
     * @return EntityType instance.
     */
    @Nonnull
    protected EntityType entityType(@Nonnull final Class<T> entityType, @Nonnull final String discriminator) {
        assert entityType != null;
        assert discriminator != null;
        return new EntityType(entityType, discriminator);
    }

    /**
     * Creates a new super entity.
     *
     * @param entityMapperType Type of the super entity.
     * @return Super entity.
     */
    @Nonnull
    protected SuperEntity superEntity(@Nonnull final Class<? extends EntityMapper<? super T>> entityMapperType) {
        assert entityMapperType != null;
        return new SuperEntity(entityMapperType);
    }

    /**
     * Creates a version.
     *
     * @param currentVersion Current version of mapper. Must be &gt;= 0.
     * @return Current version.
     */
    @Nonnull
    protected CurrentVersion currentVersion(final int currentVersion) {
        assert currentVersion >= 0;
        return new CurrentVersion(currentVersion);
    }

    /**
     * Creates a new reflective field.
     *
     * @param <U>                Type.
     * @param fieldName          Name of the field.
     * @param mapper             Mapper for the field.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Version constraints, min, max.
     * @return Id field instance.
     */
    @Nonnull
    protected <U> Field<U> field(
            @Nonnull final String fieldName,
            @Nonnull final Mapper<U> mapper,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert mapper != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return new ReflectiveField<>(fieldName, mapper, getter, setter, versionConstraints);
    }

    /**
     * /** Creates a new reflective field.
     *
     * @param <U>                Type.
     * @param fieldName          Name of the field.
     * @param mapperType         Type of the field mapper.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Version constraints, min, max.
     * @return Id field instance.
     */
    @Nonnull
    protected <U> Field<U> field(
            @Nonnull final String fieldName,
            @Nonnull final Class<? extends Mapper<U>> mapperType,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert mapperType != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return new ReflectiveField<>(fieldName, mapperType, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective Id field.
     *
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Version constraints, min, max.
     * @return Id field instance.
     */
    @Nonnull
    protected Field<Uid<T>> idField(
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(MongoDBKeyNames.ID_KEY, new ReferenceMapper<>(), getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective Id field.
     *
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Version constraints, min, max.
     * @return Id field instance.
     */
    @Nonnull
    protected Field<String> stringIdField(
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(MongoDBKeyNames.ID_KEY, StringMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective string field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<String> stringField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, StringMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective integer field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Integer> integerField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, IntegerMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective long field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Long> longField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, LongMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective double field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Double> doubleField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, DoubleMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective boolean field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Boolean> booleanField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, BooleanMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective currency field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Currency> currencyField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, CurrencyMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective date-time field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<DateTime> dateTimeField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, DateTimeMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective local date field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<LocalDate> localDateField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, LocalDateMapper.class, getter, setter, versionConstraints);
    }

    @Nonnull
    protected Field<URL> urlField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, URLMapper.class, getter, setter, versionConstraints);
    }

    @Nonnull
    protected Field<Locale> localeField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, LocaleMapper.class, getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective reference field.
     *
     * @param <U>                Type.
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected <U> Field<Uid<U>> referenceField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, new ReferenceMapper<>(), getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective collection field.
     *
     * @param <U>                Type.
     * @param fieldName          Name of the field.
     * @param mapper             Mapper for the field.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Version constraints, min, max.
     * @return Collection field instance.
     */
    @Nonnull
    protected <U> Field<Collection<U>> collectionField(
            @Nonnull final String fieldName,
            @Nonnull final Mapper<U> mapper,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert mapper != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, CollectionMapper.create(mapper), getter, setter, versionConstraints);
    }

    @Nonnull
    protected <U> Field<List<U>> listField(
            @Nonnull final String fieldName,
            @Nonnull final Mapper<U> mapper,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert mapper != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, ListMapper.create(mapper), getter, setter, versionConstraints);
    }

    /**
     * /** Creates a new reflective field.
     *
     * @param <U>                Type.
     * @param fieldName          Name of the field.
     * @param mapperType         Type of the field mapper.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Version constraints, min, max.
     * @return Collection field instance.
     */
    @Nonnull
    protected <U> Field<Collection<U>> collectionField(
            @Nonnull final String fieldName,
            @Nonnull final Class<? extends Mapper<U>> mapperType,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert mapperType != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, CollectionMapper.create(mapperType), getter, setter, versionConstraints);
    }

    @Nonnull
    protected <U> Field<List<U>> listField(
            @Nonnull final String fieldName,
            @Nonnull final Class<? extends Mapper<U>> mapperType,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert mapperType != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, ListMapper.create(mapperType), getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective string field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Collection<String>> stringCollectionField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, CollectionMapper.create(StringMapper.class), getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective double field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Collection<Double>> doubleCollectionField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, CollectionMapper.create(DoubleMapper.class), getter, setter, versionConstraints);
    }

    /**
     * Creates a new reflective date-time field.
     *
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected Field<Collection<DateTime>> dateTimeCollectionField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, CollectionMapper.create(DateTimeMapper.class), getter, setter, versionConstraints);
    }


    /**
     * Creates a new reflective reference field.
     *
     * @param <U>                Type.
     * @param fieldName          Field name.
     * @param getter             Name of the get method, or NO_GETTER.
     * @param setter             Name of the set method, or NO_SETTER or CONSTRUCTOR.
     * @param versionConstraints Empty, minVersion of minVersion, maxVersion pair.
     * @return String field instance.
     */
    @Nonnull
    protected <U> Field<Collection<Uid<U>>> referenceCollectionField(
            @Nonnull final String fieldName,
            @Nonnull final String getter,
            @Nonnull final String setter,
            @Nonnull final int... versionConstraints) {
        assert fieldName != null;
        assert getter != null;
        assert setter != null;
        assert versionConstraints != null;
        return field(fieldName, CollectionMapper.create(new ReferenceMapper<>()), getter, setter,
                versionConstraints);
    }

    /**
     * Pre-initializes the mapper. Only used by {@link MapperRegistry}. Note that initialize should only be called
     * once.
     *
     * @param mapperRegistry MapperRegistry this mapper is a part of.
     * @throws SchemaException Thrown when a mapperRegistry error is encountered.
     */
    void preInitialize(@Nonnull final MapperRegistry mapperRegistry) throws SchemaException {
        assert this.mapperRegistry == null;
        assert !initialized;
        assert mapperRegistry != null;

        // There must be exactly one EntityType.
        if (entityTypes.isEmpty()) {
            throw new SchemaException(this, "No entity type declared.");
        }
        if (entityTypes.size() > 1) {
            throw new SchemaException(this, "There should only be one declared entity type.");
        }
        this.mapperRegistry = mapperRegistry;
        this.entityType = entityTypes.get(0).getEntityType();
        this.discriminator = entityTypes.get(0).getDiscriminator();
    }

    /**
     * Initializes the mapper, and performs basic validation checks. Only used by {@link MapperRegistry}. Note that
     * initialize should only be called once.
     *
     * @throws SchemaException Thrown when a mapperRegistry error is encountered.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void initialize(@Nonnull final MapperRegistry registry) throws SchemaException {
        assert registry != null;

        // The mapper may already be initialized.
        if (initialized) {
            return;
        }
        assert !initialized;
        initialized = true;

        // There may only be one current version at most.
        if (currentVersions.size() > 1) {
            throw new SchemaException(this, "At most one current version may be specified.");
        }

        // Check 0 <= CurrentVersion < MAX_INT.
        for (final CurrentVersion currentVersion : currentVersions) {
            if (currentVersion.version < 0) {
                throw new SchemaException(
                        "MapperRegistry version should be > 0 but was: " + currentVersion.version + '.');
            }
            if (currentVersion.version >= Integer.MAX_VALUE) {
                throw new SchemaException(
                        "MapperRegistry version should be < MAX_INT but was: " + currentVersion.version + '.');
            }
        }

        // Initialize super entities.
        for (final SuperEntity superEntity : this.superEntities) {
            superEntity.initialize(registry);
            superEntity.getSuperMapper().initialize(registry);
        }

        // Determine actual version.
        // Version constraints:
        // 1. When there is a field / super-entity etc with a version constraint, there should be a current version.
        // 2. May not have a current version when a super-entity has a current version.
        // 3. All super-entity current versions must have the same version.
        int maxVersion = 0;
        for (int version = 0; version <= maxVersion; version++) {

            // Should there be a current version?
            boolean needCurentVersion = false;
            for (final Field<?> field : this.fields) {
                if ((field.getMinVersion() > 0) || (field.getMaxVersion() < Integer.MAX_VALUE)) {
                    needCurentVersion = true;
                }
            }
            for (final SuperEntity superEntity : this.superEntities) {
                if ((superEntity.getMinVersion() > 0) || (superEntity.getMaxVersion() < Integer.MAX_VALUE)) {
                    needCurentVersion = true;
                }
            }

            // Collect all current versions from super entities.
            final List<EntityMapper<?>.CurrentVersion> superVersions = new ArrayList<>();
            maxVersion = Math.max(maxVersion, findSuperVersions(superVersions, version));

            // Check 1: current version required?
            if (needCurentVersion && superVersions.isEmpty() && currentVersions.isEmpty()) {
                throw new SchemaException(this, "Current version required.");
            }

            // Check 2: may not specify a current version if there is one in super entity.
            if (!currentVersions.isEmpty() && !superVersions.isEmpty()) {
                throw new SchemaException(this, "No current versions allowed, " +
                        "since a current version is already specified in super entity: " + superVersions.get(0).getParent().
                        getClass().getSimpleName());
            }

            // Check 3: all super entities should have same version.
            for (int i = 1; i < superVersions.size(); i++) {
                if (superVersions.get(i - 1).version != superVersions.get(i).version) {
                    throw new SchemaException("All super entities should have the same current versions. " +
                            superVersions.get(i - 1).getParent().getClass().getSimpleName() + " has version " +
                            superVersions.get(i - 1).version + " while " + superVersions.get(i).getParent().
                            getClass().getSimpleName() + " has version " + superVersions.get(i).version);
                }

            }
        }
        this.currentVersion = maxVersion;

        // Check super entities.
        for (final SuperEntity superEntity : this.superEntities) {

            // Check version constraints.
            if (superEntity.getMinVersion() > currentVersion) {
                throw new SchemaException(EntityMapper.this,
                        "Super entity " + superEntity.superMapperType.getSimpleName() + " has a min version of " +
                                superEntity.getMinVersion() + "; it should be smaller than or equal " +
                                "to the current version (" +
                                currentVersion + ')');
            }
            if ((superEntity.getMaxVersion() < Integer.MAX_VALUE) && (superEntity.getMaxVersion() >= currentVersion)) {
                throw new SchemaException(EntityMapper.this,
                        "Super entity " + superEntity.superMapperType.getSimpleName() + " has a max version of " +
                                superEntity.getMaxVersion() + "; it should be " +
                                "smaller than the current version (" + currentVersion + ')');
            }
        }

        // Initialize fields.
        for (final Field<?> field : this.fields) {
            // Check version constraints.
            if (field.getMinVersion() > currentVersion) {
                throw new SchemaException(this, "Field " + field + " has a min version of " + field.getMinVersion() +
                        "; it should be smaller than or equal to the current version (" +
                        currentVersion + ')');
            }
            if ((field.getMaxVersion() < Integer.MAX_VALUE) && (field.getMaxVersion() >= currentVersion)) {
                throw new SchemaException(this, "Field " + field + " has a max version of " + field.getMaxVersion() +
                        "; it should be smaller than the current version (" + currentVersion + ')');
            }
            field.initialize(registry);
        }

        // Gather field extent (super entity fields + own fields).
        findAllFields(fieldExtent);

        // Gather constructor field extent (from super entity fields + own fields).
        findConstructorFields(constructorFieldExtent);

        // If the entityType is not abstract, find a constructor with names matching fields.
        if (!Modifier.isAbstract(entityType.getModifiers())) {
            for (final Constructor<?> ctor : entityType.getConstructors()) {
                if (isCompatibleConstructor(ctor, constructorFieldExtent)) {
                    this.constructor = ctor;
                    break;
                }
            }
            if (!constructorFieldExtent.isEmpty() && (constructor == null)) {
                throw new SchemaException(this, "No valid constructor found for mapper with constructor fields. " +
                        "constructorFieldExtent=" + constructorFieldExtent + ". Please add a public constructor with " +
                        "these fields or make the entity abstract.");
            }
        }
    }

    /**
     * Override this method to instantiate an entity. When this mapper represents an abstract entity, null can be
     * returned. The default implementation instantiates a new object reflectively.
     *
     * @return The new entity.
     * @throws MapperException Thrown when the instance could not be created.
     */
    @Nonnull
    protected T newInstance() throws MapperException {
        try {
            assert entityType != null;
            return entityType.getConstructor().newInstance();
        } catch (final Exception e) {
            LOG.error("newInstance: cannot create instance, entitype=" + entityType, e);
            throw new MapperException(this, "Instance could not be created. No default constructor?", e);
        }
    }

    /**
     * Override this method to construct an object that needs object values for construction. Useful for immutable
     * objects, like {@link com.tomtom.speedtools.geometry.GeoPoint}.
     *
     * When not overridden, one must override {@link #getEntityType()}.
     *
     * @param dbObject          Database object that can be used to instantiate object.
     * @param initializedFields Will be filled with fields that have been set using the constructor.
     * @return The new entity.
     * @throws MapperException When an instance could not be created.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    protected T newInstance(
            @Nonnull final DBObject dbObject,
            @Nonnull final Set<EntityMapper<?>.Field<?>> initializedFields) throws MapperException {
        assert dbObject != null;
        assert initializedFields != null;

        if (constructor != null) {
            assert constructor.getParameterTypes().length == constructorFieldExtent.size();
            final Object[] args = new Object[constructorFieldExtent.size()];

            final List<MapperError> mapperErrors = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                try {
                    args[i] = constructorFieldExtent.get(i).fromDbObject(dbObject);
                } catch (final MapperException e) {
                    mapperErrors.addAll(e.getMapperErrors());
                }
            }

            if (!mapperErrors.isEmpty()) {
                throw new MapperException(mapperErrors);
            }

            try {
                assert constructor != null;
                final T result = (T) constructor.newInstance(args);

                // These fields have now been initialized.
                initializedFields.addAll(constructorFieldExtent);

                return result;
            } catch (final Exception e) {
                LOG.error("newInstance(dbObject): cannot create instance, entitype=" + entityType, e);
                throw new MapperException(this, "Could not instantiate entity: " + e.getMessage(), e);
            }
        } else {
            return newInstance();
        }
    }

    private boolean isCompatibleConstructor(@Nonnull final Constructor<?> ctor, @Nonnull final List<Field<?>> fields) {
        assert ctor != null;
        assert fields != null;
        if (ctor.getParameterTypes().length == fields.size()) {
            for (int i = 0; i < fields.size(); i++) {
                final Class<?> parameterType = ctor.getParameterTypes()[i];
                final Field<?> field = fields.get(i);
                if (!isCompatibleConstructorParameter(parameterType, field)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isCompatibleConstructorParameter(
            @Nonnull final Class<?> parameterType,
            @Nonnull final Field<?> field) {
        assert parameterType != null;
        assert field != null;
        final Method getMethod;
        if (field instanceof ReflectiveField) {
            @SuppressWarnings("unchecked")
            final EntityMapper<?>.ReflectiveField<?> reflectiveField = (ReflectiveField<?>) field;
            getMethod = reflectiveField.getMethod;
        } else {
            try {
                final Class<T> parentEntityType = field.getParent().entityType;
                assert parentEntityType != null;
                getMethod = parentEntityType.getMethod(
                        "get" + Character.toUpperCase(field.getFieldName().charAt(0)) + field.getFieldName().substring(1));
            } catch (final NoSuchMethodException ignored) {
                return false;
            }
        }
        assert getMethod != null;
        return unbox(parameterType).isAssignableFrom(unbox(getMethod.getReturnType()));
    }

    private static Class<?> unbox(@Nonnull final Class<?> type) {
        assert type != null;
        if (type.isPrimitive()) {
            if (type.equals(Integer.TYPE)) {
                return Integer.class;
            }
            if (type.equals(Long.TYPE)) {
                return Long.class;
            }
            if (type.equals(Short.TYPE)) {
                return Short.class;
            }
            if (type.equals(Boolean.TYPE)) {
                return Boolean.class;
            }
            if (type.equals(Float.TYPE)) {
                return Float.class;
            }
            if (type.equals(Double.TYPE)) {
                return Double.class;
            }
            if (type.equals(Character.TYPE)) {
                return Character.class;
            }
            if (type.equals(Byte.TYPE)) {
                return Byte.class;
            }
            if (type.equals(Void.TYPE)) {
                return Void.class;
            }
        }
        return type;
    }

    private void findAllFields(@Nonnull final Map<String, Field<?>> fields) {
        assert fields != null;
        assert fields.isEmpty();
        for (final SuperEntity superEntity : superEntities) {
            @SuppressWarnings("unchecked") final
            EntityMapper<T> superMapper = (EntityMapper<T>) superEntity.getSuperMapper();
            superMapper.findAllFields(fields);
        }
        for (final Field<?> field : this.fields) {
            fields.put(field.getFieldName(), field);
        }
    }

    private void findConstructorFields(@Nonnull final List<Field<?>> fields) {
        assert fields != null;
        assert fields.isEmpty();
        for (final SuperEntity superEntity : superEntities) {
            @SuppressWarnings("unchecked") final
            EntityMapper<T> superMapper = (EntityMapper<T>) superEntity.getSuperMapper();
            superMapper.findConstructorFields(fields);
        }
        for (final Field<?> field : this.fields) {
            if (field.isConstructorField()) {
                fields.add(field);
            }
        }
    }


    private int findSuperVersions(
            @Nonnull final List<EntityMapper<?>.CurrentVersion> currentVersions,
            final int version) {
        assert currentVersions != null;
        int maxVersion = version;
        currentVersions.addAll(this.currentVersions);
        for (final Field<?> field : this.fields) {
            maxVersion = Math.max(maxVersion, field.getMinVersion());
            if (field.getMaxVersion() < Integer.MAX_VALUE) {
                maxVersion = Math.max(maxVersion, field.getMaxVersion());
            }
        }
        for (final SuperEntity superEntity : this.superEntities) {
            maxVersion = Math.max(maxVersion, superEntity.getMinVersion());
            if (superEntity.getMaxVersion() < Integer.MAX_VALUE) {
                maxVersion = Math.max(maxVersion, superEntity.getMaxVersion());
            }
            if (superEntity.isCompatible(version)) {
                final EntityMapper<? super T> superMapper = superEntity.getSuperMapper();
                maxVersion = Math.max(maxVersion, superMapper.findSuperVersions(currentVersions, version));
            }
        }
        return maxVersion;
    }

    /**
     * Gets the (sub-class) mapper for given database object, using the stored discriminator value.
     *
     * @param dbObject Database object to find mapper for.
     * @param version  Version of the database object to find sub-mapper for.
     * @param errors   Container for errors that might occur.
     * @return The mapper, possibly this.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    private <T2 extends T> EntityMapper<T2> getSubMapper(
            @Nonnull final DBObject dbObject,
            final int version,
            @Nonnull final List<MapperError> errors) {
        assert dbObject != null;
        assert errors != null;

        // Does the object contain a discriminator value?
        final Object discriminator = dbObject.get(MongoDBKeyNames.DISCRIMINATOR_KEY);
        if (discriminator != null) {

            // Discriminator must be of string type.
            if (discriminator instanceof String) {

                final EntityMapper<T2> subMapper = findSubMapper((String) discriminator, version);
                if (subMapper != null) {
                    return subMapper;
                }
            }

            // When discriminator is not of string type...
            else {

                // ... mark the error,
                errors.add(new MapperError(this, MongoDBKeyNames.DISCRIMINATOR_KEY,
                        "Discriminator value should be of string " +
                                "type, but was: " + discriminator.getClass().getName()));
            }
        }

        // When object does not contain discriminator value, use this mapper.
        return (EntityMapper<T2>) this;
    }

    /**
     * Recursive method that find an entity mapper based on a discriminator value.
     *
     * @param discriminator Discriminator value.
     * @param version       Version of the database object to find sub mapper for.
     * @return Mapper, possibly this.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private <T2 extends T> EntityMapper<T2> findSubMapper(@Nonnull final String discriminator, final int version) {
        assert discriminator != null;
        if (this.discriminator.equals(discriminator)) {
            return (EntityMapper<T2>) this;
        }

        assert mapperRegistry != null;
        for (final EntityMapper<?>.SuperEntity sub : mapperRegistry.getSubEntities(this)) {
            final EntityMapper<T2> subMapper = (EntityMapper<T2>) sub.getParent();
            if (subMapper.discriminator.equals(discriminator) && sub.isCompatible(version)) {

                // Found it!
                return subMapper;
            } else {

                // Not found yet..., recurse.
                final EntityMapper<T2> foundMapper = subMapper.findSubMapper(discriminator, version);
                if (foundMapper != null) {
                    return foundMapper;
                }
            }
        }

        // No sub-mapper found with given discriminator.
        return null;
    }

    /**
     * Find the most specific sub mapper based on the given entity type. Recurse part of {@link #getSubMapper
     * (Class<?>)}.
     *
     * @param entityType Entity type to find mapper for.
     * @return Most specific sub-mapper.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private <T2 extends T> EntityMapper<T2> findSubMapper(@Nonnull final Class<?> entityType) {
        assert entityType != null;
        // Is there a more specific mapper?
        assert mapperRegistry != null;
        for (final EntityMapper<?>.SuperEntity sub : mapperRegistry.getSubEntities(this)) {
            final EntityMapper<T> parent = (EntityMapper<T>) sub.getParent();
            final EntityMapper<T2> subMapper = parent.findSubMapper(entityType);
            if (subMapper != null) {
                return subMapper;
            }
        }

        // Does this mapper comply?
        assert this.entityType != null;
        if (this.entityType.isAssignableFrom(entityType)) {
            return (EntityMapper<T2>) this;
        }
        return null;
    }

    /**
     * Loops over all registered superEntity mappings, and sets entity properties accordingly.
     *
     * @param entity   Entity to merge into.
     * @param dbObject Data object to read data from.
     * @param version  Version of database object.
     * @param errors   Container for errors.
     */
    private void getAll(
            @Nonnull final T entity,
            @Nonnull final DBObject dbObject,
            final int version,
            @Nonnull final List<MapperError> errors) {
        assert entity != null;
        assert dbObject != null;
        assert errors != null;

        // Loop over super entities.
        for (final SuperEntity superEntity : superEntities) {

            // Valid version?
            if (superEntity.isCompatible(version)) {

                // Delegate to super entity mapper.
                final EntityMapper<? super T> superMapper = superEntity.getSuperMapper();
                superMapper.getAll(entity, dbObject, version, errors);
            }
        }

        // Loop over fields.
        for (final Field<?> field : fields) {

            // Is the superEntity compatible with database object version?
            if (field.isCompatible(version)) {

                // If so, set the entity superEntity according to this superEntity mapper.
                field.entityToDb(entity, dbObject, errors);
            }
        }
    }

    /**
     * Loops over all registered superEntity mappings, and sets entity properties accordingly.
     *
     * @param entity              Entity to merge into.
     * @param dbObject            Data object to read data from.
     * @param initializedFields   Fields that should not be set because they have already been initialized. The fields
     *                            that are actually set by this method will be added to it.
     * @param version             Version of database object.
     * @param errors              Container for errors.
     * @param checkExistingFields Whether this recursion should check all fields of the dbObject are mapped to fields in
     *                            the field extend.
     * @return Resulting entity.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    private T setAll(
            @Nonnull final T entity,
            @Nonnull final DBObject dbObject,
            @Nonnull final Set<EntityMapper<?>.Field<?>> initializedFields,
            final int version,
            @Nonnull final List<MapperError> errors,
            final boolean checkExistingFields) {
        assert entity != null;
        assert dbObject != null;
        assert initializedFields != null;
        assert errors != null;

        // Result can be built incrementally.
        T result = entity;

        // Loop over super entities.
        for (final SuperEntity superEntity : superEntities) {

            // Valid version?
            if (superEntity.isCompatible(version)) {

                // Delegate to super entity.
                final EntityMapper<? super T> superMapper = superEntity.getSuperMapper();
                result = (T) superMapper.setAll(result, dbObject, initializedFields, version, errors, false);
            }
        }

        // Check that all keys are mapped to fields.
        if (checkExistingFields) {
            for (final String keyName : dbObject.keySet()) {
                if (!predefinedFieldNames.contains(keyName)) {
                    if (!fieldExtent.containsKey(keyName)) {
                        errors.add(new MapperError(this, keyName, "Database field was not mapped."));
                    }
                }
            }
        }

        // Loop over fields that are not excluded.
        final List<Field<?>> remainingFields = new ArrayList<>(fields);
        //noinspection SuspiciousMethodCalls
        remainingFields.removeAll(initializedFields);
        for (final Field<?> field : remainingFields) {

            // Is the field compatible with database object version?
            if (field.isCompatible(version)) {

                // If so, use the field to set the value according to the field's (sub)mapper.
                result = field.dbToEntity(result, dbObject, errors);

                // Remember that we set it.
                initializedFields.add(field);
            }
        }

        return result;
    }

    /**
     * Gets the version number from the database object, defaults to 0.
     *
     * @param dbObject Database object.
     * @param errors   Container for errors.
     * @return Version of this database object, default 0.
     */
    private int getVersion(@Nonnull final DBObject dbObject, @Nonnull final List<MapperError> errors) {
        assert dbObject != null;
        assert errors != null;
        final Object versionValue = dbObject.get(MongoDBKeyNames.VERSION_KEY);
        // Version should be an integer.
        if ((versionValue != null) &&
                !(versionValue instanceof Integer)) {
            errors.add(new MapperError(this, MongoDBKeyNames.VERSION_KEY, "Version superEntity should be of integer " +
                    "type, but was: " + versionValue.getClass().getName()));
        }

        // Return version if found one.
        if (versionValue instanceof Integer) {
            return (Integer) versionValue;
        }

        // Default to version 0.
        return 0;
    }

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public class EntityType {
        @Nonnull
        private final Class<T> entityType;
        @Nonnull
        private final String discriminator;

        public EntityType(@Nonnull final Class<T> entityType, @Nonnull final String discriminator) {
            assert entityType != null;
            assert discriminator != null;
            this.entityType = entityType;
            this.discriminator = discriminator.trim();
            entityTypes.add(this);
        }

        /**
         * Discriminator value.
         *
         * @return Discriminator value, trimmed, possibly "".
         */
        @Nonnull
        public String getDiscriminator() {
            return discriminator;
        }

        @Nonnull
        public Class<T> getEntityType() {
            return entityType;
        }
    }


    public class CurrentVersion {
        private final int version;

        private CurrentVersion(final int version) {
            assert version >= 0;
            assert false : "Versioning not supported";
            this.version = 0;

            /**
             * Once supported, should add something like:
             *
             * this.version = version;
             * currentVersions.add(this);
             **/
        }

        @Nonnull
        public EntityMapper<T> getParent() {
            return EntityMapper.this;
        }
    }


    abstract static class Versioned {
        private int minVersion;
        private int maxVersion;

        Versioned(final int minVersion, final int maxVersion) {
            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
        }

        public int getMinVersion() {
            return minVersion;
        }

        void setMinVersion(final int minVersion) {
            this.minVersion = minVersion;
        }

        public int getMaxVersion() {
            return maxVersion;
        }

        void setMaxVersion(final int maxVersion) {
            this.maxVersion = maxVersion;
        }

        public boolean isCompatible(final int version) {
            return MathUtils.isBetween(version, minVersion, maxVersion);
        }

        public boolean isOverlapping(@Nonnull final Versioned other) {
            assert other != null;
            return (minVersion <= other.maxVersion) && (maxVersion >= other.minVersion);
        }
    }


    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public class SuperEntity extends Versioned {
        @Nonnull
        private final Class<? extends EntityMapper<? super T>> superMapperType;
        @Nullable
        private EntityMapper<? super T> superMapper;
        private boolean initialized = false;

        public SuperEntity(@Nonnull final Class<? extends EntityMapper<? super T>> superMapperType) {
            this(superMapperType, 0, Integer.MAX_VALUE);
            assert superMapperType != null;
        }

        public SuperEntity(
                @Nonnull final Class<? extends EntityMapper<? super T>> superMapperType,
                final int minVersion,
                final int maxVersion) {
            super(minVersion, maxVersion);
            assert superMapperType != null;
            assert discriminator != null;
            this.superMapperType = superMapperType;
            superEntities.add(this);
        }

        @Nonnull
        public EntityMapper<T> getParent() {
            assert initialized;
            return EntityMapper.this;
        }

        @Nonnull
        public EntityMapper<? super T> getSuperMapper() {
            assert initialized;
            assert superMapper != null;
            return superMapper;
        }

        void initialize(@Nonnull final MapperRegistry registry) throws SchemaException {
            assert registry != null;
            assert !initialized;
            superMapper = registry.getOrRegisterMapper(superMapperType);
            if (superMapper.equals(EntityMapper.this)) {
                throw new SchemaException("Cyclic entity mapper found at " + EntityMapper.this.getClass());
            }
            assert EntityMapper.this.entityType != null;
            final Class<? super T> superEntityType = superMapper.entityType;
            assert superEntityType != null;
            if (!superEntityType.isAssignableFrom(EntityMapper.this.entityType)) {
                throw new SchemaException("Spurious inheritance found at " + EntityMapper.this.getClass());
            }
            initialized = true;
        }
    }

    public interface HasFieldName {

        @Nonnull
        public String getFieldName();
    }

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public abstract class Field<U> extends Mapper<U> implements HasFieldName {

        @Nonnull
        private final String fieldName;
        @Nullable
        private Class<? extends Mapper<U>> mapperType;
        @Nullable
        private Mapper<U> mapper;
        private final int minVersion;
        private final int maxVersion;
        private boolean initialized = false;

        protected Field(
                @Nonnull final String fieldName,
                @Nullable final Class<? extends Mapper<U>> mapperType,
                @Nonnull final int... versionConstraints) {
            this(fieldName, (Mapper<U>) null, versionConstraints);
            assert fieldName != null;
            assert versionConstraints != null;
            this.mapperType = mapperType;
        }

        protected Field(
                @Nonnull final String fieldName,
                @Nullable final Mapper<U> mapper,
                @Nonnull final int... versionConstraints) {
            super();
            assert fieldName != null;
            assert versionConstraints != null;
            assert versionConstraints.length <= 2 : "Only minVersion / maxVersion allowed.";
            this.minVersion = (versionConstraints.length > 0) ? versionConstraints[0] : 0;
            this.maxVersion = (versionConstraints.length > 1) ? versionConstraints[1] : Integer.MAX_VALUE;
            assert fieldName != null;
            assert !fieldName.trim().isEmpty();
            this.fieldName = fieldName.trim();
            this.mapper = mapper;
            fields.add(this);
        }

        @Nonnull
        public EntityMapper<T> getParent() {
            return EntityMapper.this;
        }

        @Override
        @Nonnull
        public String getFieldName() {
            return fieldName;
        }

        @SuppressWarnings("SameReturnValue")
        public boolean isMandatory() {
            return false;
        }

        /**
         * Indicates that this field corresponds to a constructor parameter.
         *
         * @return True is this field is a constructor field.
         */
        public boolean isConstructorField() {
            return false;
        }

        public int getMinVersion() {
            return minVersion;
        }

        public int getMaxVersion() {
            return maxVersion;
        }

        public boolean isCompatible(final int version) {
            return MathUtils.isBetween(version, minVersion, maxVersion);
        }

        public boolean isOverlapping(@Nonnull final Field<?> other) {
            assert other != null;
            return (minVersion <= other.maxVersion) && (maxVersion >= other.minVersion);
        }

        @Nonnull
        public Mapper<U> getMapper() {
            assert initialized;
            assert mapper != null;
            return mapper;
        }

        @Nonnull
        @Override
        public String toString() {
            return fieldName;
        }

        @Nullable
        public U fromDbObject(@Nonnull final DBObject dbObject) throws MapperException {
            assert dbObject != null;
            assert initialized;
            final Object dbValue = dbObject.get(fieldName);
            if (isMandatory() && (dbValue == null)) {
                throw new MapperException(EntityMapper.this, fieldName, "Missing required field: " + fieldName);
            }
            return fromDb(dbValue);
        }

        @Nullable
        @Override
        public U fromDb(@Nullable final Object dbValue) throws MapperException {
            assert initialized;
            assert mapper != null;
            try {
                return mapper.fromDb(dbValue);
            } catch (final MapperException e) {
                e.setSource(EntityMapper.this, fieldName);
                throw e;
            }
        }

        @Nullable
        @Override
        public Object toDb(@Nullable final U value) throws MapperException {
            assert initialized;
            assert mapper != null;
            try {
                return mapper.toDb(value);
            } catch (final MapperException e) {
                e.setSource(EntityMapper.this, fieldName);
                throw e;
            }
        }

        @Nullable
        protected abstract U get(@Nonnull T entity) throws MapperException;

        @Nonnull
        protected abstract T set(@Nonnull T entity, U value) throws MapperException;

        void entityToDb(
                @Nonnull final T entity,
                @Nonnull final DBObject dbObject,
                @Nonnull final List<MapperError> errors) {
            assert entity != null;
            assert dbObject != null;
            assert errors != null;
            assert initialized;
            try {
                final U value = get(entity);
                final Object dbValue = toDb(value);

                // Null-Values are not stored.
                if (dbValue != null) {
                    dbObject.put(fieldName, dbValue);
                }
            } catch (final MapperException e) {
                errors.addAll(e.getMapperErrors());
            } catch (final Exception e) {
                errors.add(new MapperError(EntityMapper.this, fieldName, e.getMessage(), e));
            }
        }

        @Nonnull
        T dbToEntity(
                @Nonnull final T entity,
                @Nonnull final DBObject dbObject,
                @Nonnull final List<MapperError> errors) {
            assert entity != null;
            assert dbObject != null;
            assert errors != null;
            assert initialized;
            try {
                final U value = fromDbObject(dbObject);
                if (value != null) {
                    return set(entity, value);
                }
            } catch (final MapperException e) {
                errors.addAll(e.getMapperErrors());
            } catch (final Exception e) {
                errors.add(new MapperError(EntityMapper.this, fieldName, e.getMessage()));
            }
            return entity;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void initialize(@Nonnull final MapperRegistry registry) throws SchemaException {
            assert registry != null;
            assert !initialized;
            try {
                if (mapper != null) {
                    mapperType = (Class<? extends Mapper<U>>) mapper.getClass();
                    registry.register(mapper);
                } else {
                    assert mapperType != null;
                    mapper = registry.getOrRegisterMapper(mapperType);
                    mapper.initialize(registry);
                }
            } catch (final SchemaException e) {
                throw e.withSource(EntityMapper.this, fieldName);
            }
            initialized = true;
        }
    }

    public class ReflectiveField<U> extends Field<U> {

        @Nonnull
        private final String getter;
        @Nonnull
        private final String setter;
        @Nullable
        private Method getMethod;
        @Nullable
        private Method setMethod;

        public ReflectiveField(
                @Nonnull final String fieldName,
                @Nonnull final Class<? extends Mapper<U>> mapperType,
                @Nonnull final String getter,
                @Nonnull final String setter,
                @Nonnull final int... versionConstraints) {
            super(fieldName, mapperType, versionConstraints);
            assert fieldName != null;
            assert mapperType != null;
            assert getter != null;
            assert !getter.equals(CONSTRUCTOR);
            assert !getter.equals(NO_SETTER);
            assert setter != null;
            assert !setter.equals(NO_GETTER);
            assert versionConstraints != null;
            this.getter = getter;
            this.setter = setter;
            this.getMethod = null;
            this.setMethod = null;
        }

        public ReflectiveField(
                @Nonnull final String fieldName,
                @Nonnull final Mapper<U> mapper,
                @Nonnull final String getter,
                @Nonnull final String setter,
                @Nonnull final int... versionConstraints) {
            super(fieldName, mapper, versionConstraints);
            assert fieldName != null;
            assert mapper != null;
            assert getter != null;
            assert !getter.equals(CONSTRUCTOR);
            assert !getter.equals(NO_SETTER);
            assert setter != null;
            assert !setter.equals(NO_GETTER);
            assert versionConstraints != null;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean isConstructorField() {
            return setter.equals(CONSTRUCTOR);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void initialize(@Nonnull final MapperRegistry registry) throws SchemaException {
            super.initialize(registry);
            assert registry != null;
            Class<U> propertyClass = null;
            if (!getter.equals(NO_GETTER)) {
                assert entityType != null;
                try {
                    getMethod = entityType.getMethod(getter);
                } catch (final NoSuchMethodException ignore) {
                    throw new SchemaException(EntityMapper.this, getFieldName(),
                            "Get method not found: " + entityType.getSimpleName() + '.' + getter + "()");
                }
                propertyClass = (Class<U>) getMethod.getReturnType();
            }
            if (!setter.equals(NO_SETTER) && !setter.equals(CONSTRUCTOR)) {
                assert entityType != null;
                for (final Method m : entityType.getMethods()) {
                    if (m.getName().equals(setter) && (m.getParameterTypes().length == 1) &&
                            ((propertyClass == null) || (m.getParameterTypes()[0].isAssignableFrom(propertyClass)))) {
                        setMethod = m;
                        break;
                    }
                }
                if (setMethod == null) {
                    throw new SchemaException(EntityMapper.this, getFieldName(),
                            "Set method not found: void " + setter + '(' +
                                    ((propertyClass != null) ? propertyClass.getSimpleName() : "?") + " value)"
                    );
                }
            }
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        protected U get(@Nonnull final T entity) throws MapperException {
            assert entity != null;
            if (getMethod == null) {
                return null;
            }
            try {
                return (U) getMethod.invoke(entity);
            } catch (final Exception e) {
                LOG.error("get: cannot invoke method, fieldName=" + getFieldName() + ", getter=" + getter, e);
                throw new MapperException(EntityMapper.this, getFieldName(), "Couldn't invoke get method " + getter, e);
            }
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        @Override
        protected T set(@Nonnull final T entity, @Nonnull final U value) throws MapperException {
            assert entity != null;
            assert value != null;
            if (setMethod == null) {
                return entity;
            }
            try {
                final Object result = setMethod.invoke(entity, value);
                if (result != null) {
                    return (T) result;
                } else {
                    return entity;
                }
            } catch (final Exception e) {
                LOG.error("set: cannot invoke method, fieldName=" + getFieldName() + ", setter=" + setter, e);
                throw new MapperException(EntityMapper.this, getFieldName(), "Couldn't invoke set method " + setter, e);
            }
        }
    }
}
