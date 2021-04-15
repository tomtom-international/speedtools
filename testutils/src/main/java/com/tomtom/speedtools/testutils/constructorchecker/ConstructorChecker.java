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

package com.tomtom.speedtools.testutils.constructorchecker;

import com.tomtom.speedtools.testutils.ValidationFailException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.mockito.Mockito.mock;

/**
 * This class automates the validation of the contract of constructors and methods regarding the nullity check of
 * parameters. The contract that is being validated is that if any parameter (nullable parameters must be explicitly
 * indicated) is null an {@code AssertionError} or an {@code IllegalArgumentException} is thrown.  <p>All parameters
 * are assumed to be checked for nullity. If that isn't so, the indexes of the nullable parameters has to be given in a
 * array of integers.
 *
 * The strategy used is the following:
 *
 * When the number of passed parameters match the number of constructor arguments (types) rely on the order. Else <UL>
 * <LI> A basic parameter array of not null instances is constructed.</LI> <LI> A sanity check invocation is done with
 * the basic parameter array to ensure those instances don't cause exceptions.</LI> <LI> Then for each not nullable
 * parameter an invocation is done with the basic parameter array with one of the instances replaced by null. If
 * expected exceptions are not thrown, the fail is reported via a ValidationFailException. </LI> </UL> When the
 * constructor has more than one parameter that takes a collection this later strategy is error prone.
 *
 * If no matching constructor is found, this is treated as a {@link ValidationFailException}.
 */
@SuppressWarnings("SSBasedInspection")
public final class ConstructorChecker {
    private static final Logger LOG = LoggerFactory.getLogger(ConstructorChecker.class);

    /**
     * Map of instances that can used as parameters in case mocking is not possible or desirable.
     */
    private static final Map<Class<?>, Object> instanceMap = new HashMap<>();

    static {
        instanceMap.put(Collection.class, Collections.emptyList());
        instanceMap.put(Duration.class, new Duration(1000));
        instanceMap.put(DateTime.class, DateTime.now().withZone(DateTimeZone.UTC));
    }

    private ConstructorChecker() {
        // Prevent instantiation.
    }

    /**
     * Validates the constructor contract.
     *
     * @param objectClass        A class with a single public constructor.
     * @param parameterInstances Optional instances to be used as parameters of the constructor.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    public static void validateConstructor(
            @Nonnull final Class<?> objectClass,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert objectClass != null;
        assert parameterInstances != null;
        validateConstructor(getConstructorFromClass(objectClass, parameterInstances), new int[0], parameterInstances);
    }

    /**
     * Validates the constructor contract.
     *
     * @param constructor        The constructor to be validated.
     * @param parameterInstances Optional instances to be used as parameters of the constructor.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    public static void validateConstructor(
            @Nonnull final Constructor<?> constructor,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert constructor != null;
        assert parameterInstances != null;
        validateConstructor(constructor, new int[0], parameterInstances);
    }

    /**
     * Validates the constructor contract.
     *
     * @param objectClass              A class with a single public constructor.
     * @param nullableParameterIndexes The indexes of the nullable parameters.
     * @param parameterInstances       Optional instances to be used as parameters of the constructor.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    public static void validateConstructor(
            @Nonnull final Class<?> objectClass,
            @Nonnull final int[] nullableParameterIndexes,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert objectClass != null;
        assert nullableParameterIndexes != null;
        assert parameterInstances != null;
        validateConstructor(getConstructorFromClass(objectClass, parameterInstances), nullableParameterIndexes,
                parameterInstances);
    }

    /**
     * Validates the constructor contract.
     *
     * @param constructor              The constructor to be validated.
     * @param nullableParameterIndexes The indexes of the nullable parameters.
     * @param parameterInstances       Optional instances to be used as parameters of the constructor.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    public static void validateConstructor(
            @Nonnull final Constructor<?> constructor,
            @Nonnull final int[] nullableParameterIndexes,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert constructor != null;
        assert nullableParameterIndexes != null;
        assert parameterInstances != null;

        final Invokable invokable = new Invokable() {

            /**
             * Return a list of parameter types.
             *
             * @return List of parameter types.
             */
            @Override
            @Nonnull
            public Class<?>[] getParameterTypes() {
                return constructor.getParameterTypes();
            }

