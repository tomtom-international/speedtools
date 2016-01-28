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

package com.tomtom.speedtools.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tomtom.speedtools.geometry.GeoPoint;
import com.tomtom.speedtools.geometry.GeoRectangle;
import com.tomtom.speedtools.mongodb.mappers.*;
import com.tomtom.speedtools.mongodb.mappers.EntityMapper.HasFieldName;
import com.tomtom.speedtools.objects.Immutables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

import static com.tomtom.speedtools.mongodb.MongoDBUtils.MONGO_PATH_SEPARATOR;
import static com.tomtom.speedtools.mongodb.MongoDBUtils.mongoPath;

/**
 * Utility to facilitate creating mongo query objects using mapper fields. Fields in nested objects can be addressed.
 * Query values are type-safe.
 *
 * Example:
 * <pre>
 * collection.findAll(new MongoQuery().
 *   eq(personMapper.lastName, "Henderson"),
 *   eq(personMapper.address.street, "Elmo st."),
 *   in(personMapper.roles, Role.Admin, Role.User));
 * </pre>
 */
public class MongoDBQuery {
    public static final String OPERATOR_LT = "$lt";
    public static final String OPERATOR_LTE = "$lte";
    public static final String OPERATOR_GT = "$gt";
    public static final String OPERATOR_GTE = "$gte";
    public static final String OPERATOR_IN = "$in";
    public static final String OPERATOR_NIN = "$nin";
    public static final String OPERATOR_EXISTS = "$exists";
    public static final String OPERATOR_AND = "$and";
    public static final String OPERATOR_OR = "$or";
    public static final String OPERATOR_WITHIN = "$within";
    public static final String OPERATOR_NOT = "$not";
    public static final String OPERATOR_ELEM_MATCH = "$elemMatch";
    public static final String OPERATOR_NE = "$ne";
    public static final String INDEX_0 = "0";

    @Nonnull
    private final Map<String, List<FieldSelection>> fieldSelections = new HashMap<>();

    @Nonnull
    private final static StringMapper STRING_MAPPER = new StringMapper();

    @Nonnull
    public <T> MongoDBQuery eq(
            @Nonnull final EntityMapper<?>.Field<T> f0, @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(f0.getFieldName(), "", f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery eq(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), "", f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery eq(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), "", f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery eq(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), "", f3.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery eq(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final HasFieldName f3,
            @Nonnull final EntityMapper<?>.Field<T> f4,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert f4 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3, f4), "", f4.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery eqAny(
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f0, @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0), "", MongoDBUpdate.getElementMapper(f0.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBQuery eqAny(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), "", MongoDBUpdate.getElementMapper(f1.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBQuery eqAny(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), "", MongoDBUpdate.getElementMapper(f2.getMapper()), value);
    }

    @Nonnull
    public <T> MongoDBQuery eqAny(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<Collection<T>> f3,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), "", MongoDBUpdate.getElementMapper(f3.getMapper()), value);
    }

    /**
     * Method returns a MongoDBQuery that would match attributes with value null or the absence of an attribute.
     *
     * @param <T> Type.
     * @param f0  Element in path to the Field.
     * @return MongoDBQuery that would match attributes with value null or the absence of an attribute.
     */
    @Nonnull
    public <T> MongoDBQuery eqNullOrMissing(
            @Nonnull final EntityMapper<?>.Field<T> f0) {
        assert f0 != null;

        final String fieldName = f0.getFieldName();

        append(fieldName, new FieldSelection("", fieldName) {
            @Override
            @Nullable
            Object toDBValue() {
                return null;
            }
        });
        return this;
    }

    /**
     * Method returns a MongoDBQuery that would match attributes with value null or the absence of an attribute.
     *
     * @param <T> Type.
     * @param f0  First element in path to the Field.
     * @param f1  Second element in path to the Field.
     * @return MongoDBQuery that would match attributes with value null or the absence of an attribute.
     */
    @Nonnull
    public <T> MongoDBQuery eqNullOrMissing(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1) {
        assert f0 != null;

        final String fieldName = mongoPath(f0, f1);

        append(fieldName, new FieldSelection("", fieldName) {
            @Override
            @Nullable
            Object toDBValue() {
                return null;
            }
        });
        return this;
    }

