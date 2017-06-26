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
package com.tomtom.speedtools.rest;

import com.tomtom.speedtools.rest.security.ContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Factory to create proxy aware {@link UriBuilder}'s. Proxy aware means that the returned {@link UriBuilder} will be
 * initialised in such a way that the resulting URI will follow the external URI format as seen by clients in front of
 * the front-proxies.
 *
 * This factory <em>must</em> be instantiated on the thread that is calling the resource method, because it relies on
 * the {@link HttpServletRequest} and {@link UriInfo} objects retrieved from RestEasy. If this factory is instantiated
 * on a different thread, RestEasy will not have access to those objects, and hence this factory can't function.
 *
 * The returned {@link UriBuilder} will use the same scheme as the current request. Hence, if the caller of this factory
 * needs to construct a URI that uses a different scheme (for example to redirect from an HTTP to an HTTPS resource or
 * vice versa), then the scheme must be set explicitly on the returned {@link UriBuilder} by the caller using {@link
 * UriBuilder#scheme(String)}.
 *
 * Once instantiated, this factory must only be used within the same request, because it gets its information from the
 * current request. If the factory is re-used in a different request, it will use the information from the request in
 * whose context it was instantiated.
 *
 * For example, if a resource is visible externally (i.e. in front of the proxy) as
 * <pre>{@code
 * https://www.tomtomtaxi.com/api/web/1/info/version
 * }</pre>
 * and internally deployed under
 * <pre>{@code
 * http://prod-txbackend-001/web/1/info/version
 * }</pre>
 * then the {@link UriBuilder} will be initialised with
 * <pre>{@code
 * https://www.tomtomtaxi.com/api
 * }</pre>
 * and can then be used to construct URIs relative to that base URI.
 *
 * <strong>Note:</strong> For this factory work, the front-proxy must include the following headers in its request to
 * the application: <ul> <li>{@code X-Https}: must carry value {@code on} if the current request came in via HTTPS. Can
 * carry any other value or be empty if the request did not come in via HTTPS (but the header must be present).</li>
 * <li>{@code X-Original-URI}: must carry the path (other parts like query parameters or fragment are also allowed to be
 * present).</li> <li>{@code X-Forwarded-Host}: must carry the name of the front-proxy, or a comma-separated list of
 * proxies through which the request passed through, but the TomTom front-proxy must be the last one in the list. If
 * there are other reverse-proxies in the path between the TomTom front-proxy and the application within the
 * data-center, then they must not contribute to this list!</li></ul>
 *
 * If those headers are absent, then the factory will fall back to create a {@link UriBuilder} initialised with the
 * scheme, authority and path prefix of the application server. URIs returned to clients in front of the front-proxies
 * will not be able to access those URIs, but it will allow clients inside the data-center to function correctly (e.g.
 * for local development, integration or load and stress testing). However, an error will be logged on every call to
 * {@link ProxyAwareUriBuilderFactory#newInstance()}.
 */
@SuppressWarnings("UnnecessaryJavaDocLink") // Some method descriptions want to point to class description.
public class ProxyAwareUriBuilderFactory {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(ProxyAwareUriBuilderFactory.class);

    @Nonnull
    private static final String HEADER_NAME_X_HTTPS = "X-Https";
    @Nonnull
    private static final String HEADER_NAME_X_FORWARDED_HOST = "X-Forwarded-Host";
    @Nonnull
    private static final String HEADER_NAME_X_ORIGINAL_URI = "X-Original-URI";

    @Nonnull
    private static final String HEADER_VALUE_X_HTTPS_TRUE = "on";

    @Nonnull
    private static final String SCHEME_HTTP = "http";
    @Nonnull
    private static final String SCHEME_HTTPS = "https";

    /**
     * Error message used by {@link #newInstance()}, to prevent duplication in multiple catch clauses. Placeholder is to
     * be substituted with {@link UriInfo#getRequestUri()}.
     *
     * Once we are using Java 7, those catch clauses should be collapsed into a single clause using a multi-type
     * condition, and then this utility member is not needed anymore.
     */
    @Nonnull
    private static final String NEW_INSTANCE_ERROR_MESSAGE = "newInstance: Failed to construct UriBuilder " +
            "relative to externally visible URI, falling back to using URI of server as base URI instead. This will break " +
            "clients outside the data-center! (Currently requested resource is \"{}\".)";

    @Nonnull
    private final HttpServletRequest httpServletRequest;
    @Nonnull
    private final UriInfo uriInfo;

    /**
     * Caches the {@link UriBuilder} specific to this request context.
     */
    @Nullable
    private UriBuilder baseUriBuilder;

    /**
     * Constructs a new {@link ProxyAwareUriBuilderFactory} that is initialised with the current request context.
     * <em>Note</em> that the constructor must be called on the same thread that called the current JAX-RS resource,
     * otherwise request-contextual data cannot be retrieved. This constructor will throw an error in case it is not
     * called on the correct thread.
     *
     * @throws RuntimeException In case this constructor is not accessed on the thread that is used to call the JAX-RS
     *                          resource method.
     */
    public ProxyAwareUriBuilderFactory() {
        this(ContextHelper.getContextData(HttpServletRequest.class), ContextHelper.getContextData(UriInfo.class));
    }

    /**
     * Package-private constructor for use by unit tests.
     *
     * @param httpServletRequest The current request.
     * @param uriInfo            The {@link UriInfo} object of the current request context.
     */
    ProxyAwareUriBuilderFactory(
            @Nonnull final HttpServletRequest httpServletRequest,
            @Nonnull final UriInfo uriInfo) {
        assert httpServletRequest != null;
        assert uriInfo != null;

        this.httpServletRequest = httpServletRequest;
        this.uriInfo = uriInfo;
    }

    /**
     * Returns a new {@link UriBuilder} instance, initialised with the scheme, authority and prefix path of the
     * front-proxy in case the front-proxy is configured correctly (see {@link ProxyAwareUriBuilderFactory class
     * description} for requirements on the front-proxy). If the front-proxy is not configured correctly, or clients are
     * not accessing the resource via the front-proxy, then a {@link UriBuilder} will be returned that is initialised
     * with the scheme, authority and path prefix of the application server, and an error will be logged. URIs
     * constructed from such a {@link UriBuilder} won't be accessible by clients external to the front-proxy.
     *
     * <strong>Note:</strong> this method is not thread safe. If you want to get multiple {@link UriBuilder} instances
     * for the same request on different threads concurrently, then instantiate a {@link ProxyAwareUriBuilderFactory}
     * for each thread.
     *
     * @return {@link UriBuilder} either initialised with scheme, authority and path prefix of front-proxy or of
     * application server, if front-proxy configured incorrectly or not client does not access current resource
     * via front-proxy.
     */
    @Nonnull
    public UriBuilder newInstance() {
        if (baseUriBuilder == null) {
            @Nonnull UriBuilder uriBuilder;
            try {
                @Nonnull final String scheme = getScheme();
                @Nonnull final String authority = getAuthority();
                @Nonnull final String[] pathPrefix = getPathPrefix();

                @Nonnull final URI baseUri = new URI(scheme, authority, null, null, null);

                uriBuilder = UriBuilder.fromUri(baseUri);

                for (final String segment : pathPrefix) {
                    uriBuilder.path(segment);
                }
            } catch (final HeaderMissingException | InvalidHeaderValueException | URISyntaxException e) {
                LOG.error(NEW_INSTANCE_ERROR_MESSAGE, uriInfo.getRequestUri(), e);
                uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri());
            }

            baseUriBuilder = uriBuilder;
        }

        return baseUriBuilder.clone();
    }

    /**
     * Returns the scheme used by the current request to the front-proxy, or will throw an exception if front-proxy is
     * configured incorrectly, or client has bypassed front-proxy.
     *
     * @return The scheme of the current request.
     * @throws HeaderMissingException In case the front-proxy is configured incorrectly or client bypassed the
     *                                front-proxy.
     */
    @Nonnull
    private String getScheme() throws HeaderMissingException {
        @Nonnull final String xHttpsHeaderValue = getHeader(HEADER_NAME_X_HTTPS);
        if (HEADER_VALUE_X_HTTPS_TRUE.equals(xHttpsHeaderValue.trim())) {
            return SCHEME_HTTPS;
        }

        return SCHEME_HTTP;
    }

    /**
     * Returns the authority used by the current request to the front-proxy, or will throw an exception if front-proxy
     * is configured incorrectly, or client has bypassed front-proxy.
     *
     * @return The authority of the current request.
     * @throws HeaderMissingException      In case the front-proxy is configured incorrectly or client bypassed the
     *                                     front-proxy.
     * @throws InvalidHeaderValueException In case the required header is present but contains an invalid value.
     */
    @Nonnull
    private String getAuthority() throws HeaderMissingException, InvalidHeaderValueException {
        @Nonnull final String xForwardedHostHeaderValue = getHeader(HEADER_NAME_X_FORWARDED_HOST);

        if (xForwardedHostHeaderValue.trim().isEmpty()) {
            LOG.error("getAuthority: header {} must not be empty.", HEADER_NAME_X_FORWARDED_HOST);
            throw new InvalidHeaderValueException(HEADER_NAME_X_FORWARDED_HOST, "Header must not be empty.");
        }

        /**
         * The X-Forwarded-Host is a comma-separated list of hosts that the request has passed through. We are
         * interested only in the last host. This assumes that between the front-proxy and this application there
         * are either no further reverse-proxies in between, or they don't contribute to the X-Forwarded-Host header.
         * {@code limit} of {@code -1} tells {@link String#split(String, int)} to return empty trailing strings, so that
         * we can detect malformed header values, and not mistakenly use a proxy host that was not our front-proxy.
         */
        @Nonnull final String[] proxyHosts = xForwardedHostHeaderValue.split(",", -1);

        // Split never returns an empty array, even if no occurrence is found (then the returned value is an array of
        // length one, containing the original value (even if the original value is the empty string)).
        @Nonnull final String proxyHost = proxyHosts[proxyHosts.length - 1].trim();

        if (proxyHost.isEmpty()) {
            LOG.error("getAuthority: last host in host list \"{}\" in header {} must not be empty.",
                    xForwardedHostHeaderValue, HEADER_NAME_X_FORWARDED_HOST);
            throw new InvalidHeaderValueException(HEADER_NAME_X_FORWARDED_HOST,
                    "Last host in list \"" + xForwardedHostHeaderValue + "\" must not be empty.");
        }

        // Check validity of authority
        try {
            @Nonnull final URI proxyHostUri = new URI(proxyHost);
            proxyHostUri.parseServerAuthority();
        } catch (final URISyntaxException e) {
            LOG.error(
                    "getAuthority: host \"" + proxyHost + "\" in header " + HEADER_NAME_X_FORWARDED_HOST + " is invalid.",
                    e);
            throw new InvalidHeaderValueException(HEADER_NAME_X_FORWARDED_HOST,
                    "Invalid host \"" + xForwardedHostHeaderValue + "\".",
                    e);
        }

        return proxyHost;
    }

    /**
     * Returns the path prefix of the external URI.
     *
     * <em>Note</em> that this method may have encoding issues when comparing the two paths, e.g. if the path forwarded
     * by the proxy server has not been decoded. In practice, that should not be an issue, since the path prefix that is
     * used to map external URIs to internal URIs should not contain any reserved characters (see <a
     * href="http://en.wikipedia.org/wiki/Percent-encoding#Percent-encoding_reserved_characters">http://en.wikipedia.org/wiki/Percent-encoding#Percent-encoding_reserved_characters</a>).
     *
     * @return The path prefix used by the front-proxy of the current request.
     * @throws HeaderMissingException      In case the front-proxy is configured incorrectly or client bypassed the
     *                                     front-proxy.
     * @throws InvalidHeaderValueException In case the value contained an invalid path.
     */
    @Nonnull
    private String[] getPathPrefix() throws HeaderMissingException, InvalidHeaderValueException {
        @Nonnull final String xOriginalURIHeaderValue = getHeader(HEADER_NAME_X_ORIGINAL_URI).trim();

        @Nonnull final String[] externalPathSegments;
        try {
            externalPathSegments = (new URI(xOriginalURIHeaderValue)).getPath().split("/");
        } catch (final URISyntaxException e) {
            LOG.error("getPathPrefix: path \"" + xOriginalURIHeaderValue + "\" is not a valid path in header " +
                    HEADER_NAME_X_ORIGINAL_URI + '.', e);
            throw new InvalidHeaderValueException(HEADER_NAME_X_ORIGINAL_URI,
                    "Value \"" + xOriginalURIHeaderValue + "\" is not a valid path.", e);
        }
        @Nonnull final String[] internalPathSegments = uriInfo.getRequestUri().getPath().split("/");

        final int suffixSegmentIndex = getLongestSuffixStartIndex(externalPathSegments, internalPathSegments);

        final String[] result;
        if (suffixSegmentIndex == externalPathSegments.length) {
            result = externalPathSegments;
        } else {
            result = Arrays.copyOfRange(externalPathSegments, 0, suffixSegmentIndex);
        }

        return result;
    }

    /**
     * Returns the index into {@code list1} at which the longest matching suffix between {@code list1} and {@code list2}
     * begins.
     *
     * If there is a matching suffix found, then the result will be {@code >= 0} and {@code < list1.length}. If there is
     * no matching suffix at all, then the result will be {@code list1.length}.
     *
     * E.g., if {@code list1} is
     * <pre>{@code
     * "a", "b", "c", "d"
     * }</pre>
     * and {@code list2} is
     * <pre>{@code
     * "c", "d"
     * }</pre>
     * then the returned value will be {@code 2}, which is the index into {@code list1} at which the longest matching
     * suffix {@code "c", "d"} begins.
     *
     * @return Index into {@code list1} at which longest matching suffix begins, or {@code list1.length} if there is no
     * matching suffix.
     */
    private static int getLongestSuffixStartIndex(
            @Nonnull final String[] list1,
            @Nonnull final String[] list2) {
        assert list1 != null;
        assert list2 != null;

        final int startIndex;

        // If array is empty, {@code listMaxIndex} will be {@code -1}. This won't cause an array out of bounds error
        // because array access is guarded by {@code i > listMaxIndex}, and {@code i} is guaranteed to be {@code >= 0}.
        final int list1MaxIndex = list1.length - 1;
        final int list2MaxIndex = list2.length - 1;

        int i = 0;
        while (true) {
            if ((i > list1MaxIndex) ||
                    (i > list2MaxIndex) ||
                    (!(list1[list1MaxIndex - i].equals(list2[list2MaxIndex - i])))
                    ) {
                // The start of the longest matching suffix is plus 1 because we have just overshot (we found the first
                // non-matching element).
                startIndex = (list1MaxIndex - i) + 1;
                break;
            }

            i++;
        }

        assert (startIndex >= 0) && (startIndex <= list1.length);
        return startIndex;
    }

    /**
     * Returns the value of the header with the given {@code headerName}. If the header is not present, an exception
     * will be thrown.
     *
     * @param headerName The name of the header for which to return the value.
     * @return The value for the given {@code headerName}.
     * @throws HeaderMissingException In case the requested header is not present in the request.
     */
    @Nonnull
    private String getHeader(@Nonnull final String headerName) throws HeaderMissingException {
        assert headerName != null;

        final String headerValue = httpServletRequest.getHeader(headerName);
        if (headerValue == null) {
            LOG.error("getHeader: header {} is missing from request.", headerName);
            throw new HeaderMissingException(headerName);
        }

        return headerValue;
    }

    /**
     * Made package private for unit test.
     */
    static class HeaderMissingException extends Exception {
        HeaderMissingException(@Nonnull final String headerName) {
            super(constructMessage(headerName));
        }

        @Nonnull
        private static String constructMessage(@Nonnull final String headerName) {
            assert headerName != null;

            return "Request is missing the \"" + headerName + "\" header.";
        }
    }

    /**
     * Made package private for unit test.
     */
    static class InvalidHeaderValueException extends Exception {
        InvalidHeaderValueException(
                @Nonnull final String headerName,
                @Nonnull final String message) {
            super(constructMessage(headerName, message));
        }

        InvalidHeaderValueException(
                @Nonnull final String headerName,
                @Nonnull final String message,
                @Nonnull final Throwable cause) {
            super(constructMessage(headerName, message), cause);
        }

        @Nonnull
        private static String constructMessage(
                @Nonnull final String headerName,
                @Nonnull final String message) {
            assert headerName != null;
            assert message != null;

            return "Request contains invalid value in header \"" + headerName + "\". " + message;
        }
    }
}