            /**
             * Call constructor.
             *
             * @param args Array of objects to be passed as arguments to the invocation; values of primitive types are
             *             wrapped in a wrapper object of the appropriate type (e.g. a <tt>float</tt> in a {@link
             *             Float Float})
             * @return Return value, or null if void.
             * @throws IllegalAccessException Illegal access.
             * @throws IllegalArgumentException Illegal argument.
             * @throws InvocationTargetException Can't invoke target.
             */
            @Override
            @Nullable
            public Object invoke(
                    @Nonnull final Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                assert args != null;
                try {
                    return constructor.newInstance(args);
                } catch (final InstantiationException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };
        validateInvokable(constructor.getName(), invokable, nullableParameterIndexes, parameterInstances);
    }

    /**
     * Validates the method contract.
     *
     * @param objectClass        The class where the method is declared.
     * @param methodName         The name of the method to be validated.
     * @param parameterInstances Optional instances to be used as parameters of the method.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    public static void validateMethod(
            @Nonnull final Class<?> objectClass,
            @Nonnull final String methodName,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert objectClass != null;
        assert methodName != null;
        assert parameterInstances != null;

        final Method[] methods = objectClass.getDeclaredMethods();
        Method foundMethod = null;
        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (foundMethod == null) {
                    foundMethod = method;
                } else {
                    throw new ValidationFailException("Found more than one method named " + methodName);
                }
            }
        }
        if (foundMethod == null) {
            throw new ValidationFailException("Can't find method named " + methodName);
        } else {
            validateMethod(null, foundMethod, new int[0], parameterInstances);
        }
    }

    /**
     * Validates the method contract.
     *
     * @param instance           The instance upon which to validate the method invocation. Can be null for static
     *                           methods.
     * @param method             The method to be validated.
     * @param parameterInstances Optional instances to be used as parameters of the method.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    public static void validateMethod(
            @Nullable final Object instance,
            @Nonnull final Method method,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert method != null;
        assert parameterInstances != null;
        validateMethod(instance, method, new int[0], parameterInstances);
    }

    /**
     * Validates the method contract.
     *
     * @param instance                 The instance upon which to validate the method invocation. Can be null for static
     *                                 methods.
     * @param method                   The method to be validated.
     * @param nullableParameterIndexes The indexes of the nullable parameters.
     * @param parameterInstances       Optional instances to be used as parameters of the method.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    public static void validateMethod(
            @Nullable final Object instance,
            @Nonnull final Method method,
            @Nonnull final int[] nullableParameterIndexes,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert method != null;
        assert nullableParameterIndexes != null;
        assert parameterInstances != null;

        final Invokable invokable = new Invokable() {

            /**
             * Return a list of parameter types.
             *
             * @return List of parameter types.
             */
            @Override
            @Nonnull
            public Class<?>[] getParameterTypes() {
                return method.getParameterTypes();
            }

