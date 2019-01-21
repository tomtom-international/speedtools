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

package com.tomtom.speedtools.rest;

import com.tomtom.speedtools.testutils.PrivateAccessHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ProxyAwareUriBuilderFactoryTest {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ProxyAwareUriBuilderFactoryTest.class);

    @Nonnull
    private static final String HEADER_TEST_NAME_X_TEST = "X-Test";

    @Nonnull
    private static final String HEADER_NAME_X_HTTPS = "X-Https";
    @Nonnull
    private static final String HEADER_NAME_X_FORWARDED_HOST = "X-Forwarded-Host";
    @Nonnull
    private static final String HEADER_NAME_X_ORIGINAL_URI = "X-Original-URI";
    @Nonnull
    private static final String HEADER_VALUE_X_HTTPS_TRUE = "on";
    @Nonnull
    private static final String HEADER_VALUE_X_FORWARDED_HOST = "unittest.tomtomtaxi.com";
    @Nonnull
    private static final String HEADER_VALUE_X_ORIGINAL_URI = "/api/a/b/c?key=value#fragment";

    /**
     * Following members are all @Nullable, but they are initialised during @Before, hence will be non-null during
     * tests.
     */
    private URI internalBaseUri;
    private URI internalUri;
    private URI referenceExternalBaseUri;
    private ProxyAwareUriBuilderFactory impl;

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private UriInfo uriInfo;

    @Before
    public void setup() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        internalBaseUri = new URI("http://localhost:8080/");
        internalUri = internalBaseUri.resolve("/a/b/c");
        assert internalUri != null;
        //noinspection ObjectEquality
        assert internalBaseUri != internalUri; // Intentionally comparing identity.

        referenceExternalBaseUri = new URI("https://unittest.tomtomtaxi.com/api");
        assert referenceExternalBaseUri != null;

        impl = new ProxyAwareUriBuilderFactory(httpServletRequest, uriInfo);
    }

    @Test(expected = IllegalStateException.class)
    public void testProxyAwareUriBuilderFactoryThrowsException() {
        LOG.info(
                "testProxyAwareUriBuilderFactoryThrowsException: IMPORTANT - the following test case should throw 1 " +
                        "exception");

        //noinspection ResultOfObjectAllocationIgnored
        new ProxyAwareUriBuilderFactory();
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testProxyAwareUriBuilderFactory() {
        LOG.info("testProxyAwareUriBuilderFactory");

        //noinspection ResultOfObjectAllocationIgnored
        new ProxyAwareUriBuilderFactory(httpServletRequest, uriInfo);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testProxyAwareUriBuilderFactoryAssertNoHttpServletRequest() {
        LOG.info(
                "testProxyAwareUriBuilderFactoryAssertNoHttpServletRequest");

        //noinspection ErrorNotRethrown
        try {
            // noinspection ResultOfObjectAllocationIgnored
            new ProxyAwareUriBuilderFactory(null, uriInfo);
        } catch (AssertionError ignored) {
            return;
        } catch (IllegalArgumentException ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testProxyAwareUriBuilderFactoryAssertNoUriInfo() {
        LOG.info(
                "testProxyAwareUriBuilderFactoryAssertNoUriInfo");

        //noinspection ErrorNotRethrown
        try {
            // noinspection ResultOfObjectAllocationIgnored
            new ProxyAwareUriBuilderFactory(httpServletRequest, null);
        } catch (AssertionError ignored) {
            return;
        } catch (IllegalArgumentException ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testProxyAwareUriBuilderFactoryAssertNull() {
        LOG.info(
                "testProxyAwareUriBuilderFactoryAssertNull");

        //noinspection ErrorNotRethrown
        try {
            // noinspection ResultOfObjectAllocationIgnored
            new ProxyAwareUriBuilderFactory(null, null);
        } catch (AssertionError ignored) {
            return;
        } catch (IllegalArgumentException ignored) {
            return;
        }
        Assert.fail("Wrong: null accepted by @Nonnull parameters!");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetHeaderExistent() throws Throwable {
        LOG.info("testGetHeaderExistent");

        final String testHeaderValue = "test";
        when(httpServletRequest.getHeader(HEADER_TEST_NAME_X_TEST)).thenReturn(testHeaderValue);

        assertThat(invokeGetHeader(impl, HEADER_TEST_NAME_X_TEST), is(testHeaderValue));
    }

    @SuppressWarnings({"ExpectedExceptionNeverThrown", "ProhibitedExceptionDeclared"})
    @Test(expected = ProxyAwareUriBuilderFactory.HeaderMissingException.class)
    public void testGetHeaderNonExistent() throws Throwable {
        LOG.info(
                "testGetHeaderNonExistent: IMPORTANT - the following test case should throw 1 exception");

        when(httpServletRequest.getHeader(HEADER_TEST_NAME_X_TEST)).thenReturn(null);

        invokeGetHeader(impl, HEADER_TEST_NAME_X_TEST);
    }

    @Test
    public void testNewInstance() throws URISyntaxException {
        LOG.info("testNewInstance");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(HEADER_VALUE_X_HTTPS_TRUE);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn(HEADER_VALUE_X_FORWARDED_HOST);
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(referenceExternalBaseUri));
    }

    @Test
    public void testNewInstanceMultipleForwardingHosts() throws URISyntaxException {
        LOG.info("testNewInstanceMultipleForwardingHosts");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(HEADER_VALUE_X_HTTPS_TRUE);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn(
                "some.proxy.com,unittest.tomtomtaxi.com");
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(referenceExternalBaseUri));
    }

    @Test
    public void testNewInstanceHttp() throws URISyntaxException {
        LOG.info("testNewInstanceHttp");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn("off");
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn(HEADER_VALUE_X_FORWARDED_HOST);
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri.getScheme(), is("http"));
        assertThat(uri.getAuthority(), is(referenceExternalBaseUri.getAuthority()));
        assertThat(uri.getPath(), is(referenceExternalBaseUri.getPath()));
    }

    @Test
    public void testNewInstanceFallbackNoXHttpsHeader() throws URISyntaxException {
        LOG.info("testNewInstanceFallbackNoXHttpsHeader: IMPORTANT - the following test case should log 2 errors");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(null);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn(HEADER_VALUE_X_FORWARDED_HOST);
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);
        when(uriInfo.getBaseUri()).thenReturn(internalBaseUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(internalBaseUri));
    }

    @Test
    public void testNewInstanceFallbackNoXForwardedHostHeader() throws URISyntaxException {
        LOG.info("testNewInstanceFallbackNoXHttpsHeader: IMPORTANT - the following test case should log 2 errors");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(HEADER_VALUE_X_HTTPS_TRUE);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn(null);
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);
        when(uriInfo.getBaseUri()).thenReturn(internalBaseUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(internalBaseUri));
    }

    @Test
    public void testNewInstanceFallbackNoXOriginalURIHeader() throws URISyntaxException {
        LOG.info(
                "testNewInstanceFallbackNoXOriginalURIHeader: IMPORTANT - the following test case should log 2 errors");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(HEADER_VALUE_X_HTTPS_TRUE);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn(HEADER_VALUE_X_FORWARDED_HOST);
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(null);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);
        when(uriInfo.getBaseUri()).thenReturn(internalBaseUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(internalBaseUri));
    }

    @Test
    public void testNewInstanceFallbackNoHeaders() throws URISyntaxException {
        LOG.info(
                "testNewInstanceFallbackNoHeaders: IMPORTANT - the following test case should log 2 errors");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(null);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn(null);
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(null);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);
        when(uriInfo.getBaseUri()).thenReturn(internalBaseUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(internalBaseUri));
    }

    @Test
    public void testNewInstanceFallbackEmptyLastForwardedHosts() throws URISyntaxException {
        LOG.info(
                "testNewInstanceFallbackEmptyLastForwardedHosts: IMPORTANT - the following test case should log 2 errors");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(HEADER_VALUE_X_HTTPS_TRUE);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn("some.proxy.com,");
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);
        when(uriInfo.getBaseUri()).thenReturn(internalBaseUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(internalBaseUri));
    }

    @Test
    public void testNewInstanceFallbackEmptyForwardedHosts() throws URISyntaxException {
        LOG.info("testNewInstanceFallbackEmptyForwardedHosts: IMPORTANT - the following test case should log 2 errors");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(HEADER_VALUE_X_HTTPS_TRUE);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn("");
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);
        when(uriInfo.getBaseUri()).thenReturn(internalBaseUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(internalBaseUri));
    }

    @Test
    public void testNewInstanceFallbackInvalidForwardedHost() throws URISyntaxException {
        LOG.info(
                "testNewInstanceFallbackInvalidForwardedHost: IMPORTANT - the following test case should log 2 errors");

        when(httpServletRequest.getHeader(HEADER_NAME_X_HTTPS)).thenReturn(HEADER_VALUE_X_HTTPS_TRUE);
        when(httpServletRequest.getHeader(HEADER_NAME_X_FORWARDED_HOST)).thenReturn("{");
        when(httpServletRequest.getHeader(HEADER_NAME_X_ORIGINAL_URI)).thenReturn(HEADER_VALUE_X_ORIGINAL_URI);
        when(uriInfo.getRequestUri()).thenReturn(internalUri);
        when(uriInfo.getBaseUri()).thenReturn(internalBaseUri);

        final UriBuilder uriBuilder = impl.newInstance();

        assertThat(uriBuilder, is(notNullValue()));

        final URI uri = uriBuilder.build();

        assertThat(uri, is(notNullValue()));
        assertThat(uri, is(internalBaseUri));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndex1LongerThan2() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndex1LongerThan2");

        final String[] list1 = new String[]{"a", "b", "c", "d"};
        final String[] list2 = new String[]{"c", "d"};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(2));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndex1ShorterThan2() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndex1ShorterThan2");

        final String[] list1 = new String[]{"c", "d"};
        final String[] list2 = new String[]{"a", "b", "c", "d"};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(0));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndex1Equals2Single() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndex1Equals2Single");

        final String[] list1 = new String[]{"a"};
        final String[] list2 = new String[]{"a"};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(0));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndex1Equals2Multi() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndex1Equals2Multi");

        final String[] list1 = new String[]{"a", "b"};
        final String[] list2 = new String[]{"a", "b"};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(0));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndexLastMatch() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndexLastMatch");

        final String[] list1 = new String[]{"a", "b"};
        final String[] list2 = new String[]{"c", "b"};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(1));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndexPartialMatch() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndexPartialMatch");

        final String[] list1 = new String[]{"a", "b", "c"};
        final String[] list2 = new String[]{"a", "b", "d"};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(list1.length));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndexNoMatch() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndexNoMatch");

        final String[] list1 = new String[]{"a", "b"};
        final String[] list2 = new String[]{"c", "d"};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(list1.length));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndexEmpty() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndexEmpty");

        final String[] list1 = new String[]{};
        final String[] list2 = new String[]{};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(list1.length));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Test
    public void testGetLongestSuffixStartIndex2Empty() throws Throwable {
        LOG.info("testGetLongestSuffixStartIndex2Empty");

        final String[] list1 = new String[]{"a"};
        final String[] list2 = new String[]{};

        final int result = invokeGetLongestSuffixStartIndex(list1, list2);
        assertThat(result, is(list1.length));
    }

    /**
     * Invokes the private {@link ProxyAwareUriBuilderFactory#getHeader(String)} method. Throws any exceptions thrown by
     * the invoked method, as well as a {@link PrivateAccessHelper.PrivateAccessFailure PrivateAccessFailure} (which is
     * a direct sub-type of {@link Throwable}) in case the method cannot be found.
     *
     * @param instance   The {@link ProxyAwareUriBuilderFactory} instance to call {@link ProxyAwareUriBuilderFactory#getHeader(String)
     *                   getHeader(String)} on.
     * @param headerName See {@link ProxyAwareUriBuilderFactory#getHeader(String) getHeader(String)}.
     * @return See {@link ProxyAwareUriBuilderFactory#getHeader(String) getHeader(String)}.
     * @throws Throwable See {@link ProxyAwareUriBuilderFactory#getHeader(String) getHeader(String)}, or a {@link
     *                   PrivateAccessHelper.PrivateAccessFailure PrivateAccessFailure} in case the method could not be
     *                   found.
     */
    @SuppressWarnings("ProhibitedExceptionDeclared")
    private static String invokeGetHeader(
            @Nonnull final ProxyAwareUriBuilderFactory instance,
            @Nullable final String headerName) throws Throwable {
        assert instance != null;

        return (String) PrivateAccessHelper.invokePrivateMethod(instance, "getHeader", new Class[]{String.class},
                headerName);
    }

    /**
     * Invokes the private static {@link ProxyAwareUriBuilderFactory#getLongestSuffixStartIndex(String[], String[])}
     * method. Throws any exceptions thrown by the invoked method, as well as a {@link
     * PrivateAccessHelper.PrivateAccessFailure PrivateAccessFailure} (which is a direct sub-type of {@link Throwable})
     * in case the method cannot be found.
     *
     * @param list1 See {@link ProxyAwareUriBuilderFactory#getLongestSuffixStartIndex(String[], String[])
     *              getLongestSuffixStartIndex(String[], String[])}.
     * @param list2 See {@link ProxyAwareUriBuilderFactory#getLongestSuffixStartIndex(String[], String[])
     *              getLongestSuffixStartIndex(String[], String[])}.
     * @return See {@link ProxyAwareUriBuilderFactory#getLongestSuffixStartIndex(String[], String[])
     * getLongestSuffixStartIndex(String[], String[])}.
     * @throws Throwable See {@link ProxyAwareUriBuilderFactory#getLongestSuffixStartIndex(String[], String[])
     *                   getLongestSuffixStartIndex(String[], String[])}, or a {@link PrivateAccessHelper.PrivateAccessFailure
     *                   PrivateAccessFailure} in case the method could not be found.
     */
    @SuppressWarnings("ProhibitedExceptionDeclared")
    private static int invokeGetLongestSuffixStartIndex(
            @Nullable final String[] list1,
            @Nullable final String[] list2) throws Throwable {
        return (Integer) PrivateAccessHelper.invokePrivateStaticMethod(ProxyAwareUriBuilderFactory.class,
                "getLongestSuffixStartIndex", new Class[]{String[].class, String[].class}, list1, list2);
    }
}
