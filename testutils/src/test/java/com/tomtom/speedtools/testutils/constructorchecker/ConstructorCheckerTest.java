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

package com.tomtom.speedtools.testutils.constructorchecker;

import com.tomtom.speedtools.testutils.ValidationFailException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConstructorCheckerTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConstructorCheckerTest.class);

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testValidateConstructorAssertionError() throws ValidationFailException {
        LOG.info("testValidateConstructorAssertionError");
        ConstructorChecker.validateConstructor(CorrectAssertionClass.class);
    }

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testValidateConstructorWithException() throws ValidationFailException {
        LOG.info("testValidateConstructorWithException");
        ConstructorChecker.validateConstructor(MissingAssertClass.class, new int[]{1});
    }

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testValidateConstructorIllegalArgumentException() throws ValidationFailException {
        LOG.info("testValidateConstructorIllegalArgumentException");
        ConstructorChecker.validateConstructor(CorrectIllegalArgumentClass.class);
    }

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testValidateStaticMethod() throws ValidationFailException, NoSuchMethodException {
        LOG.info("testValidateStaticMethod");
        ConstructorChecker.validateMethod(null, StaticMethodClass.class.getMethod("staticMethod", String.class));
    }

    @Test(expected = ValidationFailException.class)
    public void testValidateConstructorWithMissingAssertCheck() throws ValidationFailException {
        LOG.info("testValidateConstructorWithMissingAssertCheck");
        ConstructorChecker.validateConstructor(MissingAssertClass.class);
    }

    @Test(expected = ValidationFailException.class)
    public void testValidateConstructorWithMissingIllegalArgumentCheck() throws ValidationFailException {
        LOG.info("testValidateConstructorWithMissingIllegalArgumentCheck");
        ConstructorChecker.validateConstructor(MissingIllegalArgumentClass.class);
    }

    @Test(expected = ValidationFailException.class)
    public void testValidateConstructorThatAlwaysThrowsAssertionError() throws ValidationFailException {
        LOG.info("testValidateConstructorThatAlwaysThrowsAssertionError: " +
                "IMPORTANT - the following test case should throw 1 exception");
        ConstructorChecker.validateConstructor(AlwaysThrowsAssertionErrorClass.class);
    }

    private static class CorrectAssertionClass {
        public CorrectAssertionClass(final String string, final List<?> list) {
            assert string != null;
            assert list != null;
        }
    }

    private static class CorrectIllegalArgumentClass {
        public CorrectIllegalArgumentClass(final String string, final List<?> list) {
            if ((string == null) || (list == null)) {
                throw new IllegalArgumentException();
            }
        }
    }

    private static class MissingAssertClass {
        @SuppressWarnings("UnusedParameters")
        public MissingAssertClass(final String string, final List<?> list) {
            assert string != null;
        }
    }

    private static class MissingIllegalArgumentClass {
        @SuppressWarnings("UnusedParameters")
        public MissingIllegalArgumentClass(final String string, final List<?> list) {
            if (list == null) {
                throw new IllegalArgumentException();
            }
        }
    }

    private static class AlwaysThrowsAssertionErrorClass {
        @SuppressWarnings("UnusedParameters")
        public AlwaysThrowsAssertionErrorClass(final String string, final List<?> list) {
            assert false;
        }
    }

    private static class StaticMethodClass {
        public static void staticMethod(final String string) {
            assert string != null;
        }
    }
}
