/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.mongodb.migratedb;

import com.google.common.collect.MapMaker;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.tomtom.speedtools.mongodb.MongoDB;
import com.tomtom.speedtools.mongodb.MongoDBKeyNames;
import com.tomtom.speedtools.objects.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This class defines a MongoDB migration from 1 version to another. By default, its migration path is the identity
 * transformation, which effectively only changes the database version number in the 'migrator' collection.
 */
public class MongoDBMigration {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBMigration.class);
    private static final Object NO_DEFAULT = new Object();
    public static final boolean NO_DUPLICATES = false;

    private final Map<DBObject, Context> contextMap = new MapMaker().weakKeys().makeMap();
    private final List<MongoDBMigrationProblem> problems = new ArrayList<>();
    private final Context rootContext = new Context(null, null, "");
    private boolean dryRun = false;
    private boolean databaseChanged = true;

    private final String fromVersion;
    private final String toVersion;

    private static final Comparator<Command> RANKING_COMPARATOR =
            new Comparator<Command>() {
                @Override
                public int compare(@Nonnull final Command o1, @Nonnull final Command o2) {
                    assert o1 != null;
                    assert o2 != null;

                    return Integer.valueOf(o1.ranking()).compareTo(o2.ranking());
                }
            };

    /**
     * Define a MongoDBMigration.
     *
     * @param fromVersion 'From'-version (will be trimmed).
     * @param toVersion   'To'-version (will be trimmed).
     */
    public MongoDBMigration(@Nonnull final String fromVersion, @Nonnull final String toVersion) {
        assert fromVersion != null;
        assert toVersion != null;
        this.fromVersion = fromVersion.trim();
        this.toVersion = toVersion.trim();
    }

    @Nonnull
    protected List<MongoDBMigrationProblem> flush() {
        rootContext.flush();
        return problems;
    }

    /**
     * Used to modify top-level documents. Documents will be stored in the collection when modified.
     *
     * @param db             Database.
     * @param collectionName Collection to iterate over.
     * @return Iterable to loop over all documents.
     */
    @Nonnull
    protected Iterable<DBObject> migrateCollection(@Nonnull final MongoDB db, @Nonnull final String collectionName) {
        assert db != null;
        assert collectionName != null;

        rootContext.flush();
        final DBCollection collection = db.getCollection(collectionName);

        final long count = collection.count();
        if (count > Integer.MAX_VALUE) {
            addProblem("", "Collection has too many records (" + count + ", where " + Integer.MAX_VALUE + " is max)");
        }

        /**
         * This set is going to to contain all records for sure, so make sure it is large enough not to get
         * re-allocated all the time.
         *
         * See HashMap's class description at [http://docs.oracle.com/javase/6/docs/api/java/util/HashMap.html],
         * specifically "The expected number of entries in the map and its load factor should be taken into account
         * when setting its initial capacity, so as to minimize the number of rehash operations. If the initial
         * capacity is greater than the maximum number of entries divided by the load factor, no rehash operations
         * will ever occur.".
         */
        @SuppressWarnings("NumericCastThatLosesPrecision")
        final Set<Object> recordIds = new HashSet<>((int) ((double) count / 0.75) + 1);

        return new IterableDelegate<DBObject, DBObject>(collection.find()) {

            private int index = 1;

            @Nullable
            @Override
            public DBObject next(@Nonnull final DBObject value) {

                final Context context = rootContext.createChild(value, collectionName + ':' + index);
                index++;

                // Each document should have an _id field.
                final Object id = value.get("_id");
                if (id == null) {
                    addProblem(context.path, "Document has no _id field: " + value);
                    return null;
                }

                // Don't process records we have already processed. This can happen if a record
                // is modified.
                if (recordIds.contains(id)) {
                    return null;
                }
                recordIds.add(id);

                // Keep original value in immutable string, referenced from 'flush()'.
                final String originalStringValue = value.toString();

                // Save object.
                context.add(new Command() {

                    @Override
                    public void flush() {

                        // If the new value differs from the old one, store it and print it.
                        final String stringValue = value.toString();
                        if (!originalStringValue.equals(stringValue)) {
                            if (!dryRun) {
                                collection.save(value);
                            }
                            LOG.debug(context.path + " - original document: " + originalStringValue);
                            LOG.debug(context.path + " - migrated document: " + value);
                        }
                    }

                    @Override
                    public int ranking() {
                        return Integer.MAX_VALUE; // Saves should be executed last.
                    }
                });

                return value;
            }
        };
    }

    /**
     * Used to add top-level documents.
     *
     * @param db             Database.
     * @param collectionName Collection to add to.
     * @param values         The values to add to the collection. Individual values must not be null.
     */
    protected void addToCollection(
            @Nonnull final MongoDB db,
            @Nonnull final String collectionName,
            @Nonnull final DBObject... values) {
        assert db != null;
        assert collectionName != null;
        assert values != null;

        rootContext.flush();
        final DBCollection collection = db.getCollection(collectionName);

        int index = 1;
        for (final DBObject value : values) {
            if (value != null) {
                final Context context = rootContext.createChild(value, collectionName + '+' + index);
                index++;

                // Each document should have an _id field.
                final Object id = value.get("_id");
                if (id == null) {
                    addProblem(context.path, "Document has no _id field: " + value);
                    break;
                }

                // Save object.
                context.add(new Command() {

                    @Override
                    public void flush() {
                        if (!dryRun) {
                            collection.save(value);
                        }
                        LOG.debug(context.path + " - added document: " + value);
                    }

                    @Override
                    public int ranking() {
                        return Integer.MAX_VALUE; // Saves should be executed last.
                    }
                });
            } else {
                addProblem(rootContext.path, "Trying to add null document.");
            }
        }
    }

    void setDryRun(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    protected static abstract class Converter<T> {

        @Nullable
        public abstract T convert(@Nonnull Value value);

        @Nullable
        Object defaultValue() {
            return NO_DEFAULT;
        }
    }

    @Nonnull
    protected final Converter<Integer> convertToInt(final boolean optional) {
        return new Converter<Integer>() {

            @Nullable
            @Override
            public Integer convert(@Nonnull final Value value) {
                assert value != null;
                final Object valueValue = toSingle(value.value);
                if (valueValue != null) {
                    if (optional && valueValue.toString().trim().isEmpty()) {
                        return null;
                    }
                    try {
                        //noinspection unchecked
                        return Integer.parseInt(valueValue.toString());
                    } catch (final NumberFormatException ignored) {
                        addProblem(value.path, "could not convert '" + valueValue + "' to an integer. Value " +
                                "discarded.");
                    }
                }
                return null;
            }
        };
    }

    @Nonnull
    protected static Converter<Object> setValueConverter(@Nullable final Object value) {
        //noinspection ParameterNameDiffersFromOverriddenParameter
        return new Converter<Object>() {

            @Nullable
            @Override
            public Object convert(@Nonnull final Value ignored) {
                return value;
            }

            @Nullable
            @Override
            Nullable defaultValue() {
                // Can be any value other than NO_DEFAULT.
                return null;
            }
        };
    }

    @Nonnull
    protected final Converter<String> urlConverter = new Converter<String>() {

        @Nullable
        @Override
        public String convert(@Nonnull final Value value) {
            assert value != null;
            final Object valueValue = toSingle(value.value);
            if (valueValue != null) {
                try {
                    //noinspection unchecked
                    return new URL(valueValue.toString()).toString();
                } catch (final MalformedURLException ignored) {
                    addProblem(value.path, "Value '" + valueValue + "' is not a valid URL. " +
                            "Value discarded.");
                }
            }
            return null;
        }
    };


    protected static <T> Converter<T> setDefaultConverter(@Nullable final T toValue) {
        return replaceValueConverter(null, null, toValue);
    }

    protected static <T> Converter<T> replaceValueConverter(
            @Nullable final T defaultValue,
            @Nullable final T fromValue,
            @Nullable final T toValue) {
        return new Converter<T>() {
            @Nullable
            @Override
            public T convert(@Nonnull final Value value) {
                if (Objects.equal(value.value, fromValue)) {
                    return toValue;
                }
                //noinspection unchecked
                return (T) value.value;
            }

            @Nullable
            @Override
            Object defaultValue() {
                return defaultValue;
            }
        };

    }

    /**
     * Reads all values from the property denoted by {@code fieldPaths}. Returns an {@link Iterable}, which may hold 0
     * or more {@link DBObject}s.
     *
     * @param object     The object to start evaluating the fieldPaths from.
     * @param fieldPaths The path components to the value that should be retrieved. A component must be a single
     *                   property name.
     * @return {@link Iterable} of type {@link DBObject} holding the retrieved value(s).
     */
    @Nonnull
    protected Iterable<DBObject> get(@Nonnull final DBObject object, @Nonnull final String... fieldPaths) {
        assert object != null;
        assert fieldPaths != null;

        final Context parentContext = contextMap.get(object);
        final Context context = parentContext.createChild(null, parentContext.path);

        final List<Value> values = getValues(new ArrayList<>(), object, parentContext.path, NO_DEFAULT, true,
                fieldPaths);
        return new IterableDelegate<Value, DBObject>(values) {
            private int index = 0;

            /**
             * Returns the {@link DBObject} at this position in the iterator.
             *
             * @param value The {@link Value} from which to return the {@link DBObject}.
             * @return {@link DBObject} or {@code null} if {@link Value} did not contain one.
             */
            @Override
            @Nullable
            public DBObject next(@Nonnull final Value value) {
                assert value != null;

                index++;
                context.createChild((DBObject) value.value, value.path + ((values.size() > 1) ? (":" + index) : ""));

                // Should not forget to process.
                return (DBObject) value.value;
            }
        };
    }

    /**
     * Reads a single value from property denoted by the {@code fieldPath}. The value in the property must have a class
     * (derived from) {@code resultClass}. If not, a problem will be added, and null will be returned. If more than one
     * value is found, a problem will also be added, and null will be returned. If the path cannot be traversed (because
     * a property is missing), null is returned.
     *
     * @param resultClass The class the value should be assignable from.
     * @param object      The object to start evaluating the fieldPath from.
     * @param fieldPath   The path to the value that should be retrieved.
     * @param <T>         The expected type of the value.
     * @return The value, or null in case of errors.
     */
    @Nullable
    protected <T> T getSingleValue(
            @Nonnull final Class<T> resultClass,
            @Nonnull final DBObject object,
            @Nonnull final String fieldPath) {
        assert resultClass != null;
        assert object != null;
        assert fieldPath != null;

        final List<T> result = new ArrayList<>();
        final Iterable<T> values = getValues(resultClass, object, fieldPath);
        for (final T value : values) {
            if (value != null) {
                result.add(value);
            }
        }

        if (result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            addProblem(fieldPath, "Multiple values found where a single value is expected");
            return null;
        }
    }

    /**
     * Reads values from one or more properties reachable from {@code object} by traversing the given {@code
     * fieldPaths}. These values should be assignable from the class {@code resultClass}. If a value is not, the value
     * is skipped, and a problem is added. If the path cannot be traversed (because a property is not available) it will
     * be skipped.
     *
     * @param resultClass The class the values should be assignable from.
     * @param object      The object to start evaluating the fieldPaths from.
     * @param fieldPaths  The paths to the values that should be retrieved.
     * @param <T>         The expected type of the value.
     * @return An iterable with values of type {@code T}.
     */
    @Nonnull
    protected <T> Iterable<T> getValues(@Nonnull final Class<T> resultClass, @Nonnull final DBObject object,
                                        @Nonnull final String... fieldPaths
    ) {
        assert object != null;
        assert fieldPaths != null;
        assert resultClass != null;
        assert !DBObject.class.isAssignableFrom(resultClass) : "Use get(DBObject, String...) for DBObject results";

        final Context parentContext = contextMap.get(object);

        final List<Value> values = getValues(new ArrayList<>(), object, parentContext.path, NO_DEFAULT, false,
                fieldPaths);
        return new IterableDelegate<Value, T>(values) {

            @Override
            @Nullable
            public T next(@Nonnull final Value value) {
                assert value != null;

                // Check the type of the value.
                if ((value.value != null) && !resultClass.isAssignableFrom(value.value.getClass())) {
                    addProblem(value.path, "Expected type " + resultClass.getSimpleName());
                    return null;
                }

                // We either have null, or the right type.
                assert (value.value == null) || resultClass.isAssignableFrom(value.value.getClass());

                //noinspection unchecked
                return (T) value.value;
            }
        };
    }

    protected Iterable<Replaceable<DBObject, DBObject>> replace(@Nonnull final DBObject object,
                                                                @Nonnull final String... fieldPaths) {
        return replace(object, DBObject.class, DBObject.class, fieldPaths);
    }

    protected <T, U> Iterable<Replaceable<T, U>> replace(@Nonnull final DBObject object,
                                                         @Nonnull final Class<T> fieldType, @Nonnull final Class<U> newFieldType, @Nonnull final String... fieldPaths) {
        assert object != null;
        assert fieldType != null;
        assert newFieldType != null;
        assert fieldPaths != null;

        final Context parentContext = contextMap.get(object);
        final Context context = parentContext.createChild(null, parentContext.path);
        final List<Value> values = getValues(new ArrayList<>(), object, parentContext.path, NO_DEFAULT, false,
                fieldPaths);

        return new IterableDelegate<Value, Replaceable<T, U>>(values) {
            @Override
            public Replaceable<T, U> next(@Nonnull final Value value) {

                // Check source type.
                if (!fieldType.isInstance(value.value)) {
                    addProblem(value.path, "expected existing value of type " + fieldType.getSimpleName() +
                            ", but got: " + value.value);
                    return null;
                }
                context.createChild((DBObject) value.value, value.path);

                // Make sure empty elements are removed from the list.
                if (value.isSingleValue) {
                    value.parent.removeField(value.fieldName);
                } else {
                    context.add(new PruneList(value));
                }

                // Because of getValues(..., NO_DEFAULT, ...).
                final Object valueValue = value.value;
                assert valueValue != null;

                // Should not forget to process.
                //noinspection unchecked
                return new Replaceable<T, U>(context, (T) valueValue) {
                    @Override
                    public void set(@Nullable final U newValue) {
                        U newVal = newValue;
                        if ("".equals(newVal)) {
                            newVal = null;
                        }
                        if (value.isSingleValue) {
                            if (newVal != null) {
                                value.parent.put(value.fieldName, newVal);
                            } else {
                                value.parent.removeField(value.fieldName);
                            }
                        } else {
                            value.list.set(value.index, newVal);
                        }
                        logValueChanged(value, newVal);
                    }
                };
            }
        };
    }

    private static <U> void logValueChanged(@Nonnull final Value value, @Nullable final U newValue) {
        if (newValue == null) {
            if (value.value != null) {
                LOG.debug(value.path + " - removed value " + printValue(value.value));
            }
        } else {
            if (value.value == null) {
                LOG.debug(value.path + " - added value " + printValue(newValue));
            } else {
                if (!Objects.equal(value.value, newValue)) {
                    LOG.debug(value.path + " - replaced " + printValue(value.value) + " with " + printValue(newValue));
                }
            }
        }
    }

    private static String printValue(@Nullable final Object value) {
        if (value instanceof String) {
            return "'" + value + '\'';
        }
        if (value == null) {
            return "null";
        }
        return value.toString();

    }

    protected void rename(@Nonnull final DBObject object, @Nonnull final String fieldPath,
                          @Nonnull final String newFieldName) {
        assert object != null;
        assert fieldPath != null;
        assert newFieldName != null;
        final Context parentContext = contextMap.get(object);
        for (final Value value : getValues(new ArrayList<>(), object, parentContext.path, null, false,
                fieldPath)) {
            if (value.index == 0) {
                final Object v = value.parent.removeField(value.fieldName);
                if (v != null) {
                    value.parent.put(newFieldName, v);
                    LOG.debug(value.path + " - renamed to " + newFieldName);
                }
            }
        }
    }

    /**
     * Removes the field given by {@code fieldPath} from {@code object}.
     *
     * @param object    The object from which to delete the field.
     * @param fieldPath The path to the field to delete.
     */
    protected void remove(@Nonnull final DBObject object, @Nonnull final String fieldPath) {
        assert object != null;
        assert fieldPath != null;
        assert !fieldPath.isEmpty();
        final Context parentContext = contextMap.get(object);
        for (final Value value : getValues(new ArrayList<>(), object, parentContext.path, null, false,
                fieldPath)) {
            if (value.index == 0) {
                value.parent.removeField(value.fieldName);
                LOG.debug(value.path + " - removed");
            }
        }
    }

    /**
     * Sets the value at {@code fieldPath} to {@code toValue}. If the base path (i.e., the path before the last '.')
     * cannot be resolved, the value is not set. If any of the segments of the path refers to a collection field, all
     * objects contained are traversed, potentially resulting in multiple fields that will be set.
     *
     * @param object    The object to start evaluating the fieldPath from.
     * @param fieldPath The path to the value to set.
     * @param toValue   The value to set.
     */
    protected void setValue(@Nonnull final DBObject object, @Nonnull final String fieldPath,
                            @Nullable final Object toValue) {
        convert(object, fieldPath, false, setValueConverter(toValue));
    }

    /**
     * Replaces the value {@code fromValue} at path {@code fieldPath} with {@code toValue}. If the path cannot be
     * resolved nothing is replaced. If any of the segments of the path refers to a collection field, all objects
     * contained are traversed, potentially resulting in multiple values that will be replaced.
     *
     * @param object    The object to start evaluating the fieldPath from.
     * @param fieldPath The path to the value to replace.
     * @param fromValue The value to replace.
     * @param toValue   The value to replace with.
     */
    protected void replaceValue(@Nonnull final DBObject object, @Nonnull final String fieldPath,
                                @Nullable final Object fromValue, @Nullable final Object toValue) {
        convert(object, fieldPath, false, replaceValueConverter(NO_DEFAULT, fromValue, toValue));
    }

    /**
     * Sets the value {@code toValue} at path {@code fieldPath} when it does not already have a value. If the {@code
     * fieldPath} cannot be resolved, no changes are made. If any of the segments of the path refers to a collection
     * field, all objects contained are traversed, potentially resulting in multiple fields that will be set.
     *
     * @param object    The object to start evaluating the fieldPath from.
     * @param fieldPath The path to the value to set.
     * @param toValue   The value to set.
     */
    protected void setDefault(@Nonnull final DBObject object, @Nonnull final String fieldPath,
                              @Nullable final Object toValue) {
        convert(object, fieldPath, false, setDefaultConverter(toValue));
    }

    /**
     * Convert the values referred to by (object, fieldPath) using the given {@link Converter}.
     *
     * @param object             The object to start evaluating the fieldPath from.
     * @param fieldPath          The path to the values to convert.
     * @param createChildContext If true, the created {@link Value}s will be added to the parent context. The
     *                           consequence of this is that the converter is then able to use MongoDBMigration-methods
     *                           that are dependent on this context (any of the getters that work with Values
     *                           internally, such as {@link #getSingleValue(Class, DBObject, String)}).
     * @param converter          The converter to convert the values with.
     */
    protected void convert(
            @Nonnull final DBObject object,
            @Nonnull final String fieldPath,
            final boolean createChildContext,
            @Nonnull final Converter<?> converter) {
        assert object != null;
        assert fieldPath != null;
        assert converter != null;

        final Context parentContext = contextMap.get(object);
        final List<Value> values = getValues(new ArrayList<>(), object, parentContext.path,
                converter.defaultValue(), false,
                fieldPath);
        for (final Value value : values) {

            if (createChildContext) {
                parentContext.createChild((DBObject) value.value, value.path);
            }

            Object newValue = converter.convert(value);
            if ("".equals(newValue)) {
                newValue = null;
            }
            if (value.isSingleValue) {
                if (newValue != null) {
                    value.parent.put(value.fieldName, newValue);
                } else {
                    value.parent.removeField(value.fieldName);
                }
            } else {
                // If a default value was added in getValues(), the value list can be empty.
                if (value.list.isEmpty()) {
                    value.list.add(newValue);
                } else {
                    value.list.set(value.index, newValue);
                }

                // Always prune the values list.
                parentContext.add(new PruneList(value), NO_DUPLICATES);
            }
            logValueChanged(value, newValue);
        }
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private List<Value> getValues(
            @Nonnull final List<Value> result,
            @Nonnull final DBObject object,
            @Nonnull final String currentPath,
            @Nullable final Object defaultValue,
            final boolean needObjects,
            @Nonnull final String... fieldPaths) {
        assert result != null;
        assert object != null;
        assert currentPath != null;
        assert fieldPaths != null;

        for (final String commaSeparatedFieldPath : fieldPaths) {
            for (final String unTrimmedFieldPath : commaSeparatedFieldPath.split(",")) {
                final String fieldPath = unTrimmedFieldPath.trim();
                final String typedFieldName;
                final String remainingPath;
                final int dotIndex = fieldPath.indexOf('.');
                if (dotIndex >= 0) {
                    typedFieldName = fieldPath.substring(0, dotIndex).trim();
                    remainingPath = fieldPath.substring(dotIndex + 1).trim();
                } else {
                    typedFieldName = fieldPath.trim();
                    remainingPath = null;
                }
                final String fieldName;
                final String discriminator;
                final int colonIndex = typedFieldName.indexOf(':');
                if (colonIndex >= 0) {
                    fieldName = typedFieldName.substring(0, colonIndex).trim();
                    discriminator = typedFieldName.substring(colonIndex + 1).trim();
                } else {
                    fieldName = typedFieldName;
                    discriminator = null;
                }
                final Object value;
                // check top-level discriminator
                if (fieldName.isEmpty() && !discriminator.isEmpty()) {
                    value = object;
                } else {
                    value = object.get(fieldName);
                }
                int index = 0;
                final boolean shouldRecurse = (remainingPath != null);
                final List<Object> list = toList(value);
                for (final Object elt : list) {
                    final String path = currentPath + '.' + fieldName + ((list.size() > 1) ? (":" + (index + 1)) : "");
                    if (elt instanceof DBObject) {
                        final DBObject objval = ((DBObject) elt);
                        if ((discriminator == null) ||
                                discriminator.equals(objval.get(MongoDBKeyNames.DISCRIMINATOR_KEY))) {
                            if (shouldRecurse) {
                                // Recurse.
                                getValues(result, objval, path, defaultValue, needObjects, remainingPath);
                            }
                        }
                    } else {
                        if (shouldRecurse || (discriminator != null) || needObjects) {
                            addProblem(path, "not an object");
                        }
                    }
                    if (!shouldRecurse) {
                        final Value v = new Value(currentPath + '.' + fieldName, object, fieldName, list, index,
                                !(value instanceof List), elt);
                        result.add(v);
                    }
                    index++;
                }
                if ((remainingPath == null) && list.isEmpty() && !NO_DEFAULT.equals(defaultValue)) {
                    result.add(new Value(currentPath + '.' + fieldName, object, fieldName, list, index,
                            !(value instanceof List),
                            defaultValue));
                }
            }
        }
        return result;
    }

    protected static class Value {
        @Nonnull
        public final String path;
        @Nonnull
        public final DBObject parent;
        @Nonnull
        public final String fieldName;
        @Nonnull
        public final List<Object> list;
        public final int index;
        @Nullable
        public final Object value;
        public final boolean isSingleValue;

        private Value(final @Nonnull String path, final @Nonnull DBObject parent, final @Nonnull String fieldName,
                      final @Nonnull List<Object> list, final int index, final boolean isSingleValue,
                      @Nullable final Object value) {
            assert path != null;
            assert parent != null;
            assert fieldName != null;
            assert list != null;
            this.path = path;
            this.parent = parent;
            this.fieldName = fieldName;
            this.list = list;
            this.index = index;
            this.value = value;
            this.isSingleValue = isSingleValue;
        }
    }

    @Nonnull
    private static List<Object> toList(@Nullable final Object value) {
        if (value instanceof List) {
            //noinspection unchecked
            return (List<Object>) value;
        }
        if (value == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(value);
    }

    @Nullable
    protected static Object toSingle(@Nullable final Object value) {
        if (value instanceof List) {
            if (((List) value).isEmpty()) {
                return null;
            }
            return ((List) value).get(0);
        }
        return value;
    }

    @Nonnull
    protected static String getFirstString(@Nullable final Object value) {
        final Object single = toSingle(value);
        if (single != null) {
            return single.toString();
        }
        return "";
    }

    @Nullable
    protected Integer parsePositiveInt(@Nonnull final String value, @Nullable final Integer defaultValue) {
        assert value != null;
        int firstDigit = -1;
        int lastDigit = -1;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                if (firstDigit == -1) {
                    firstDigit = i;
                }
                lastDigit = i;
            } else if (firstDigit != -1) {
                break;
            }
        }
        if (firstDigit == -1) {
            return defaultValue;
        }
        return Integer.parseInt(value.substring(firstDigit, lastDigit + 1).trim());
    }


    @Nonnull
    protected String parsePositiveIntSuffix(@Nonnull final String value) {
        assert value != null;
        boolean digitFound = false;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if ((c != ' ') && (c != '-')) {
                if (Character.isDigit(c)) {
                    digitFound = true;
                } else if (digitFound) {
                    return value.substring(i).trim();
                }
            }
        }
        return "";
    }

    protected void addProblem(@Nonnull final String path, @Nonnull final String problem) {
        assert problem != null;
        assert path != null;
        LOG.error("{} - {}", path, problem);
        problems.add(new MongoDBMigrationProblem(problem, path));
    }

    /**
     * Return 'from'-version.
     *
     * @return Trimmed version string.
     */
    @Nonnull
    protected String getFromVersion() {
        return fromVersion;
    }

    /**
     * Return 'to'-version.
     *
     * @return Trimmed version string.
     */
    @Nonnull
    protected String getToVersion() {
        return toVersion;
    }

    /**
     * Default migration: no-op.
     *
     * @param db MongoDB database.
     * @throws MigrationException Never thrown by this class, but potentially by derived classes, if the migration
     *                            fails.
     */
    protected void migrate(@Nonnull final MongoDB db) throws MigrationException {
        assert db != null;

        // No upgrade required.
        LOG.info("migrate: no upgrade required");

        // Only this method can set databaseChange to false. Derived classes cannot.
        databaseChanged = false;
    }

    /**
     * Execute migration and report if database was changed or not.
     *
     * @param db MongoDB database.
     * @return True if database changed as a result of migration.
     * @throws MigrationException Never thrown by this class, but potentially by derived classes, if the migration
     *                            fails.
     */
    boolean migrateChangedDatabase(@Nonnull final MongoDB db) throws MigrationException {
        assert db != null;

        migrate(db);
        return databaseChanged;
    }

    protected abstract static class Replaceable<T, U> {

        @Nonnull
        private final Context context;
        @Nonnull
        private final T value;

        private Replaceable(@Nonnull final Context context, @Nonnull final T value) {
            assert context != null;
            assert value != null;
            this.context = context;
            this.value = value;
        }

        @Nonnull
        public T get() {
            return value;
        }

        @Nonnull
        public Context getContext() {
            return context;
        }

        public abstract void set(@Nullable final U newValue);
    }

    protected interface Command {
        public void flush();

        /**
         * Ranking for executing the command. Lower ranked commands are executed first.
         *
         * @return The ranking for the command.
         */
        public int ranking();
    }

    private static class PruneList implements Command {

        @Nonnull
        private final Value value;

        private PruneList(@Nonnull final Value value) {
            assert value != null;
            this.value = value;
        }

        @Override
        public void flush() {
            for (final Iterator<Object> it = value.list.iterator(); it.hasNext(); ) {
                if (it.next() == null) {
                    it.remove();
                }
            }
            if (value.list.isEmpty()) {
                value.parent.removeField(value.fieldName);
            }
        }

        @Override
        public int ranking() {
            return 0;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }

            final PruneList pruneList = (PruneList) obj;

            if (!value.list.equals(pruneList.value.list)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return value.list.hashCode();
        }
    }

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    protected class Context {
        @Nullable
        private final Context parent;
        @Nullable
        private Context child;
        @Nullable
        private final DBObject object;
        @Nullable
        private PriorityQueue<Command> commands;
        @Nonnull
        private final String path;

        public Context(@Nullable final Context parent, @Nullable final DBObject object, @Nonnull final String path) {
            assert path != null;
            this.parent = parent;
            this.object = object;
            this.path = path;
            if (object != null) {
                contextMap.put(object, this);
            }
        }

        @Nonnull
        public Context createChild(@Nullable final DBObject object, @Nonnull final String path) {
            assert path != null;
            flushChild();
            child = new Context(this, object, path);
            return child;
        }

        public void addProblem(@Nonnull final String problem) {
            assert problem != null;
            LOG.error(path + ": " + problem);
            problems.add(new MongoDBMigrationProblem(problem, path));
        }

        public void assertValid() {
            //noinspection ObjectEquality
            assert (parent == null) || (parent.child == this) : "Context no longer valid.";
        }

        /**
         * Convenience method for add(Command, boolean) with {@code true} as its second parameter.
         *
         * @param command The command to add.
         */
        public void add(@Nonnull final Command command) {
            add(command, true);
        }

        /**
         * Add a command to the context.
         *
         * @param command      The command to add.
         * @param addIfPresent If true, the command is added even if the command was already added before. If false, the
         *                     command will only be added if not already present.
         */
        public void add(@Nonnull final Command command, final boolean addIfPresent) {
            assert command != null;
            assertValid();
            if (commands == null) {
                commands = new PriorityQueue<>(1, RANKING_COMPARATOR);
            }
            if (addIfPresent || !commands.contains(command)) {
                commands.add(command);
            }
        }

        public void flush() {
            assertValid();
            flushChild();
            if (commands != null) {
                for (Command command = commands.poll(); command != null; command = commands.poll()) {
                    command.flush();
                }
            }
        }

        private void flushChild() {
            if (child != null) {
                child.flush();
                // Child should no longer be used.
                if (child.object != null) {
                    contextMap.remove(child.object);
                }
                child = null;
            }
        }
    }

    protected abstract static class IterableDelegate<T, U> implements Iterable<U> {
        @Nonnull
        final private Iterable<T> delegate;

        protected IterableDelegate(@Nonnull final Iterable<T> delegate) {
            this.delegate = delegate;
        }

        @Nullable
        protected abstract U next(T value);

        protected void finished() {
            // Empty.
        }

        @Override
        public Iterator<U> iterator() {
            final Iterator<T> iterator = delegate.iterator();
            return new Iterator<U>() {
                private U next;
                private boolean finished = false;

                @Override
                public boolean hasNext() {
                    while ((next == null) && iterator.hasNext()) {
                        next = IterableDelegate.this.next(iterator.next());
                    }
                    if (next != null) {
                        return true;
                    }
                    if (!finished) {
                        finished = true;
                        finished();
                    }
                    return false;
                }

                @Override
                public U next() throws NoSuchElementException {
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                    final U result = next;
                    next = null;
                    return result;
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }
    }
}
