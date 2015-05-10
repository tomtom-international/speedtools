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

package com.tomtom.speedtools.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tomtom.speedtools.mongodb.mappers.*;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper.HasFieldName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.tomtom.speedtools.mongodb.MongoDBUtils.mongoPath;

/**
 * Utility to facilitate creating mongo update objects using mapper fields. Fields in nested objects can be addressed.
 * Query values are type-safe.
 *
 * Example:
 * <pre>
 * collection.update(
 *   new MongoDBQuery().
 *     eq(personMapper.lastName, "Henderson"),
 *   new MongoDBUpdate().
 *     set(personMapper.address.street, "Sunset blvd."));
 * </pre>
 */
public class MongoDBUpdate {

    public static final String OPERATOR_SET = "$set";
    public static final String OPERATOR_UNSET = "$unset";
    public static final String OPERATOR_PUSH = "$push";
    public static final String OPERATOR_PUSH_ALL = "$pushAll";
    public static final String OPERATOR_PULL = "$pull";
    public static final String OPERATOR_POP_FIRST = "$popFirst";
    public static final String OPERATOR_POP_LAST = "$popLast";
    public static final String OPERATOR_INC = "$inc";
    public static final String OPERATOR_ADD_TO_SET = "$addToSet";

    @Nonnull
    private final List<Modifier> modifiers = new LinkedList<>();