    /**
     * Method returns a MongoDBQuery that would match attributes with value null or the absence of an attribute.
     *
     * @param <T> Type.
     * @param f0  First element in path to the Field.
     * @param f1  Second element in path to the Field.
     * @param f2  Third element in path to the Field.
     * @return MongoDBQuery that would match attributes with value null or the absence of an attribute.
     */
    @Nonnull
    public <T> MongoDBQuery eqNullOrMissing(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2) {
        assert f0 != null;

        final String fieldName = mongoPath(f0, f1, f2);

        append(fieldName, new FieldSelection("", fieldName) {
            @Override
            @Nullable
            Object toDBValue() {
                return null;
            }
        });
        return this;
    }

    @Nonnull
    public <T> MongoDBQuery ne(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), OPERATOR_NE, f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery ne(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), OPERATOR_NE, f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery ne(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(f0.getFieldName(), OPERATOR_NE, f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery not(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(f0.getFieldName(), OPERATOR_NOT, f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery not(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), OPERATOR_NOT, f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery not(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), OPERATOR_NOT, f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery not(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), OPERATOR_NOT, f3.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lt(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(f0.getFieldName(), OPERATOR_LT, f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lt(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), OPERATOR_LT, f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lt(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), OPERATOR_LT, f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lt(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), OPERATOR_LT, f3.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lte(
            @Nonnull final EntityMapper<?>.Field<T> f0, @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0), OPERATOR_LTE, f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lte(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), OPERATOR_LTE, f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lte(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), OPERATOR_LTE, f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery lte(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), OPERATOR_LTE, f3.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gt(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(f0.getFieldName(), OPERATOR_GT, f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gt(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), OPERATOR_GT, f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gt(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), OPERATOR_GT, f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gt(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), OPERATOR_GT, f3.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gte(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final T value) {
        assert f0 != null;
        assert value != null;
        return addFieldSelection(f0.getFieldName(), OPERATOR_GTE, f0.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gte(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1), OPERATOR_GTE, f1.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gte(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2), OPERATOR_GTE, f2.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gte(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), OPERATOR_GTE, f3.getMapper(), value);
    }

    @Nonnull
    public <T> MongoDBQuery gte(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final HasFieldName f3,
            @Nonnull final EntityMapper<?>.Field<T> f4,
            @Nonnull final T value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert f4 != null;
        assert value != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3, f4), OPERATOR_GTE, f4.getMapper(), value);
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery in(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final T... values) {
        assert f0 != null;
        assert values != null;
        final String fieldName = f0.getFieldName();
        final Mapper<T> mapper = f0.getMapper();
        return addFieldSelection(fieldName, OPERATOR_IN, mapper, Immutables.listOf(values));
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery in(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T... values) {
        assert f0 != null;
        assert f1 != null;
        assert values != null;
        final String fieldName = mongoPath(f0, f1);
        final Mapper<T> mapper = f1.getMapper();
        return addFieldSelection(fieldName, OPERATOR_IN, mapper, Immutables.listOf(values));
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery in(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T... values) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert values != null;
        final String fieldName = mongoPath(f0, f1, f2);
        final Mapper<T> mapper = f2.getMapper();
        return addFieldSelection(fieldName, OPERATOR_IN, mapper, Immutables.listOf(values));
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery in(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T... values) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert values != null;
        final String fieldName = mongoPath(f0, f1, f2, f3);
        final Mapper<T> mapper = f3.getMapper();
        return addFieldSelection(fieldName, OPERATOR_IN, mapper, Immutables.listOf(values));
    }

    @Nonnull
    public <T> MongoDBQuery in(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final Collection<T> values) {
        assert f0 != null;
        assert values != null;
        final String fieldName = f0.getFieldName();
        final Mapper<T> mapper = f0.getMapper();
        return addFieldSelection(fieldName, OPERATOR_IN, mapper, values);
    }

    @Nonnull
    public <T> MongoDBQuery in(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final Collection<T> values) {
        assert f1 != null;
        assert values != null;
        final String fieldName = mongoPath(f0, f1);
        final Mapper<T> mapper = f1.getMapper();
        return addFieldSelection(fieldName, OPERATOR_IN, mapper, values);
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery notIn(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final T... values) {
        assert f0 != null;
        assert values != null;
        final String fieldName = f0.getFieldName();
        final Mapper<T> mapper = f0.getMapper();
        return addFieldSelection(fieldName, OPERATOR_NIN, mapper, Immutables.listOf(values));
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery notIn(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<T> f1,
            @Nonnull final T... values) {
        assert f0 != null;
        assert f1 != null;
        assert values != null;
        final String fieldName = mongoPath(f0, f1);
        final Mapper<T> mapper = f1.getMapper();
        return addFieldSelection(fieldName, OPERATOR_NIN, mapper, Immutables.listOf(values));
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery notIn(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<T> f2,
            @Nonnull final T... values) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert values != null;
        final String fieldName = mongoPath(f0, f1, f2);
        final Mapper<T> mapper = f2.getMapper();
        return addFieldSelection(fieldName, OPERATOR_NIN, mapper, Immutables.listOf(values));
    }

    @SafeVarargs
    @Nonnull
    public final <T> MongoDBQuery notIn(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<T> f3,
            @Nonnull final T... values) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert values != null;
        final String fieldName = mongoPath(f0, f1, f2, f3);
        final Mapper<T> mapper = f3.getMapper();
        return addFieldSelection(fieldName, OPERATOR_NIN, mapper, Immutables.listOf(values));
    }

    @Nonnull
    public <T> MongoDBQuery notIn(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final Collection<T> values) {
        assert f0 != null;
        assert values != null;
        return addFieldSelection(f0.getFieldName(), OPERATOR_NIN, f0.getMapper(), values);
    }

    @Nonnull
    public <T> MongoDBQuery elemMatch(
            @Nonnull final EntityMapper<?>.Field<T> f0,
            @Nonnull final MongoDBQuery... q) {
        assert f0 != null;
        assert q != null;

        final String fieldName = mongoPath(f0);
        append(fieldName, new FieldSelection(OPERATOR_ELEM_MATCH, fieldName) {
            @Override
            @Nonnull
            Object toDBValue() throws MapperException {
                final BasicDBObject elemMatch = new BasicDBObject();

                for (final MongoDBQuery query : q) {
                    elemMatch.putAll(query.toDBObject());
                }
                return elemMatch;
            }
        });
        return this;
    }

    /**
     * Method returns a MongoDBQuery that would match the presence of an attribute.
     *
     * @param f0 Element in path to the Field.
     * @return MongoDBQuery that would match the presence of an attribute.
     */
    @Nonnull
    public MongoDBQuery exists(@Nonnull final HasFieldName f0) {
        assert f0 != null;
        return addFieldSelection(mongoPath(f0), OPERATOR_EXISTS, IdentityMapper.getInstance(), true);
    }

    /**
     * Method returns a MongoDBQuery that would match the presence of an attribute.
     *
     * @param f0 First element in path to the Field.
     * @param f1 Second element in path to the Field.
     * @return MongoDBQuery that would match the presence of an attribute.
     */
    @Nonnull
    public MongoDBQuery exists(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1) {
        assert f0 != null;
        assert f1 != null;
        return addFieldSelection(mongoPath(f0, f1), OPERATOR_EXISTS, IdentityMapper.getInstance(), true);
    }

    /**
     * Method returns a MongoDBQuery that would match the presence of an attribute.
     *
     * @param f0 First element in path to the Field.
     * @param f1 Second element in path to the Field.
     * @param f2 Third element in path to the Field.
     * @return MongoDBQuery that would match the presence of an attribute.
     */
    @Nonnull
    public MongoDBQuery exists(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        return addFieldSelection(mongoPath(f0, f1, f2), OPERATOR_EXISTS, IdentityMapper.getInstance(), true);
    }

    /**
     * Method returns a MongoDBQuery that would match the presence of an attribute.
     *
     * @param f0 First element in path to the Field.
     * @param f1 Second element in path to the Field.
     * @param f2 Third element in path to the Field.
     * @param f3 Fourth element in path to the Field.
     * @return MongoDBQuery that would match the presence of an attribute.
     */
    @Nonnull
    public MongoDBQuery exists(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final HasFieldName f3) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        return addFieldSelection(mongoPath(f0, f1, f2, f3), OPERATOR_EXISTS, IdentityMapper.getInstance(), true);
    }

    @Nonnull
    public MongoDBQuery within(
            @Nonnull final EntityMapper<?>.Field<GeoPoint> f0,
            @Nonnull final GeoRectangle value) {
        return doWithin(mongoPath(f0), f0.getMapper(), value);
    }

    @Nonnull
    public MongoDBQuery within(
            @Nonnull final HasFieldName f0,
            @Nonnull final EntityMapper<?>.Field<GeoPoint> f1,
            @Nonnull final GeoRectangle value) {
        assert f0 != null;
        assert f1 != null;
        assert value != null;
        return doWithin(mongoPath(f0, f1), f1.getMapper(), value);
    }

    @Nonnull
    public MongoDBQuery within(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final EntityMapper<?>.Field<GeoPoint> f2,
            @Nonnull final GeoRectangle value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert value != null;
        return doWithin(mongoPath(f0, f1, f2), f2.getMapper(), value);
    }

    @Nonnull
    public MongoDBQuery within(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final EntityMapper<?>.Field<GeoPoint> f3,
            @Nonnull final GeoRectangle value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert value != null;
        return doWithin(mongoPath(f0, f1, f2, f3), f3.getMapper(), value);
    }

    @Nonnull
    public MongoDBQuery within(
            @Nonnull final HasFieldName f0,
            @Nonnull final HasFieldName f1,
            @Nonnull final HasFieldName f2,
            @Nonnull final HasFieldName f3,
            @Nonnull final EntityMapper<?>.Field<GeoPoint> f4,
            @Nonnull final GeoRectangle value) {
        assert f0 != null;
        assert f1 != null;
        assert f2 != null;
        assert f3 != null;
        assert f4 != null;
        assert value != null;
        return doWithin(mongoPath(f0, f1, f2, f3, f4), f4.getMapper(), value);
    }

    @Nonnull
    public MongoDBQuery ofType(
            @Nonnull final EntityMapper<?> type) {
        assert type != null;
        assert !type.getDiscriminator().isEmpty();

        return addFieldSelection(MongoDBKeyNames.DISCRIMINATOR_KEY, "", STRING_MAPPER, type.getDiscriminator());
    }

    @Nonnull
    public MongoDBQuery or(final MongoDBQuery... queryOperands) {
        append(OPERATOR_OR, new FieldSelection("", "") {
            @Override
            Object toDBValue() throws MapperException {
                final List<Object> operands = new ArrayList<>();
                for (final MongoDBQuery operand : queryOperands) {
                    operands.add(operand.toDBObject());
                }
                return operands;
            }
        });

        return this;
    }

    /**
     * Method created a HasFieldName instance that will return a reference to the first element of the given collection
     * field.
     *
     * @param collectionField A collection field.
     * @return Reference to first element in array.
     */
    @Nonnull
    public static HasFieldName first(final HasFieldName collectionField) {
        return new HasFieldName() {
            @Nonnull
            @Override
            public String getFieldName() {
                return collectionField.getFieldName() + MONGO_PATH_SEPARATOR + INDEX_0;
            }
        };
    }

    @Nonnull
    public DBObject toDBObject() throws MapperException {
        final DBObject result = new BasicDBObject();
        for (final Entry<String, List<FieldSelection>> entry : fieldSelections.entrySet()) {
            final List<FieldSelection> selections = entry.getValue();
            assert !selections.isEmpty();
            if (selections.size() == 1) {
                result.put(entry.getKey(), selections.get(0).toDBObject());
            } else {
                final List<Object> operands = new ArrayList<>();
                for (final FieldSelection selection : selections) {
                    operands.add(selection.toDBObject());
                }
                result.put(OPERATOR_AND, operands);
            }
        }
        return result;
    }

    @Nonnull
    private <T> MongoDBQuery addFieldSelection(@Nonnull final String fieldName, @Nonnull final String operator,
                                               @Nonnull final Mapper<T> mapper, @Nonnull final Collection<T> values) {
        assert fieldName != null;
        assert operator != null;
        assert mapper != null;
        append(fieldName, new FieldSelection(operator, fieldName) {
            @Override
            @Nonnull
            Object toDBValue() throws MapperException {
                final List<Object> mappedValues = new ArrayList<>();
                for (final T value : values) {
                    mappedValues.add(mapper.toDb(value));
                }
                return mappedValues;
            }
        });
        return this;
    }

    @Nonnull
    private <T> MongoDBQuery addFieldSelection(@Nonnull final String fieldName, @Nonnull final String operator,
                                               @Nonnull final Mapper<T> mapper, @Nullable final T value) {
        assert fieldName != null;
        assert operator != null;
        assert mapper != null;
        append(fieldName, new FieldSelection(operator, fieldName) {
            @Override
            @Nullable
            Object toDBValue() throws MapperException {
                return mapper.toDb(value);
            }
        });
        return this;
    }

    private void append(@Nonnull final String fieldName, @Nonnull final FieldSelection selection) {
        assert fieldName != null;
        assert selection != null;
        final List<FieldSelection> selections = fieldSelections.get(fieldName);
        if (selections == null) {
            fieldSelections.put(fieldName, Collections.singletonList(selection));
        } else if (selections.size() == 1) {
            final List<FieldSelection> list = new ArrayList<>(selections);
            list.add(selection);
            fieldSelections.put(fieldName, list);
        } else {
            assert selections.size() > 1;
            selections.add(selection);
        }
    }

    @Nonnull
    private MongoDBQuery doWithin(@Nonnull final String fieldName, @Nonnull final Mapper<GeoPoint> mapper,
                                  @Nonnull final GeoRectangle value) {
        append(fieldName, new FieldSelection(OPERATOR_WITHIN, fieldName) {
            @Override
            Object toDBValue() throws MapperException {
                return MongoDBUtils.createBasicDBObjectFromRectangle(value, (GeoPointMapper) mapper);
            }
        });
        return this;
    }


    private static abstract class FieldSelection {
        @Nonnull
        public final String operator;
        @Nonnull
        public final String fieldName;

        private FieldSelection(@Nonnull final String operator, @Nonnull final String fieldName) {
            assert operator != null;
            assert fieldName != null;
            this.operator = operator;
            this.fieldName = fieldName;
        }

        @Nullable
        Object toDBObject() throws MapperException {
            if (operator.isEmpty()) {
                return toDBValue();
            } else {
                return new BasicDBObject(operator, toDBValue());
            }
        }

        @Nullable
        abstract Object toDBValue() throws MapperException;
    }

    @Override
    @Nonnull
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append('{');
        String sep = "";
        for (final List<FieldSelection> selections : fieldSelections.values()) {
            for (final FieldSelection selection : selections) {
                out.append(sep);
                sep = ", ";
                Object value;
                try {
                    value = selection.toDBValue();
                } catch (final MapperException ignored) {
                    value = "?";
                }
                out.append(selection.fieldName).append(':');
                if (selection.operator.isEmpty()) {
                    out.append(selection.operator).append('(').append(value).append(')');
                } else {
                    out.append(value);
                }
            }
        }
        out.append('}');
        return out.toString();
    }
}
