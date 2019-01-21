/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.json;

import com.google.common.collect.ImmutableMap;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;
import com.tomtom.speedtools.domain.Uid;
import com.tomtom.speedtools.objects.Immutables;
import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.utils.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple POJO (Plain Old Java Object, or 'regular Java object') to JSON object mapper. It is lenient, invalid values
 * may be discarded or an exception can be thrown (customizable).
 * <p>
 * For non JSON-type fields, the serializer returns a Java Map object that contains the JSON representation of the
 * supplied POJO. However, This mapper may be overridden to generate other forms of JSON map objects, such as a MongoDB
 * DBObject (which effectively is also a JSON map object). To do so, override the {@link #createJsonMappedObject()}
 * method.
 */
@SuppressWarnings("UnnecessarilyQualifiedInnerClassAccess")
public class SimpleJsonSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleJsonSerializer.class);

    /**
     * Fields used by serializer to denote class name and nameless values.
     */
    private static final String CLASS_NAME_FIELD = "@class";
    private static final String WRAPPER_VALUE_FIELD = "_value";

    /**
     * Indentation for pretty printing.
     */
    private static final String INDENT = "    "; // Indent is 4 spaces.

    /**
     * Prefixes for getters and setters.
     */
    private static final List<String> GETTER_PREFIXES = Immutables.listOf("get", "is", "has");
    private static final String SETTER_PREFIX = "set";

    /**
     * Look-up cache for mappers.
     */
    private final Map<String, Mapper> mappers = new ConcurrentHashMap<>();

    /**
     * Singleton instance of this class.
     */
    private static final SimpleJsonSerializer INSTANCE = new SimpleJsonSerializer();

    /**
     * Constructor should only be used by singleton instance and derived classes.
     */
    protected SimpleJsonSerializer() {
        super();
    }

    /**
     * Get singleton instance of this class.
     *
     * @return Singleton instance.
     */
    @Nonnull
    public static SimpleJsonSerializer getInstance() {
        return INSTANCE;
    }

    /**
     * Serialize a POJO into a JSON value object.
     *
     * @param pojo POJO to serialize.
     * @return JSON mapped object, or null if serialization failed.
     */
    @Nullable
    public Object serialize(@Nullable final Object pojo) {
        if (pojo == null) {
            return null;
        }

        Object serialized;
        try {
            final Class<?> cls = pojo.getClass();
            final Mapper mapper = getOrCreateMapper(cls);
            final IdentityHashMap<Object, Object> seen = new IdentityHashMap<>();
            serialized = mapper.toJson(pojo, cls, seen);
        } catch (final RuntimeException e) {
            LOG.warn("serialize: failure, value='{}'. Runtime exception:", Json.toStringJson(pojo), e);
            serialized = null;
        }
        return serialized;
    }

    /**
     * Given a JSON value object, deserialize it into a POJO.
     *
     * @param jsonValue JSON mapped object to deserialize.
     * @return POJO object. If deserialization failed, the return value is null. If the serialized object was null,
     * however, the deserialized object is also null.
     */
    @Nullable
    public Object deserialize(@Nullable final Object jsonValue) {
        Object deserialized;
        try {
            final Mapper mapper = getOrCreateMapper(Object.class);
            deserialized = mapper.fromJson(jsonValue);
        } catch (final RuntimeException e) {
            LOG.warn("deserialize: failure, value='{}'. Runtime exception:", Json.toStringJson(jsonValue), e);
            deserialized = null;
        }
        return deserialized;
    }

    /**
     * Convert a POJO to JSON string object. Note that there is no method to read the JSON string representation and
     * deserialize it. It is primarily used for logging.
     * <p>
     * Also note that the string representation includes additional tags for class/type names, unnamed values and such.
     * As such, it is not compatible with other JSON frameworks, such as Jackson, and is not suitable for use in JSON
     * web responses, for example.
     *
     * @param pojo   POJO to convert.
     * @param pretty True for pretty printing (indented).
     * @return JSON string representation of POJO.
     */
    @Nonnull
    public String toStringPretty(@Nullable final Object pojo, final boolean pretty) {
        return toStringInternal(pojo, "\n", pretty);
    }

    /**
     * Same as {@link #toStringPretty(Object, boolean)}, without pretty printing.
     *
     * @param pojo POJO to convert.
     * @return JSON string representation of POJO.
     */
    @Nonnull
    public String toString(@Nullable final Object pojo) {
        return toStringPretty(pojo, false);
    }

    /**
     * Create a new JSON mapped object. Note that this object can be overridden by derived implementations to, for
     * example, create a MongoDB JSON object.
     *
     * @return New, empty JSON mapped object.
     */
    @Nonnull
    protected Map<String, Object> createJsonMappedObject() {
        return new LinkedHashMap<>();
    }

    // ------------------------------------------------------------------------
    // Private methods and classes.
    // ------------------------------------------------------------------------

    /**
     * Find the mapper for a specific object type. Uses a lookup cache to speed things up.
     *
     * @param type Object type.
     * @return Object mapper.
     */
    @Nonnull
    private Mapper getOrCreateMapper(@Nonnull final Type type) {
        assert type != null;

        // Lookup mapper in cache.
        Mapper mapper = mappers.get(type.toString());
        if (mapper == null) {

            // Might be calculated multiple times, but result will always be the same.
            mapper = determineMapper(type);
            mappers.put(type.toString(), mapper);
        }
        return mapper;
    }

    /**
     * Determine the mapper for a given raw type. You can override this method to add additional mappers. You don't need
     * to call super.determineMapperFromRawType() in that case.
     *
     * @param rawType Object type.
     * @return Always null (needs to be overridden in order to be useful).
     */
    @Nullable
    protected Mapper determineMapperFromRawType(@Nonnull final Class<?> rawType) {
        assert rawType != null;
        return null;
    }

    /**
     * Determine object mapper for a specific object type.
     *
     * @param type Object type.
     * @return Object mapper.
     */
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private Mapper determineMapper(@Nonnull final Type type) {
        assert type != null;

        // Get the object type.
        final Class<?> rawType = getRawType(type);

        /**
         * Now, for every known type, return its object mapper.
         */
        final Mapper mapper = determineMapperFromRawType(rawType);
        if (mapper != null) {
            return mapper;
        }

        if (List.class.isAssignableFrom(rawType)) {
            return new CollectionMapper(type);
        }

        if (Set.class.isAssignableFrom(rawType)) {
            return new WrappedCollectionMapper(type);
        }

        if (rawType.isArray()) {
            return new ArrayMapper(type);
        }

        if (DateTime.class.isAssignableFrom(rawType)) {
            return dateTimeMapper;
        }

        if (Integer.class.isAssignableFrom(rawType) || Integer.TYPE.isAssignableFrom(rawType)) {
            return intMapper;
        }

        if (Long.class.isAssignableFrom(rawType) || Long.TYPE.isAssignableFrom(rawType)) {
            return longMapper;
        }

        if (Byte.class.isAssignableFrom(rawType) || Byte.TYPE.isAssignableFrom(rawType)) {
            return byteMapper;
        }

        if (Short.class.isAssignableFrom(rawType) || Short.TYPE.isAssignableFrom(rawType)) {
            return shortMapper;
        }

        if (Float.class.isAssignableFrom(rawType) || Float.TYPE.isAssignableFrom(rawType)) {
            return floatMapper;
        }

        if (Double.class.isAssignableFrom(rawType) || Double.TYPE.isAssignableFrom(rawType)) {
            return doubleMapper;
        }

        if (Character.class.isAssignableFrom(rawType) || Character.TYPE.isAssignableFrom(rawType)) {
            return charMapper;
        }

        if (CharSequence.class.isAssignableFrom(rawType)) {
            return stringMapper;
        }

        if (Boolean.class.isAssignableFrom(rawType) || Boolean.TYPE.isAssignableFrom(rawType)) {
            return booleanMapper;
        }

        if (Currency.class.isAssignableFrom(rawType)) {
            return currencyMapper;
        }

        if (Uid.class.isAssignableFrom(rawType)) {
            return uidMapper;
        }

        if (Class.class.isAssignableFrom(rawType)) {
            return classMapper;
        }

        if (rawType.isEnum()) {
            //noinspection unchecked
            return new EnumMapper(rawType);
        }

        @Nonnull final Map<String, PropertyInfo> propertyMap = new HashMap<>();
        findBeanProperties(rawType, propertyMap);

        // Find suitable constructor.
        PropertyInfo[] mostConstructorProperties = null;
        final List<BeanInfo.Ctor> ctors = new ArrayList<>();
        final Constructor<?>[] declaredConstructors = rawType.getDeclaredConstructors();
        for (final Constructor<?> constructor : declaredConstructors) {
            final PropertyInfo[] constructorProperties = getCompatibleConstructor(constructor, propertyMap);

            // Add the constructor to the list of constructors and force it to be accessible.
            if (constructorProperties != null) {

                // Set the nullable flag for the constructor parameters according to the annotations.
                for (int i = 0; i < constructorProperties.length; ++i) {
                    final Annotation[] annotations = constructor.getParameterAnnotations()[i];
                    if (annotations.length > 0) {
                        LOG.debug("determineMapper: Parameter {} has annotations: {}",
                                constructorProperties[i].name, Json.toStringJson(annotations));
                    }
                    final boolean nullable = !hasAnnotationNonnull(annotations);
                    constructorProperties[i] = new PropertyInfo(constructorProperties[i], nullable);
                }

                constructor.setAccessible(true);
                ctors.add(new BeanInfo.Ctor(constructor, constructorProperties));
                if ((mostConstructorProperties == null) ||
                        (mostConstructorProperties.length < constructorProperties.length)) {
                    mostConstructorProperties = constructorProperties;
                }
            }
        }

        // Add all constructor parameters to the BeanInfo mapper object.
        final Set<PropertyInfo> properties = new LinkedHashSet<>();
        if (mostConstructorProperties != null) {
            properties.addAll(Immutables.listOf(mostConstructorProperties));
        }

        // And add the properties with setters.
        for (final PropertyInfo property : propertyMap.values()) {
            //noinspection VariableNotUsedInsideIf
            if (property.setter != null) {
                properties.add(property);
            }
        }

        return new BeanInfoMapper(new BeanInfo(rawType, ctors, properties));
    }

    /**
     * Figure out if one of the annotations (of a parameter) is NotNull or Nonnull.
     *
     * @param annotations List of annotations.
     * @return True if the annotations contains Nonnull.
     */
    private static boolean hasAnnotationNonnull(@Nonnull final Annotation[] annotations) {
        assert annotations != null;
        for (final Annotation annotation : annotations) {

            /**
             * Now, unfortunately, some or all of these annotations may not be available runtime
             * if their retention policy is not set to RUNTIME...
             */
            if (annotation instanceof Nonnull) {        // This one is not available runtime.

                /**
                 * The annotation javax.annotation.Nonnull does not seem to exists
                 * anymore...
                 */
                return true;
            }
        }
        return false;
    }

    private void findBeanProperties(
            @Nonnull final Class<?> objClass,
            @Nonnull final Map<String, PropertyInfo> properties) {
        assert objClass != null;
        assert properties != null;

        final Class<?> superclass = objClass.getSuperclass();
        if (superclass != null) {
            findBeanProperties(superclass, properties);
        }

        if (objClass.equals(Object.class)) {
            return;
        }

        for (final Method getter : objClass.getDeclaredMethods()) {
            final String getterPrefix = getterPrefixOrNull(getter.getName());
            if ((getter.getParameterTypes().length == 0) && (getterPrefix != null)) {

                final int getterPrefixLength = getterPrefix.length();
                final Character propertyNameFirstCharacter = getter.getName().charAt(getterPrefixLength);
                final String propertyNameWithoutFirstCharacter = getter.getName().substring(getterPrefixLength + 1);
                final String propertyName = Character.toLowerCase(propertyNameFirstCharacter) +
                        propertyNameWithoutFirstCharacter;

                // Find corresponding setter.
                @Nullable Method setter = null;
                final String setterName = SETTER_PREFIX + Character.toUpperCase(propertyNameFirstCharacter) +
                        propertyNameWithoutFirstCharacter;
                for (final Method s : objClass.getDeclaredMethods()) {
                    if ((s.getParameterTypes().length == 1) && s.getName().equals(setterName)) {
                        setter = s;
                        break;
                    }
                }

                properties.put(propertyName, new PropertyInfo(propertyName, getter, setter));
            }
        }
    }

    @Nullable
    private static String getterPrefixOrNull(@Nonnull final String methodName) {
        assert methodName != null;
        for (final String getterPrefix : GETTER_PREFIXES) {
            if (methodName.startsWith(getterPrefix) &&
                    (methodName.length() > getterPrefix.length()) &&
                    Character.isUpperCase(methodName.charAt(getterPrefix.length()))) {
                return getterPrefix;
            }
        }
        return null;
    }

    @Nullable
    private PropertyInfo[] getCompatibleConstructor(@Nonnull final Constructor<?> constructor,
                                                    @Nonnull final Map<String, PropertyInfo> properties) {
        assert constructor != null;
        assert properties != null;

        // Get all parameter names reflectively (if possible).
        final String[] parameterNames;
        try {
            parameterNames = new BytecodeReadingParanamer().lookupParameterNames(constructor);
        } catch (final ParameterNamesNotFoundException ignored) {

            // Some libraries don't provide names, so return null (no ctor found).
            return null;
        }

        // Create a list of constructor parameter properties.
        final PropertyInfo[] constructorProperties = new PropertyInfo[parameterNames.length];
        for (int i = 0; i < parameterNames.length; ++i) {
            final PropertyInfo property = properties.get(parameterNames[i]);
            if (property == null) {

                // The parameter name was not found in our list, no ctor found.
                return null;
            }
            constructorProperties[i] = property;
        }
        return constructorProperties;
    }

    @Nullable
    private Map<String, Object> createWrappedValue(@Nonnull final Class<?> type, @Nullable final Object value) {
        assert type != null;
        if (value == null) {
            return null;
        }
        final Map<String, Object> jsonObject = createJsonMappedObject();
        jsonObject.put(CLASS_NAME_FIELD, type.getName());
        final String actualValue;
        if (type.isEnum()) {
            actualValue = ((Enum) value).name();
        } else {
            actualValue = value.toString();
        }
        jsonObject.put(WRAPPER_VALUE_FIELD, actualValue);
        return jsonObject;
    }

    @Nonnull
    private static Class<?> getRawType(final @Nonnull Type type) {
        assert type != null;
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }

        if (type instanceof Class) {
            return (Class<?>) type;
        }
        return Object.class;
    }

    @Nonnull
    private static Class<?> getBoxedType(@Nonnull final Type type) {
        assert type != null;
        if (type instanceof Class) {
            final Class<?> cls = (Class<?>) type;
            if (cls.isPrimitive()) {

                if (cls.equals(Integer.TYPE)) {
                    return Integer.class;
                }

                if (cls.equals(Long.TYPE)) {
                    return Long.class;
                }

                if (cls.equals(Byte.TYPE)) {
                    return Byte.class;
                }

                if (cls.equals(Boolean.TYPE)) {
                    return Boolean.class;
                }

                if (cls.equals(Short.TYPE)) {
                    return Short.class;
                }

                if (cls.equals(Character.TYPE)) {
                    return Character.class;
                }

                if (cls.equals(Float.TYPE)) {
                    return Float.class;
                }

                if (cls.equals(Double.TYPE)) {
                    return Double.class;
                }
            }
            return cls;
        }
        return Object.class;
    }

    @Nonnull
    private String toStringInternal(
            @Nullable final Object value,
            @Nonnull final String prefix,
            final boolean pretty) {
        assert prefix != null;

        final StringBuilder sb = new StringBuilder();
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            final String escapedValue = StringUtils.encodeToJsonString((String) value);
            sb.append(escapedValue);
        } else if (value instanceof Character) {
            final String escapedValue = StringUtils.encodeToJsonString(String.valueOf(value));
            sb.append(escapedValue);
        } else if (value instanceof Number) {
            sb.append(value);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Date) {
            sb.append('"').append(UTCTime.from((Date) value)).append('"');
        } else if (value instanceof List) {
            if (pretty) {
                sb.append(prefix);
            }
            sb.append('[');
            final String nestedPrefix = prefix + INDENT;
            String sep = "";
            for (final Object elem : (List) value) {
                sb.append(sep);
                sep = ", ";
                sb.append(toStringInternal(elem, nestedPrefix, pretty));
            }
            if (pretty) {
                sb.append(prefix);
            }
            sb.append(']');
        } else if (value instanceof Map) {
            if (pretty) {
                sb.append(prefix);
            }
            sb.append('{');
            final String nestedPrefix = prefix + INDENT;
            String sep = "";
            //noinspection unchecked
            for (final Entry<String, Object> elem : ((Map<String, Object>) value).entrySet()) {
                sb.append(sep);
                sep = ", ";
                if (pretty) {
                    sb.append(prefix).append(INDENT);
                }
                sb.append(StringUtils.encodeToJsonString(elem.getKey())).append(": ");
                sb.append(toStringInternal(elem.getValue(), nestedPrefix, pretty));
            }
            if (pretty) {
                sb.append(prefix);
            }
            sb.append('}');
        } else {
            sb.append(toStringInternal(serialize(value), prefix, pretty));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // BeanInfo class.
    // ------------------------------------------------------------------------

    private static class BeanInfo {
        @Nonnull
        private final Class<?> beanClass;
        @Nonnull
        private final List<Ctor> ctors;         // Sorted most args to least.
        @Nonnull
        private final Set<PropertyInfo> properties;    // Properties.

        private BeanInfo(
                @Nonnull final Class<?> beanClass,
                @Nonnull final List<Ctor> ctors,
                @Nonnull final Set<PropertyInfo> properties) {
            assert beanClass != null;
            assert properties != null;
            this.beanClass = beanClass;
            final List<Ctor> copy = new ArrayList<>(ctors.size());
            copy.addAll(ctors);
            Collections.sort(copy);
            this.ctors = copy;
            this.properties = properties;

        }

        @SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
        private static class Ctor implements Comparable<Ctor> {
            @Nonnull
            private final Constructor<?> constructor;
            @Nonnull
            private final PropertyInfo[] constructorProperties;

            private Ctor(
                    @Nonnull final Constructor<?> constructor,
                    @Nonnull final PropertyInfo[] constructorProperties) {
                this.constructor = constructor;
                this.constructorProperties = constructorProperties;
            }

            @Override
            public int compareTo(@Nullable final Ctor o) {
                if ((o == null) || (constructorProperties.length > o.constructorProperties.length)) {
                    return -1;
                } else if (constructorProperties.length < o.constructorProperties.length) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // PropertyInfo class.
    // ------------------------------------------------------------------------

    private class PropertyInfo {
        @Nonnull
        private final String name;
        @Nonnull
        private final Method getter;
        @Nullable
        private final Method setter;
        private final boolean nullable;

        private PropertyInfo(
                @Nonnull final String name,
                @Nonnull final Method getter,
                @Nullable final Method setter) {
            assert name != null;
            assert getter != null;
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.nullable = true;
        }

        private PropertyInfo(
                @Nonnull final PropertyInfo propertyInfo,
                final boolean nullable) {
            assert propertyInfo != null;
            this.name = propertyInfo.name;
            this.getter = propertyInfo.getter;
            this.setter = propertyInfo.setter;
            this.nullable = nullable;
        }

        @Nonnull
        public Mapper getMapper() {
            return getOrCreateMapper(getter.getGenericReturnType());
        }
    }

    // ------------------------------------------------------------------------
    // Mapper interface.
    // ------------------------------------------------------------------------

    protected interface Mapper {

        /**
         * Convert a POJO to JSON value object.
         *
         * @param object      POJO to convert.
         * @param elementType Type of POJO.
         * @param seen        Identity map, needed to detect recursive data structures.
         * @return JSON mapped object, or null if the mapping was not possible for some reason.
         */
        @Nullable
        Object toJson(
                @Nullable Object object,
                @Nonnull final Type elementType,
                @Nonnull IdentityHashMap<Object, Object> seen);

        /**
         * Convert a JSON value object back to a POJO.
         *
         * @param jsonValue JSON value object.
         * @return (Newly instantiated) POJO, or null if the JSON object could not be mapped back to a POJO.
         */
        @Nullable
        Object fromJson(@Nullable Object jsonValue);

        /**
         * Return mapper name (for debugging).
         *
         * @return Mapper name.
         */
        @Nonnull
        String getName();
    }

    private abstract static class NamedMapper implements Mapper {
        @Nonnull
        private final String name;

        private NamedMapper(@Nonnull final String name) {
            assert name != null;
            this.name = name;
        }

        @Override
        @Nullable
        public abstract Object toJson(
                @Nullable Object object,
                @Nonnull final Type elementType,
                @Nonnull IdentityHashMap<Object, Object> seen);

        @Override
        @Nullable
        public abstract Object fromJson(
                @Nullable Object jsonValue);

        @Override
        @Nonnull
        public String getName() {
            return name;
        }
    }

    // ------------------------------------------------------------------------
    // Identity mapper (used to map BSON (e.g. MongoDB) object IDs).
    // ------------------------------------------------------------------------

    protected static class IdentityMapper extends NamedMapper {
        @Nonnull
        private final Class<?> rawType;

        public IdentityMapper(@Nonnull final Class<?> rawType) {
            super("IdentityMapper");
            assert rawType != null;
            this.rawType = rawType;
        }

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (rawType.isInstance(jsonValue)) {
                return jsonValue;
            }
            return null;
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            if (seen.put(object, object) != null) {
                return null;
            }

            if (rawType.isInstance(object)) {
                return object;
            }
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Bean info mapper (mapper for POJOs, which maps JSON types to JSON
    // objects and wraps non-JSON types to mapped objects (type/value pairs).
    // ------------------------------------------------------------------------

    @SuppressWarnings("UnnecessarilyQualifiedInnerClassAccess")
    private class BeanInfoMapper extends NamedMapper {
        @Nonnull
        private final BeanInfo beanInfo;

        private BeanInfoMapper(@Nonnull final BeanInfo beanInfo) {
            super("BeanInfoMapper");
            assert beanInfo != null;
            this.beanInfo = beanInfo;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            // List?
            if (jsonValue instanceof List) {
                final List<Object> values = new ArrayList<>();
                for (final Object jsonElem : (List) jsonValue) {
                    values.add(fromJson(jsonElem));
                }
                return values;
            }

            // Set?
            if (jsonValue instanceof Set) {
                final Set<Object> values = new LinkedHashSet<>();
                for (final Object jsonElem : (Set) jsonValue) {
                    values.add(fromJson(jsonElem));
                }
                return values;
            }

            // Bean?
            if (jsonValue instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> jsonObject = (Map<String, Object>) jsonValue;

                // Determine specific mapper.
                BeanInfo beanInfo = this.beanInfo;
                final Object className = jsonObject.get(CLASS_NAME_FIELD);
                if (className != null) {
                    try {

                        // Try to find a specific mapper for this bean class.
                        final Class<?> beanClass =
                                SimpleJsonSerializer.class.getClassLoader().loadClass(className.toString());
                        final Mapper mapper = getOrCreateMapper(beanClass);

                        if (mapper instanceof BeanInfoMapper) {
                            beanInfo = ((BeanInfoMapper) mapper).beanInfo;
                        } else {
                            if (jsonObject.containsKey(WRAPPER_VALUE_FIELD)) {
                                final Object value = jsonObject.get(WRAPPER_VALUE_FIELD);
                                return mapper.fromJson(value);
                            }
                        }
                    } catch (final ClassNotFoundException e) {
                        // Ignore, use mappers bean info.
                        LOG.error("fromJson: Error looking for className={}", className, e);
                    }
                }

                // Construct bean.
                Object bean = null;
                if (!beanInfo.ctors.isEmpty()) {

                    // Try all constructor until we have a matching one.
                    for (final BeanInfo.Ctor ctor : beanInfo.ctors) {

                        /**
                         * The value of tryThisConstructor is true if constructor is worth trying. The constructor is
                         * considered not worth trying if one of the parameters is null, where the actual parameter
                         * had been annotated with NotNull (or Nonnull).
                         */
                        boolean tryThisConstructor = true;

                        // Create the parameter list from the JSON object.
                        final Object[] parameters = new Object[ctor.constructorProperties.length];
                        for (int i = 0; i < parameters.length; ++i) {
                            final PropertyInfo property = ctor.constructorProperties[i];
                            final Mapper mapper = property.getMapper();
                            final Object paramValue = jsonObject.get(property.name);

                            /**
                             *  Check if the paramValue is null and the corresponding constructors parameter is annotated to be not null.
                             *  In that case we need to try to drop the parameter altogether and find a constructor that does not
                             *  require this parameter.
                             */
                            if ((paramValue == null) && !property.nullable) {
                                LOG.debug("fromJson: Skipping constructor {} because not-null arg #{} is null)...",
                                        ctor.constructor, i + 1);
                                tryThisConstructor = false;
                                break;
                            }

                            parameters[i] = mapper.fromJson(paramValue);
                        }

                        // Try to instantiate the POJO.
                        if (tryThisConstructor) {
                            try {
                                bean = ctor.constructor.newInstance(parameters);
                                return bean;
                            } catch (final InvocationTargetException ignored) {

                                /**
                                 * This exception occurs if a null argument is passed to a non-null.
                                 * We try to not do that, but the default NotNull annotation is simply
                                 * not available runtime so we need to try, fail and retry.
                                 */
                                LOG.trace("fromJson: Constructor invocation failed {}({} args), retry with next ctor",
                                        ctor.constructor, ctor.constructorProperties.length);
                                // Ignore, try next constructor.
                            } catch (final IllegalArgumentException ignored) {

                                /**
                                 * This exception might occur if a "wrong" constructor was selected in case
                                 * there are multiple constructors with the same number of parameters
                                 * but different types.
                                 *
                                 * For example: having a constructor which takes a Collection and another
                                 * one which takes a vararg[] list. Trying to apply the vararg[] constructor
                                 * will fail with an IllegalArgumentException if a Collection is passed to it.
                                 *
                                 * It might also occur if the argument had an incorrect format, which is a real
                                 * error and should be logged as such. This error is not logged in here, but below
                                 * in the "bean == null" case.
                                 */
                                LOG.trace(
                                        "fromJson: Illegal arguments in constructor {} ({} args) of class {} with parameters: {}, retry with next ctor",
                                        ctor.constructor, ctor.constructorProperties.length, beanInfo.beanClass.getName(),
                                        Json.toStringJson(parameters));
                                // Ignore, try next constructor.
                            } catch (final InstantiationException e) {
                                LOG.error(
                                        "fromJson: Cannot use constructor " + ctor.constructor +
                                                '(' + ctor.constructorProperties.length + " args) " +
                                                " to instantiate class " + beanInfo.beanClass.getName() +
                                                " with parameters: " + Json.toStringJson(parameters), e);
                                // Ignore, try next constructor.
                            } catch (final IllegalAccessException e) {
                                LOG.error(
                                        "fromJson: Cannot access constructor " + ctor.constructor +
                                                '(' + ctor.constructorProperties.length + " args) " +
                                                " of class " + beanInfo.beanClass.getName() +
                                                " with parameters: " + Json.toStringJson(parameters), e);
                                // Ignore, try next constructor.
                            }
                        }
                    }
                }

                // Constructor instantiation failed, try the default constructor instead, and use setters.
                if (bean == null) {
                    try {
                        final Constructor<?> defaultConstructor = beanInfo.beanClass.getConstructor();
                        defaultConstructor.setAccessible(true);
                        bean = defaultConstructor.newInstance();
                    } catch (final InvocationTargetException e) {
                        LOG.error("fromJson: Cannot invoke default constructor of class {} without parameters",
                                beanInfo.beanClass.getName(), e);
                        return null;
                    } catch (final NoSuchMethodException e) {
                        LOG.debug("fromJson: Default constructor without parameters is not defined for class {}, " +
                                "examining special cases...");

                        // TODO: Fix serializer: no handling of maps yet.
                        // Special case: Check if this is a Google Guava ImmutableMap.
                        if (ImmutableMap.class.isAssignableFrom(beanInfo.beanClass)) {
                            LOG.warn("fromJson: ImmutableMap not supported, class={}",
                                    beanInfo.beanClass.getName());
                            return null;
                        } else {
                            LOG.error("fromJson: Default constructor without parameters is not defined for class {}",
                                    beanInfo.beanClass.getName(), e);
                            return null;
                        }
                    } catch (final InstantiationException e) {
                        LOG.error("fromJson: Cannot instantiate class {} using default constructor without parameters",
                                beanInfo.beanClass.getName(), e);
                        return null;
                    } catch (final IllegalAccessException e) {
                        LOG.error("fromJson: Cannot access default constructor without parameters for class {}",
                                beanInfo.beanClass.getName(), e);
                        return null;
                    }
                }

                // Set all properties using setters.
                for (final PropertyInfo property : beanInfo.properties) {
                    if (property.setter != null) {
                        try {
                            final Object parameter = property.getMapper().fromJson(jsonObject.get(property.name));
                            property.setter.invoke(bean, parameter);
                        } catch (final InvocationTargetException e) {
                            LOG.error("fromJson: Cannot invoke setter: " + property.setter.getName() + " for class " +
                                    beanInfo.beanClass.getName(), e);
                        } catch (final IllegalAccessException e) {
                            LOG.error("fromJson: Cannot access setter: " + property.setter.getName() + " for class " +
                                    beanInfo.beanClass.getName(), e);
                        }
                    }
                }

                // All OK now, return the bean.
                return bean;
            }

            /**
             * No discriminator, Object type. Check if we can use some specific mappers.
             */
            if (beanInfo.beanClass.isAssignableFrom(Character.class) && (jsonValue instanceof Character)) {
                return charMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(String.class) && (jsonValue instanceof String)) {
                return stringMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Boolean.class) && (jsonValue instanceof Boolean)) {
                return booleanMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Integer.class) && (jsonValue instanceof Integer)) {
                return intMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Long.class) && (jsonValue instanceof Long)) {
                return longMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Short.class) && (jsonValue instanceof Short)) {
                return shortMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Byte.class) && (jsonValue instanceof Byte)) {
                return byteMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Float.class) && (jsonValue instanceof Float)) {
                return floatMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Double.class) && (jsonValue instanceof Double)) {
                return doubleMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Date.class) && (jsonValue instanceof Date)) {
                return dateTimeMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Currency.class) && (jsonValue instanceof Currency)) {
                return currencyMapper.fromJson(jsonValue);
            }

            if (beanInfo.beanClass.isAssignableFrom(Uid.class) && (jsonValue instanceof Uid)) {
                return uidMapper.fromJson(jsonValue);
            }

            LOG.error("fromJson: Cannot deserialize JSON value, jsonValue={}", Json.toStringJson(jsonValue));
            return null;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // Cycle detection.
            if (seen.containsKey(object)) {
                return seen.get(object);
            }

            // Store null for now - overwrite later.
            seen.put(object, null);

            // List?
            if (object instanceof List) {
                final List<Object> result = new ArrayList<>();
                for (final Object o : (List) object) {
                    final Object json = toJson(o, elementType, seen);
                    result.add(json);
                }
                seen.put(object, result);
                return result;
            }

            // TODO: Fix serializer. Support maps.
            // Map?
            if (object instanceof Map) {
                LOG.error("toJson: Maps are not supported yet");
                return null;
            }

            // Set?
            if (object instanceof Set) {
                final Map<String, Object> result = createJsonMappedObject();
                result.put(CLASS_NAME_FIELD, object.getClass().getName());
                final List<Object> values = new ArrayList<>();
                for (final Object o : (Set) object) {
                    values.add(toJson(o, elementType, seen));
                }
                result.put(WRAPPER_VALUE_FIELD, values);
                seen.put(object, result);
                return result;
            }

            // TODO: Fix serializer. Support unmodifiable collections.
            // Unmodifiable collection?
            if ((object instanceof Collection) &&
                    elementType.toString().equals("class java.util.Collections$UnmodifiableCollection")) {
                LOG.error("toJson: Unmodifiable maps are not supported yet");
                return null;
            }

            // Determine specific mapper.
            BeanInfo beanInfo = this.beanInfo;
            final Mapper mapper = getOrCreateMapper(object.getClass());
            if (mapper instanceof BeanInfoMapper) {
                beanInfo = ((BeanInfoMapper) mapper).beanInfo;
            }

            // Bean type.
            final Map<String, Object> result = createJsonMappedObject();
            for (final PropertyInfo property : beanInfo.properties) {
                try {
                    final Object value = property.getter.invoke(object);
                    final Mapper valueMapper = property.getMapper();
                    final Class<?> returnType = property.getter.getReturnType();
                    final Object jsonValue = valueMapper.toJson(value, returnType, seen);

                    // Skip null values.
                    if (jsonValue != null) {
                        result.put(property.name, jsonValue);
                    }
                } catch (final Exception e) {
                    LOG.error("toJson: Couldn't get property " + property.name, e);
                    // Ignore this property.
                }
            }

            // TODO: Fix for anonymous classes.
            result.put(CLASS_NAME_FIELD, object.getClass().getName());
            seen.put(object, result);
            return result;
        }
    }

    // ------------------------------------------------------------------------
    // Collection mapper.
    // ------------------------------------------------------------------------

    private class CollectionMapper extends NamedMapper {
        @Nonnull
        protected final Class<?> rawType;
        @Nonnull
        protected final Type elementType;

        public CollectionMapper(@Nonnull final Type elementType) {
            super("CollectionMapper");
            assert elementType != null;

            if (elementType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = ((ParameterizedType) elementType);
                rawType = (Class<?>) parameterizedType.getRawType();
                this.elementType = parameterizedType.getActualTypeArguments()[0];
            } else if (elementType instanceof Class) {
                rawType = (Class<?>) elementType;
                this.elementType = Object.class;
            } else {
                rawType = List.class;
                this.elementType = Object.class;
            }
        }

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {
            final Collection<Object> result;

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (Set.class.isAssignableFrom(rawType)) {
                result = new LinkedHashSet<>();
            } else {
                result = new ArrayList<>();
            }

            final Mapper elementMapper = getOrCreateMapper(elementType);
            if (jsonValue instanceof Collection) {

                // Read the collection.
                for (final Object jsonElt : (Collection) jsonValue) {
                    if (jsonElt == null) {
                        result.add(null);
                    } else {
                        final Object elem = elementMapper.fromJson(jsonElt);
                        if (elem != null) {
                            result.add(elem);
                        }
                    }
                }
            } else if (jsonValue instanceof Map) {

                // Don't read a wrapped object, bail out.
                if (((Map) jsonValue).containsKey(CLASS_NAME_FIELD)) {
                    return null;
                }
            } else {
                final Object elem = elementMapper.fromJson(jsonValue);
                if (elem != null) {
                    result.add(elem);
                }
            }
            return result;
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // Cycle detection.
            if (seen.containsKey(object)) {
                return seen.get(object);
            }

            // Store null for now - overwrite later.
            seen.put(object, null);

            final List<Object> jsonValue = new ArrayList<>();
            if (object instanceof Collection) {
                for (final Object elem : (Collection) object) {
                    if (elem == null) {
                        jsonValue.add(null);
                    } else {
                        final Mapper elemMapper = getOrCreateMapper(elem.getClass());
                        final Object jsonElem = elemMapper.toJson(elem, elem.getClass(), seen);
                        if (jsonElem != null) {
                            jsonValue.add(jsonElem);
                        }
                    }
                }
            }
            seen.put(object, jsonValue);
            return jsonValue;
        }
    }

    // ------------------------------------------------------------------------
    // Wrapped collection mapper.
    // ------------------------------------------------------------------------

    private class WrappedCollectionMapper extends CollectionMapper {

        public WrappedCollectionMapper(@Nonnull final Type elementType) {
            super(elementType);
            assert elementType != null;
        }

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            final Object result = super.fromJson(jsonValue);
            if (result != null) {
                return result;
            }
            return getOrCreateMapper(Object.class).fromJson(jsonValue);
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // Cycle detection.
            if (seen.containsKey(object)) {
                return seen.get(object);
            }

            // Store null for now - overwrite later.
            seen.put(object, null);

            final Map<String, Object> jsonObject = createJsonMappedObject();
            jsonObject.put(CLASS_NAME_FIELD, object.getClass().getName());

            final List<Object> jsonValue = new ArrayList<>();
            if (object instanceof Collection) {
                for (final Object elem : (Collection) object) {
                    if (elem == null) {
                        jsonValue.add(null);
                    } else {
                        final Mapper elemMapper = getOrCreateMapper(elem.getClass());
                        final Object jsonElem = elemMapper.toJson(elem, elem.getClass(), seen);
                        if (jsonElem != null) {
                            jsonValue.add(jsonElem);
                        }
                    }
                }
            }
            jsonObject.put(WRAPPER_VALUE_FIELD, jsonValue);
            seen.put(object, jsonObject);
            return jsonObject;
        }
    }

    // ------------------------------------------------------------------------
    // Array mapper.
    // ------------------------------------------------------------------------

    private class ArrayMapper extends CollectionMapper {

        public ArrayMapper(@Nonnull final Type type) {
            super(type);
            assert type != null;
        }

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            final Collection<?> collection = (Collection) super.fromJson(jsonValue);
            if (collection == null) {
                return null;
            }

            return collection.toArray((Object[]) Array.newInstance((Class<?>) elementType, collection.size()));
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // Cycle detection done in super.toJson().
            final List<Object> list = Arrays.asList((Object[]) object);
            return super.toJson(list, elementType, seen);
        }
    }

    // ------------------------------------------------------------------------
    // Enum mapper.
    // ------------------------------------------------------------------------

    private class EnumMapper<T extends Enum<T>> extends NamedMapper {
        @Nonnull
        private final Class<?> enumType;

        public EnumMapper(@Nonnull final Class<?> enumType) {
            super("EnumMapper");
            assert enumType != null;
            this.enumType = enumType;
        }

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof String) {
                //noinspection unchecked
                return Enum.valueOf((Class<T>) enumType, (String) jsonValue);
            }

            return getOrCreateMapper(Object.class).fromJson(jsonValue);
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // No cycle detection required.
            return createWrappedValue(object.getClass(), object);
        }
    }

    // ------------------------------------------------------------------------
    // JodaTime mapper.
    // ------------------------------------------------------------------------

    @Nonnull
    private final Mapper dateTimeMapper = new NamedMapper("DateTimeMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Date) {
                return UTCTime.from(new DateTime(((Date) jsonValue).getTime()));
            }
            return null;
        }

        /**
         * Method escapes a DateTime object into a JSON string.
         *
         * @param object The object to convert. When the object is a DateTime, its timezone will be converted to UTC.
         * @param elementType The type of the object.
         * @param seen Cache.
         * @return the JSON representation.
         */
        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // No cycle detection required.
            if (object instanceof DateTime) {
                final DateTime objectAsDateTime = ((DateTime) object);

                // Convert date/time to UTC if needed.
                if (!objectAsDateTime.getZone().equals(DateTimeZone.UTC)) {
                    return objectAsDateTime.toDateTime(DateTimeZone.UTC).toDate();
                }
                return objectAsDateTime.toDate();
            }
            return null;
        }
    };

    // ------------------------------------------------------------------------
    // Number mappers (int, long, short, byte, float, double).
    // ------------------------------------------------------------------------

    private abstract class NumberMapper extends NamedMapper {

        protected NumberMapper(@Nonnull final String name) {
            super(name);
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            if (object == null) {
                return null;
            }

            // No cycle detection required.
            if (Number.class.isAssignableFrom(getBoxedType(elementType))) {
                return object;
            }
            return createWrappedValue(object.getClass(), object);
        }
    }

    private final Mapper intMapper = new NumberMapper("IntegerMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Number) {
                return ((Number) jsonValue).intValue();
            }
            if (jsonValue instanceof String) {
                return Integer.parseInt((String) jsonValue);
            }
            return null;
        }
    };

    private final Mapper longMapper = new NumberMapper("LongMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Number) {
                return ((Number) jsonValue).longValue();
            }
            if (jsonValue instanceof String) {
                return Long.parseLong((String) jsonValue);
            }
            return null;
        }
    };

    private final Mapper shortMapper = new NumberMapper("ShortMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Number) {
                return ((Number) jsonValue).shortValue();
            }
            if (jsonValue instanceof String) {
                return Short.parseShort((String) jsonValue);
            }
            return null;
        }
    };

    private final Mapper byteMapper = new NumberMapper("ByteMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Number) {
                return ((Number) jsonValue).byteValue();
            }
            if (jsonValue instanceof String) {
                return Byte.parseByte((String) jsonValue);
            }
            return null;
        }
    };

    private final Mapper floatMapper = new NumberMapper("FloatMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Number) {
                return ((Number) jsonValue).floatValue();
            }
            if (jsonValue instanceof String) {
                return Float.parseFloat((String) jsonValue);
            }
            return null;
        }
    };

    private final Mapper doubleMapper = new NumberMapper("DoubleMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Number) {
                return ((Number) jsonValue).doubleValue();
            }
            if (jsonValue instanceof String) {
                return Double.parseDouble((String) jsonValue);
            }
            return null;
        }
    };

    // ------------------------------------------------------------------------
    // Character mapper.
    // ------------------------------------------------------------------------

    private final Mapper charMapper = new NamedMapper("CharacterMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Character) {
                return jsonValue;
            }

            if (jsonValue instanceof String) {

                // When a String is received, convert only the first character from it.
                final String jsonAsString = (String) jsonValue;
                if (!jsonAsString.isEmpty()) {
                    return jsonAsString.charAt(0);
                } else {
                    return null;
                }
            }
            return null;
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // No cycle detection required.
            if (object instanceof Character) {
                return object;
            }
            return null;
        }
    };

    // ------------------------------------------------------------------------
    // String mapper.
    // ------------------------------------------------------------------------

    private final Mapper stringMapper = new NamedMapper("StringMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            return jsonValue.toString();
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // No cycle detection required.
            return object.toString();
        }
    };

    // ------------------------------------------------------------------------
    // Boolean mapper.
    // ------------------------------------------------------------------------

    private final Mapper booleanMapper = new NamedMapper("BooleanMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof Boolean) {
                return jsonValue;
            }
            if (jsonValue instanceof String) {
                return Boolean.parseBoolean((String) jsonValue);
            }
            return null;
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // No cycle detection required.
            //noinspection EqualsBetweenInconvertibleTypes
            if (elementType.equals(Boolean.class) || elementType.equals(Boolean.TYPE)) {
                return object;
            }
            return createWrappedValue(object.getClass(), object);
        }
    };

    // ------------------------------------------------------------------------
    // Currency mapper.
    // ------------------------------------------------------------------------

    private final Mapper currencyMapper = new NamedMapper("CurrencyMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof String) {
                return Currency.getInstance((String) jsonValue);
            }

            return getOrCreateMapper(Object.class).fromJson(jsonValue);
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // No cycle detection required.
            return createWrappedValue(object.getClass(), object.toString());
        }
    };

    // ------------------------------------------------------------------------
    // Uid mapper.
    // ------------------------------------------------------------------------

    private final Mapper uidMapper = new NamedMapper("UidMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            if (jsonValue instanceof String) {
                return Uid.fromString((String) jsonValue);
            }

            return getOrCreateMapper(Object.class).fromJson(jsonValue);
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            return createWrappedValue(Uid.class, object.toString());
        }
    };

    // ------------------------------------------------------------------------
    // Class mapper.
    // ------------------------------------------------------------------------

    private final Mapper classMapper = new NamedMapper("ClassMapper") {

        @Override
        @Nullable
        public Object fromJson(@Nullable final Object jsonValue) {

            // Shortcut.
            if (jsonValue == null) {
                return null;
            }

            try {
                return Class.forName(jsonValue.toString());
            } catch (final ClassNotFoundException ignored) {
                return null;
            }
        }

        @Override
        @Nullable
        public Object toJson(
                @Nullable final Object object,
                @Nonnull final Type elementType,
                @Nonnull final IdentityHashMap<Object, Object> seen) {
            assert elementType != null;
            assert seen != null;

            // Shortcut.
            if (object == null) {
                return null;
            }

            // No cycle detection required.
            if (object instanceof Class) {
                return ((Class) object).getName();
            }
            return null;
        }
    };
}