    @Nonnull
    public <T> MongoDBUpdate set(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nullable final T value) {
        assert f0 != null;
        return addModifier(OPERATOR_SET, mongoPath(f0), f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate set(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        return addModifier(OPERATOR_SET, mongoPath(f0, f1), f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate set(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        return addModifier(OPERATOR_SET, mongoPath(f0, f1, f2), f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate set(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addModifier(OPERATOR_SET, mongoPath(f0, f1, f2, f3), f3.getMapper(), value);
    }

    @Nonnull
    public <E> MongoDBUpdate setAllExcept(
            @Nonnull final EntityMapper<E> mapper,
            @Nonnull final E entity,
            @Nonnull final EntityMapper<E>.Field<?>... fields) throws MapperException {
        assert mapper != null;
        assert entity != null;
        assert fields != null;
        final Set<EntityMapper<E>.Field<?>> fieldSet = new HashSet<>(Arrays.asList(fields));

        // Get all fields from the sub mapper.
        for (final EntityMapper<E>.Field<?> field : mapper.getSubMapper(
                entity.getClass()).getFieldsIncludingSuperEntities()) {
            if (!fieldSet.contains(field)) {
                // ID field must be excluded.
                if (!field.getFieldName().equals(MongoDBKeyNames.ID_KEY)) {
                    final DBObject dbObject = mapper.toDb(entity);
                    // Since entity is not null a dbObject is expected.
                    assert dbObject != null;
                    final Object value = dbObject.get(field.getFieldName());

                    if (value != null) {
                        modifiers.add(new Modifier(OPERATOR_SET, mongoPath(field)) {
                            @Nullable
                            @Override
                            Object toDBValue() {
                                return value;
                            }
                        });
                    } else {
                        // When a value is null it has to be unset since the policy is to not store null values.
                        modifiers.add(new Modifier(OPERATOR_UNSET, mongoPath(field)) {
                            @Nullable
                            @Override
                            Object toDBValue() {
                                return 1;
                            }
                        });
                    }
                }
            }
        }
        return this;
    }

    @Nonnull
    public <T> MongoDBUpdate setRaw(
            @Nonnull final String fieldName,
            @Nullable final T value) {
        assert fieldName != null;
        return addModifier(OPERATOR_SET, fieldName, IdentityMapper.getInstance(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate setRaw(
            @Nonnull final String fieldName,
            @Nonnull final Mapper<T> mapper,
            @Nullable final T value) {
        assert fieldName != null;
        assert mapper != null;
        return addModifier(OPERATOR_SET, fieldName, mapper, value);
    }

    @Nonnull
    public MongoDBUpdate unset(@Nonnull final EntityMapper<?>.Field<?>... fs) {
        assert fs != null;
        assert fs.length > 0;
        return addModifier(OPERATOR_UNSET, mongoPath(fs), IdentityMapper.getInstance(), 1);
    }

    @Nonnull
    public <T> MongoDBUpdate push(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0,
            @Nullable final T value) {
        assert f0 != null;
        return addModifier(OPERATOR_PUSH, mongoPath(f0), getElementMapper(f0.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate push(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addModifier(OPERATOR_PUSH, mongoPath(f0, f1), getElementMapper(f1.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate push(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        return addModifier(OPERATOR_PUSH, mongoPath(f0, f1, f2), getElementMapper(f2.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate push(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addModifier(OPERATOR_PUSH, mongoPath(f0, f1, f2, f3), getElementMapper(f3.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate pushAll(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0,
            final Collection<T> values) {
        assert f0 != null;
        assert values != null;
        return addModifier(OPERATOR_PUSH_ALL, mongoPath(f0), f0.getMapper(), values);
    }

    @Nonnull
    public <T> MongoDBUpdate pushAll(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1,
            final Collection<T> values) {
        assert f0 != null;
        assert f1 != null;
        assert values != null;
        return addModifier(OPERATOR_PUSH_ALL, mongoPath(f0, f1), f1.getMapper(), values);
    }

    @Nonnull
    public <T> MongoDBUpdate pushAll(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2,
            final Collection<T> values) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert values != null;
        return addModifier(OPERATOR_PUSH_ALL, mongoPath(f0, f1, f2), f2.getMapper(), values);
    }

    @Nonnull
    public <T> MongoDBUpdate pushAll(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3,
            final Collection<T> values) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addModifier(OPERATOR_PUSH_ALL, mongoPath(f0, f1, f2, f3), f3.getMapper(), values);
    }

    @Nonnull
    public <T> MongoDBUpdate pull(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0,
            @Nullable final T value) {
        assert f0 != null;
        return addModifier(OPERATOR_PULL, mongoPath(f0), getElementMapper(f0.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate pull(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        return addModifier(OPERATOR_PULL, mongoPath(f0, f1), getElementMapper(f1.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate pull(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        return addModifier(OPERATOR_PULL, mongoPath(f0, f1, f2), getElementMapper(f2.getMapper()), value);
    }

    /**
     * Removes all occurrences of value from a field, if field is an array. If the field is present but is not an array,
     * an error condition is raised.
     *
     * Mongo shell equivalent:
     * <pre>
     * { $pull : { field : {field2: value} } }
     * </pre>
     *
     * @param <T>          Type.
     * @param f0           The field that holds an array.
     * @param mongoDBQuery The query that matches an element in the array.
     * @return This instance.
     */
    @Nonnull
    public <T> MongoDBUpdate pull(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0,
            @Nonnull final MongoDBQuery mongoDBQuery) {
        assert f0 != null;
        assert mongoDBQuery != null;

        final String fieldName = mongoPath(f0);
        modifiers.add(new Modifier(OPERATOR_PULL, fieldName) {
            @Override
            @Nullable
            Object toDBValue() throws MapperException {
                return mongoDBQuery.toDBObject();
            }
        });
        return this;
    }

    @Nonnull
    public <T> MongoDBUpdate pull(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addModifier(OPERATOR_PULL, mongoPath(f0, f1, f2, f3), getElementMapper(f3.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate popFirst(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0) {
        assert f0 != null;
        final Integer value = -1;
        return addModifier(OPERATOR_POP_FIRST, mongoPath(f0), IdentityMapper.getInstance(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate popFirst(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1) {
        assert f0 != null;
        assert f1 != null;
        final Integer value = -1;
        return addModifier(OPERATOR_POP_FIRST, mongoPath(f0, f1), IdentityMapper.getInstance(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate popFirst(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        final Integer value = -1;
        return addModifier(OPERATOR_POP_FIRST, mongoPath(f0, f1, f2), IdentityMapper.getInstance(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate popFirst(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        final Integer value = -1;
        return addModifier(OPERATOR_POP_FIRST, mongoPath(f0, f1, f2, f3), IdentityMapper.getInstance(), value);
    }

    @Nonnull
    public <T> MongoDBUpdate popLast(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0) {
        assert f0 != null;
        return addModifier(OPERATOR_POP_LAST, mongoPath(f0), IdentityMapper.getInstance(), 1);
    }

    @Nonnull
    public <T> MongoDBUpdate popLast(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1) {
        assert f0 != null;
        assert f1 != null;
        return addModifier(OPERATOR_POP_LAST, mongoPath(f0, f1), IdentityMapper.getInstance(), 1);
    }

    @Nonnull
    public <T> MongoDBUpdate popLast(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        return addModifier(OPERATOR_POP_LAST, mongoPath(f0, f1, f2), IdentityMapper.getInstance(), 1);
    }

    @Nonnull
    public <T> MongoDBUpdate popLast(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addModifier(OPERATOR_POP_LAST, mongoPath(f0, f1, f2, f3), IdentityMapper.getInstance(), 1);
    }

    @Nonnull
    public <T> MongoDBUpdate inc(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0,
            @Nullable final T value) {
        assert f0 != null;
        return addModifier(OPERATOR_INC, mongoPath(f0), getElementMapper(f0.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate inc(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        return addModifier(OPERATOR_INC, mongoPath(f0, f1), getElementMapper(f1.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate inc(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        return addModifier(OPERATOR_INC, mongoPath(f0, f1, f2), getElementMapper(f2.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate inc(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addModifier(OPERATOR_INC, mongoPath(f0, f1, f2, f3), getElementMapper(f3.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate addToSet(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0,
            @Nullable final T value) {
        assert f0 != null;
        return addModifier(OPERATOR_ADD_TO_SET, mongoPath(f0), getElementMapper(f0.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate addToSet(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addModifier(OPERATOR_ADD_TO_SET, mongoPath(f0, f1), getElementMapper(f1.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate addToSet(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        return addModifier(OPERATOR_ADD_TO_SET, mongoPath(f0, f1, f2), getElementMapper(f2.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBUpdate addToSet(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3,
            @Nullable final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addModifier(OPERATOR_ADD_TO_SET, mongoPath(f0, f1, f2, f3), getElementMapper(f3.getMapper()), value);
    }

    @Nonnull
    public DBObject toDBObject() throws MapperException {
        final DBObject result = new BasicDBObject();
        for (final Modifier modifier : modifiers) {
            BasicDBObject existing = (BasicDBObject) result.get(modifier.operator);
            if (existing == null) {
                existing = new BasicDBObject();
            }
            result.put(modifier.operator, existing.append(modifier.fieldName, modifier.toDBValue()));
        }
        return result;
    }

    @Nonnull
    private <T> MongoDBUpdate addModifier(
            @Nonnull final String operator,
            @Nonnull final String fieldName,
            @Nonnull final Mapper<Collection<T>> mapper,
            @Nonnull final Collection<T> values) {
        assert operator != null;
        assert fieldName != null;
        assert mapper != null;
        assert values != null;
        modifiers.add(new Modifier(operator, fieldName) {
            @Override
            @Nonnull
            Object toDBValue() throws MapperException {
                final List<Object> mappedValues = new ArrayList<>();
                final Mapper<T> elementMapper = getElementMapper(mapper);
                for (@Nullable final T value : values) {
                    mappedValues.add(elementMapper.toDb(value));
                }
                return mappedValues;
            }
        });
        return this;
    }

    @Nonnull
    private <T> MongoDBUpdate addModifier(
            @Nonnull final String operator,
            @Nonnull final String fieldName,
            @Nonnull final Mapper<T> mapper,
            @Nullable final T value) {
        assert operator != null;
        assert fieldName != null;
        assert mapper != null;
        modifiers.add(new Modifier(operator, fieldName) {
            @Override
            @Nullable
            Object toDBValue() throws MapperException {
                return mapper.toDb(value);
            }
        });
        return this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    static <T> Mapper<T> getElementMapper(@Nonnull final Mapper<Collection<T>> mapper) {
        assert mapper instanceof CollectionMapper;
        return ((CollectionMapper) mapper).getElementMapper();
    }

    private static abstract class Modifier {
        @Nonnull
        public final String operator;
        @Nonnull
        public final String fieldName;

        private Modifier(@Nonnull final String operator, @Nonnull final String fieldName) {
            assert operator != null;
            assert fieldName != null;
            this.operator = operator;
            this.fieldName = fieldName;
        }

        @Nullable
        abstract Object toDBValue() throws MapperException;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append('{');
        String sep = "";
        for (final Modifier modifier : modifiers) {
            out.append(sep);
            sep = ", ";
            Object value;
            try {
                value = modifier.toDBValue();
            } catch (final MapperException ignored) {
                value = "?";
            }
            out.append(modifier.fieldName).append(':');
            if (modifier.operator.isEmpty()) {
                out.append(modifier.operator).append('(').append(value).append(')');
            } else {
                out.append(value);
            }
        }
        out.append('}');
        return out.toString();
    }
}
