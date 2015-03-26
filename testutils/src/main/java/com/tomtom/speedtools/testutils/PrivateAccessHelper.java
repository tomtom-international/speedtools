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

package com.tomtom.speedtools.testutils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The static methods provided by this class are to be used to access private or private static methods of classes under
 * test.
 *
 * Note that you should think twice before testing private methods! Prefer to test private methods via non-private
 * methods of their class. Also, think about whether those private methods wouldn't be better suited to be part of a new
 * class as non-privates, which can then be unit tested as usual.
 *
 * However, if the private methods to test are logically part of their current class and factoring them out into a
 * separate class would spread logic over two classes and hence reduce cohesiveness, then you may consider testing them
 * with the help of the functionality provided here.
 *
 * Care must be applied if your test catches overly broad exceptions, e.g. by using {@code @Test(expected =
 * Throwable.class)}. If the class under test is refactored, and the method name or signature is not adapted in the
 * test, then this may go undetected, because the helper methods in this class will throw a {@link PrivateAccessFailure
 * PrivateAccessFailure} in case the target method could not be invoked. However, if your test expects a sub-type of
 * {@link Throwable}, then the test will fail. The helper methods explicitly throw {@link PrivateAccessFailure
 * PrivateAccessFailure} which is a direct sub-type of {@link Throwable} instead of a sub-type of one of the less
 * generic exception types such that in case of a non-existent target method, refactoring errors can only go undetected
 * if the test is expecting the too broad exception {@link Throwable}.
 */
public final class PrivateAccessHelper {

    private PrivateAccessHelper() {
        // Prevent instantiation.
    }

    /**
     * Invokes a private static method on the given {@code clazz} and returns its return value.
     *
     * Any exception the invoked method throws is re-thrown. Note that exceptions are not wrapped in a {@link
     * InvocationTargetException} as they would be when calling {@link Method#invoke(Object, Object...)} directly,
     * instead they are unwrapped first (using {@link Throwable#getCause()} and then rethrown. This way, exceptions can
     * be asserted as usual in a unit test.
     *
     * Note that if the target method does not exist, an exception of type {@link PrivateAccessFailure
     * PrivateAccessFailure} will be thrown. This exception extends the most generic type of exceptions, {@link
     * Throwable}, which is chosen so that refactoring errors have the biggest chance of being detected as a failing
     * test (unless the test expects a {@link Throwable} itself).
     *
     * @param clazz           The class on which to invoke the method.
     * @param methodName      The name of the method to invoke.
     * @param argumentTypes   The types of the arguments the given method takes (in same order as in target method
     *                        signature).
     * @param argumentObjects The parameters to the method to invoke.
     * @param <T>             The type of the class on which to invoke the method.
     * @return Returns the value of the invoked method.
     * @throws Throwable Throws any exception the invoked method threw, or a {@link PrivateAccessFailure
     *                   PrivateAccessFailure} in case the target method could not be invoked (method does not exist,
     *                   method has different signature, security manager forbids access, etc.).
     */
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public static <T> Object invokePrivateStaticMethod(
            @Nonnull final Class<T> clazz,
            @Nonnull final String methodName,
            @Nonnull final Class<?>[] argumentTypes,
            @Nullable final Object... argumentObjects) throws Throwable {
        assert clazz != null;
        assert methodName != null;
        assert argumentTypes != null;

        // OpenJDK 6 bug (possibly #611284) required explicit type parameter for null value.
        //noinspection RedundantCast
        return invokePrivateMethod(clazz, (T) null, methodName, argumentTypes, argumentObjects);
    }