            /**
             * Call method.
             *
             * @param args Array of objects to be passed as arguments to the invocation; values of primitive types are
             *             wrapped in a wrapper object of the appropriate type (e.g. a <tt>float</tt> in a {@link
             *             Float Float})
             * @return Return value or null if void.
             * @throws IllegalAccessException Illegal access.
             * @throws IllegalArgumentException Illegal argument.
             * @throws InvocationTargetException Can't invoke target.
             */
            @Override
            @Nullable
            public Object invoke(@Nonnull final Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                assert args != null;
                return method.invoke(instance, args);
            }
        };
        validateInvokable(method.getName(), invokable, nullableParameterIndexes, parameterInstances);
    }

    /**
     * Validates the invokable contract.
     *
     * @param invokable                The invokable to be validated.
     * @param nullableParameterIndexes The indexes of the nullable parameters.
     * @param parameterInstances       Optional instances to be used as parameters of the invocation.
     * @throws ValidationFailException If unexpected behaviour is found. Check the class documentation for details.
     */
    private static void validateInvokable(
            @Nonnull final String methodName,
            @Nonnull final Invokable invokable,
            @Nonnull final int[] nullableParameterIndexes,
            @Nonnull final Object... parameterInstances) throws ValidationFailException {
        assert methodName != null;
        assert invokable != null;
        assert nullableParameterIndexes != null;
        assert parameterInstances != null;

        final Class<?>[] parameterTypes = invokable.getParameterTypes();
        final Object[] notNullparameters = generateNotNullParameters(methodName, parameterTypes, parameterInstances);

        LOG.debug("Checking {} @Nonnull and {} @Nullable parameters for {}",
                notNullparameters.length - nullableParameterIndexes.length,
                nullableParameterIndexes.length, methodName);
        if (throwsAnyException(invokable, notNullparameters)) {
            throw new ValidationFailException("@Nonnull parameter invocation failed");
        }

        Arrays.sort(nullableParameterIndexes);
        for (int parameterIndex = 0; parameterIndex < parameterTypes.length; ++parameterIndex) {
            if (Arrays.binarySearch(nullableParameterIndexes, parameterIndex) >= 0) {
                continue;
            }
            final Object[] parametersClone = notNullparameters.clone();
            parametersClone[parameterIndex] = null;
            if (!throwsExceptedException(invokable, parametersClone)) {
                throw new ValidationFailException("@Nonnull " +
                        parameterTypes[parameterIndex].getSimpleName() +
                        " parameter " + (parameterIndex + 1) +
                        " (from " + parameterTypes.length + ") accepts null");
            }
        }
    }

    /**
     * Checks if the given invocation throws any exception.
     *
     * @param invokable  The invocation to be executed.
     * @param parameters The parameters to be used in the invocation.
     * @return True if an exception is throw by the invocation. False if no exception is thrown.
     */
    private static boolean throwsAnyException(
            @Nonnull final Invokable invokable,
            @Nonnull final Object[] parameters) {
        assert invokable != null;
        assert parameters != null;

        try {
            invokable.invoke(parameters);
        } catch (final Exception e) {
            LOG.error("Exception: message=" + e.getMessage(), e);
            return true;
        }
        return false;
    }

    /**
     * Checks if the given invocation throws an exception consistent with a null check.
     *
     * @param invokable  The invocation to be executed.
     * @param parameters The parameters to be used in the invocation.
     * @return True if an expected exception is throw by the invocation. False if any other or no exception is thrown.
     */
    @SuppressWarnings("ErrorNotRethrown")
    private static boolean throwsExceptedException(
            @Nonnull final Invokable invokable,
            @Nonnull final Object[] parameters) {
        assert invokable != null;
        assert parameters != null;

        try {
            //noinspection NestedTryStatement
            try {
                invokable.invoke(parameters);
            } catch (final InvocationTargetException e) {
                //noinspection ProhibitedExceptionThrown,ThrowCaughtLocally
                throw e.getTargetException();
            }
        } catch (final IllegalArgumentException | AssertionError ignored) {
            return true;
        } catch (final Throwable e) {
            LOG.error("Exception: {}", e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Generates an array of object instances. The class of the instance is determined by the array of classes given as
     * input. One instance will be generated for each element of the classes array.
     *
     * @param parameterTypes     Defined the expected instance class for each element of the resulting array.
     * @param parameterInstances Optional instances to be used in populating the resulting array.
     * @return An array of object instances.
     */
    @Nonnull
    private static Object[] generateNotNullParameters(
            @Nonnull final String name,
            @Nonnull final Class<?>[] parameterTypes,
            @Nonnull final Object... parameterInstances) {
        assert parameterInstances != null;
        assert parameterInstances != null;
        assert name != null;

        LOG.debug("Creating {} non-null parameters for: {}", parameterTypes.length, name);
        final Object[] notNullParameters = new Object[parameterTypes.length];
        for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
            final Class<?> parameterType = parameterTypes[parameterIndex];
            LOG.debug("  Parameter {}: {}", parameterIndex, parameterType);

            // When the number of parameters match the number of constructor arguments (types) rely on the order.
            // Else do a look up that will find the first match. When the constructor has more than one parameter that
            // takes a collection this is error prone.
            if (parameterInstances.length == parameterTypes.length) {
                notNullParameters[parameterIndex] = parameterInstances[parameterIndex];
            } else {
                notNullParameters[parameterIndex] = createInstance(parameterType, parameterInstances);
            }
        }
        return notNullParameters;
    }

    /**
     * Creates an instance of a class given as parameter. It can do so (in order of preference) by obtaining from the
     * instances array,by invoking the default constructor of the class or by mocking.
     *
     * @param instanceType The class of the instance to return.
     * @param instances    Optional instances that might of the required class.
     * @return An instance of a class given as parameter
     */
    @Nonnull
    private static Object createInstance(
            @Nonnull final Class<?> instanceType,
            @Nonnull final Object... instances) {
        assert instanceType != null;
        assert instances != null;

        // Check if correct instance example was passed now.
        final Object instance = getInstanceFromArray(instanceType, instances);
        if (instance != null) {
            return instance;
        }

        // Create a new instance.
        try {
            //noinspection ClassNewInstance
            return instanceType.newInstance();
        } catch (final InstantiationException ignored) {
            LOG.debug("Cannot instantiate: {}", instanceType.getSimpleName());
        } catch (final IllegalAccessException ignored) {
            LOG.debug("Cannot access constructor for: {}", instanceType.getSimpleName());
        }

        // Check if correct instance example is stored in own cache.
        final Object cachedInstance = instanceMap.get(instanceType);
        if (cachedInstance != null) {
            return cachedInstance;
        }
        return mock(instanceType);
    }

    /**
     * Looks for an instance compatible with the given type in the given instances array.
     *
     * @param instanceType The class of the instance to return.
     * @param instances    Instances that might be compatible with the required type.
     * @return The first instance of the array that is compatible with the required type. Null is no such instance is
     * found.
     */
    @Nullable
    private static Object getInstanceFromArray(
            @Nonnull final Class<?> instanceType,
            @Nonnull final Object[] instances) {
        assert instanceType != null;
        assert instances != null;

        for (final Object instance : instances) {
            if (instanceType.isInstance(instance)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Interface that allows to abstract from the concrete nature of the invocation.
     */
    private interface Invokable {

        /**
         * Returns an array of {@code Class} objects that represent the formal parameter types, in declaration order, of
         * the invokable. Returns an array of length 0 if the underlying constructor takes no parameters.
         *
         * @return The parameter types for the invokable.
         */
        @Nonnull
        Class<?>[] getParameterTypes();

        /**
         * Calls the invokable represented by this object. Individual parameters are automatically unwrapped to match
         * primitive formal parameters, and both primitive and reference parameters are subject to method invocation
         * conversions as necessary.
         *
         * <p>If the number of formal parameters required by the underlying constructor is 0, the supplied {@code args}
         * array may be of length 0 or null.
         *
         * @param args array of objects to be passed as arguments to the invocation; values of primitive types are
         *             wrapped in a wrapper object of the appropriate type (e.g. a <tt>float</tt> in a {@link Float
         *             Float})
         * @return A new object returned by calling the invokable, or null if void.
         * @throws IllegalAccessException      If this invokable object enforces Java language access control and the
         *                                     underlying constructor is inaccessible.
         * @throws IllegalArgumentException    If the number of actual and formal parameters differ; if an unwrapping
         *                                     conversion for primitive arguments fails; or if, after possible
         *                                     unwrapping, a parameter value cannot be converted to the corresponding
         *                                     formal parameter type by a method invocation conversion; if this
         *                                     constructor pertains to an enum type.
         * @throws InvocationTargetException   If the underlying invokable throws an exception.
         * @throws ExceptionInInitializerError If the initialization provoked by this method fails.
         */
        @Nullable
        public Object invoke(@Nonnull Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    }

    /**
     * Returns the public constructor for the given class.
     *
     * @param objectClass        A class with a single public constructor.
     * @param parameterInstances Instances to match constructor arguments with.
     * @return The public constructor for the given class.
     */
    @Nonnull
    private static Constructor<?> getConstructorFromClass(@Nonnull final Class<?> objectClass,
                                                          @Nonnull final Object[] parameterInstances) throws ValidationFailException {
        assert objectClass != null;
        assert parameterInstances != null;

        final Constructor<?>[] constructors = objectClass.getConstructors();

        // Find public ctor with most arguments.
        int maxLen = 0;
        int index = -1;
        for (int i = 0; i < constructors.length; ++i) {
            final Constructor<?> ctor = constructors[i];
            if (Modifier.isPublic(ctor.getModifiers())) {

                // Constructor should have at least the number of parameters from instances.
                if (ctor.getParameterTypes().length >= parameterInstances.length) {

                    // Validate all parameter instance types.
                    boolean allValid = true;
                    for (int j = 0; j < parameterInstances.length; j++) {
                        if (!ctor.getParameterTypes()[j].isInstance(parameterInstances[j])) {
                            allValid = false;
                            break;
                        }
                    }
                    if (allValid) {
                        final int len = ctor.getParameterTypes().length;
                        if (maxLen < len) {
                            maxLen = len;
                            index = i;
                        }
                    }
                }
            }
        }

        if (index == -1) {
            throw new ValidationFailException(
                    "No constructor found in " + objectClass.getName() + " with at least " + parameterInstances.length +
                            " matching parameters");
        }

        assert index >= 0;
        return constructors[index];
    }
}