    /**
     * Invokes a private method on the given {@code instance} and returns its return value. The type used to invoke the
     * private method on is the runtime type of {@code instance}. If you want to invoke the private method of a
     * different class (i.e. a super-type of {@code instance}), use {@link #invokePrivateMethod(Class, Object, String,
     * Class[], Object...)} instead and specify the type explicitly.
     *
     * Any exception the invoked method throws is re-thrown. Note that exceptions are not wrapped in a {@link
     * InvocationTargetException} as they would be when calling {@link Method#invoke(Object, Object...)} directly,
     * instead they are unwrapped first (using {@link Throwable#getCause()} and then rethrown. This way, exceptions can
     * be asserted as usual in a unit test.
     *
     * Note that if the target method does not exist, an exception of type {@link PrivateAccessFailure
     * PrivateAccessFailure} will be thrown. This exception extends the most generic type of exceptions, {@link
     * Throwable}, which is chosen so that refactoring errors have the biggest chance of being detected as a failing
     * test (unless the test expects a {@link Throwable} itself).
     *
     * @param instance        The instance on which to invoke the method.
     * @param methodName      The name of the method to invoke.
     * @param argumentTypes   The types of the arguments the given method takes (in same order as in target method
     *                        signature).
     * @param argumentObjects The parameters to the method to invoke.
     * @param <T>             The type of the class on which to invoke the method.
     * @return Returns the value of the invoked method.
     * @throws Throwable Throws any exception the invoked method threw, or a {@link PrivateAccessFailure
     *                   PrivateAccessFailure} in case the target method could not be invoked (method does not exist,
     *                   method has different signature, security manager forbids access, etc.).
     */
    @SuppressWarnings({"ProhibitedExceptionDeclared", "RedundantCast"})
    public static <T> Object invokePrivateMethod(
            @Nonnull final T instance,
            @Nonnull final String methodName,
            @Nonnull final Class<?>[] argumentTypes,
            @Nullable final Object... argumentObjects) throws Throwable {
        assert instance != null;
        assert methodName != null;
        assert argumentTypes != null;

        // Casting to {@code Class<T>} because {@link #invokePrivateMethod} expects the first argument to be the type
        // or super-type of the instance's runtime type. The cast is legal because {@link Object#getClass} will by
        // definition return the runtime type of {@code instance}. Code inspection thinks this cast is redundant, but
        // the compiler disagrees.
        //noinspection unchecked,RedundantCast
        return invokePrivateMethod((Class<T>) instance.getClass(), instance, methodName, argumentTypes,
                argumentObjects);
    }

    /**
     * Invokes a private method on the given {@code instance} using type {@code clazz} for method lookup, and returns
     * its return value. Use this method if you want to invoke a method on a different type than the runtime type of
     * {@code instance}. Otherwise, use {@link #invokePrivateMethod(Object, String, Class[], Object...)} instead.
     *
     * Any exception the invoked method throws is re-thrown. Note that exceptions are not wrapped in a {@link
     * InvocationTargetException} as they would be when calling {@link Method#invoke(Object, Object...)} directly,
     * instead they are unwrapped first (using {@link Throwable#getCause()} and then rethrown. This way, exceptions can
     * be asserted as usual in a unit test.
     *
     * Note that if the target method does not exist, an exception of type {@link PrivateAccessFailure
     * PrivateAccessFailure} will be thrown. This exception extends the most generic type of exceptions, {@link
     * Throwable}, which is chosen so that refactoring errors have the biggest chance of being detected as a failing
     * test (unless the test expects a {@link Throwable} itself).
     *
     * @param clazz           The class on which to lookup the method (must be of same type or super-type as {@code
     *                        instance}.
     * @param instance        The instance on which to invoke the method (must be of same type or sub-type as {@code
     *                        clazz}.
     * @param methodName      The name of the method to invoke.
     * @param argumentTypes   The types of the arguments the given method takes (in same order as in target method
     *                        signature).
     * @param argumentObjects The parameters to the method to invoke.
     * @return Returns the value of the invoked method.
     * @throws Throwable Throws any exception the invoked method threw, or a {@link PrivateAccessFailure
     *                   PrivateAccessFailure} in case the target method could not be invoked (method does not exist,
     *                   method has different signature, security manager forbids access, etc.).
     */
    @SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
    public static <T, U extends T> Object invokePrivateMethod(
            @Nonnull final Class<T> clazz,
            @Nullable final U instance,
            @Nonnull final String methodName,
            @Nonnull final Class<?>[] argumentTypes,
            @Nullable final Object... argumentObjects) throws Throwable {
        assert clazz != null;
        assert methodName != null;
        assert argumentTypes != null;

        final Object result;
        try {
            @Nonnull final Method method =
                    clazz.getDeclaredMethod(methodName, argumentTypes);
            method.setAccessible(true);
            result = method.invoke(instance, argumentObjects);
        } catch (final NoSuchMethodException | SecurityException | IllegalAccessException e) {
            throw new PrivateAccessFailure(e);
        } catch (final InvocationTargetException e) {
            // Unwrap exception thrown by invoked method and re-throwing.
            //noinspection ProhibitedExceptionThrown
            throw e.getCause();
        }

        return result;
    }

    public static class PrivateAccessFailure extends Throwable {
        private PrivateAccessFailure(@Nonnull final Throwable cause) {
            super(cause);

            assert cause != null;
        }
    }
}
