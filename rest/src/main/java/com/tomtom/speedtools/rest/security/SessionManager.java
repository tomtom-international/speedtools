/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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
package com.tomtom.speedtools.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.SecurityContext;
import java.io.Serializable;
import java.security.Principal;

/**
 * The session manager is the central place to start and terminate sessions, and create a {@link SecurityContext} for
 * the ongoing session. It encapsulates how information is stored in a session, and how sessions are marked as
 * authenticated.
 *
 * <em>None</em> of the methods provided by this class are thread-safe in the sense that no other threads can interfere
 * with the session during method calls of another thread. This is due to the asynchronous nature of HTTP, where, even
 * if the methods here were thread-safe, a session that was created could e.g. be torn down immediately again by a
 * different thread while the HTTP request of the first thread is still being processed by the application.
 * <em>However</em>, the session manager guarantees that any data set in the session is set atomically, so that the
 * session can never contain mixed data from invocations by multiple concurrent calls. For more information, see the
 * JavaDoc of each method in question.
 */
public class SessionManager {
    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

    /**
     * Authentication schemes used by this class in {@code startWebSession} and {@code startAppSession}.
     * Any other strings may be used as well, for example, by derived classes of {@code SessionManager}.
     */
    public static final String AUTHENTICATION_SCHEME_USERNAME = "USERNAME";
    public static final String AUTHENTICATION_SCHEME_APPTOKEN = "APPTOKEN";

    /**
     * The name of the key that is used to store our session data in the {@link HttpSession}. The name must be
     * name-spaced to prevent collisions with e.g. container attributes or attributes set by other frameworks.
     */
    @Nonnull
    private static final String SESSION_DATA_KEY = "com.tomtom.speedtools.rest.security.SESSIONDATA";

    /**
     * The current servlet request.
     */
    @Nonnull
    private final HttpServletRequest httpServletRequest;

    /**
     * Constructs a new {@code SessionManager} that is initialised with the current request context. <em>Note</em> that
     * the constructor must be called on the same thread that called the current JAX-RS resource, otherwise
     * request-contextual data cannot be retrieved. This constructor will throw an error in case it is not called on the
     * correct thread.
     *
     * @throws RuntimeException In case this constructor is not accessed on the thread that is used to call the JAX-RS
     *                          resource method.
     */
    public SessionManager() {
        this(ContextHelper.getContextData(HttpServletRequest.class));
    }

    /**
     * Package-private constructor for use by unit tests.
     *
     * @param httpServletRequest The current request.
     */
    SessionManager(@Nonnull final HttpServletRequest httpServletRequest) {
        assert httpServletRequest != null;

        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Starts a new web session for the given {@code principal} in the current {@code httpServletRequest}. A potentially
     * existing session is terminated first to prevent session fixation attacks.
     *
     * This method is <em>not</em> thread-safe in the sense that a session actually exists when this method returns,
     * because a racing request may have torn down the session that is being created here while this method is
     * executing, or the data set in this call is actually the data stored in the session at the point this method
     * returns. This cannot be prevented even with locking, because HTTP is inherently asynchronous, and even if this
     * method would lock, as soon as the method returns a racing request could then tear down the session or modify the
     * session data. <em>However</em>, this method guarantees that all data that is being set as part of the
     * authenticated session is set atomically, i.e. multiple racing calls will not cause the session to contain mixed
     * data of those different calls, one call will always win. Also, data set during this call is stored in the session
     * before this call returns, but as described above, may have already been overwritten by a racing thread at the
     * point this call has returned.
     *
     * @param principal The {@link Principal} for which to create a session.
     * @return The session ID of the created session.
     */
    @Nonnull
    public String startWebSession(@Nonnull final Principal principal) {
        assert principal != null;

        return startSession(principal, AUTHENTICATION_SCHEME_USERNAME);
    }

    /**
     * Starts a new app session for the given {@code Principal} in the current {@code httpServletRequest}. A potentially
     * existing session is terminated first to prevent session fixation attacks.
     *
     * This method is <em>not</em> thread-safe in the sense that a session actually exists when this method returns,
     * because a racing request may have torn down the session that is being created here while this method is
     * executing, or the data set in this call is actually the data stored in the session at the point this method
     * returns. This cannot be prevented even with locking, because HTTP is inherently asynchronous, and even if this
     * method would lock, as soon as the method returns a racing request could then tear down the session or modify the
     * session data. <em>However</em>, this method guarantees that all data that is being set as part of the
     * authenticated session is set atomically, i.e. multiple racing calls will not cause the session to contain mixed
     * data of those different calls, one call will always win. Also, data set during this call is stored in the session
     * before this call returns, but as described above, may have already been overwritten by a racing thread at the
     * point this call has returned.
     *
     * @param principal The {@link Principal} for which to create a session.
     * @return The session ID of the created session.
     */
    @Nonnull
    public String startAppSession(@Nonnull final Principal principal) {
        assert principal != null;

        return startSession(principal, AUTHENTICATION_SCHEME_APPTOKEN);
    }

    /**
     * Terminates the current session. If there is no current session, the method does nothing.
     *
     * This method is <em>not</em> thread-safe, as a racing request may be setting up a new session for the current
     * request while this method is terminating the current session.
     */
    public void terminateSession() {
        // Check whether there actually is an ongoing session.
        @Nullable final HttpSession httpSession = getCurrentSession();
        if (httpSession == null) {
            return;
        }

        clearSessionData(httpSession);

        try {
            httpSession.invalidate();
        } catch (final IllegalStateException e) {

            // To be expected, since multiple racing HTTP requests may terminate the same session.
            LOG.info("terminateSession: session has already been terminated", e);
        }
    }

    /**
     * Returns the session ID of the current session, even if there is <em>no user authenticated</em> in the current
     * session. Returns {@code null} if there is no current session. <em>Note</em> however that the session may have
     * already been terminated by the time this method returns.
     *
     * @return The session ID of the current session, or {@code null} if there is no ongoing session.
     */
    @Nullable
    public String getCurrentSessionId() {

        // Get the current session. Return {@code null} if there is no ongoing session.
        @Nullable final HttpSession httpSession = getCurrentSession();
        if (httpSession == null) {
            return null;
        }

        return getSessionId(httpSession);
    }

    /**
     * Creates the {@link SecurityContext} for the current session. It does so by inspecting the {@link HttpSession} for
     * the presence of a username. If and only if a user is authenticated in the session, a {@link SecurityContext}
     * representing that user is created and returned. If no user is authenticated in the session, {@code null} will be
     * returned.
     *
     * <strong>Note:</strong> must not be used to access a {@link SecurityContext}, only to <em>create</em> the initial
     * {@code SecurityContext}! The way to access a {@code SecurityContext} is by either getting it injected via the
     * {@link javax.ws.rs.core.Context @Context} annotation, or by retrieving it from the RestEasy context stack.
     *
     * This method is <em>not</em> thread-safe in the sense that an authenticated session will actually exist even if
     * this method returns a {@link SecurityContext}, because a racing request may terminate the session while this
     * method here is executing. <em>However</em>, this method guarantees that the data in the returned {@link
     * SecurityContext} does not contain data from racing threads, but will always contain the atomic set of data that
     * was set on the authenticated session.
     *
     * @param httpServletRequest The current request.
     * @return If a user is authenticated in this session, then a {@link SecurityContext} is returned. Returns {@code
     * null} otherwise.
     */
    @Nullable
    public static SecurityContext createSecurityContextForSession(@Nonnull final HttpServletRequest httpServletRequest) {
        assert httpServletRequest != null;

        // Get the current session. {@link HttpServletRequest#getSession(boolean) getSession(false)} returns
        // {@code null} if there is no ongoing session.
        @Nullable final HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession == null) {
            return null;
        }

        // Get the session data. The simple fact that the session contains our session data is sufficient proof that
        // the session is authenticated. Return no {@link SecurityContext} if no session data (i.e. current session is
        // not authenticated).
        @Nullable final SessionData sessionData = getSessionData(httpSession);
        if (sessionData == null) {
            return null;
        }

        final SecurityContext securityContext =
                new SecurityContextImpl(new PrincipalImpl(sessionData.getUserId()), sessionData.getAuthenticationScheme());

        return securityContext;
    }

    /**
     * Starts a new session of the given {@code authenticationScheme} for the given {@code principal} in the current
     * servlet request. A potentially existing session is terminated first to prevent session fixation attacks.
     *
     * This method is <em>not</em> thread-safe in the sense that a session actually exists when this method returns,
     * because a racing request may have torn down the session that is being created here while this method is
     * executing, or the data set in this call is actually the data stored in the session at the point this method
     * returns. This cannot be prevented even with locking, because HTTP is inherently asynchronous, and even if this
     * method would lock, as soon as the method returns a racing request could then tear down the session or modify the
     * session data. <em>However</em>, this method guarantees that all data that is being set as part of the
     * authenticated session is set atomically, i.e. multiple racing calls will not cause the session to contain mixed
     * data of those different calls, one call will always win. Also, data set during this call is stored in the session
     * before this call returns, but as described above, may have already been overwritten by a racing thread at the
     * point this call has returned.
     *
     * @param principal            The {@link Principal} for which to create a session.
     * @param authenticationScheme The authentication scheme of the session to create.
     * @return The session ID of the created session.
     */
    @Nonnull
    private String startSession(
            @Nonnull final Principal principal,
            @Nonnull final String authenticationScheme) {
        assert principal != null;
        assert authenticationScheme != null;

        @Nonnull final HttpSession httpSession = createNewSession();
        try {
            // Set authenticated user ID and authentication scheme on session. Throws an {@link IllegalStateException}
            // in case the session has been concurrently invalidated before the session data could be set.
            @Nonnull final String userId = setSessionData(httpSession, principal, authenticationScheme);
            @Nonnull final String sessionId = getSessionId(httpSession);
            LOG.debug("startSession: created new session for user with ID {}. Session ID is {}", userId, sessionId);

            return sessionId;
        } catch (final IllegalStateException e) {

            // To be expected, since a racing HTTP request may have terminated this session before we were able to set
            // the session data.
            LOG.info("startSession: session has already been terminated", e);

            // We carry on as if we were able to set the session data, because the method does not guarantee that there
            // actually is a valid authenticated session at the end of this method.
            return getSessionId(httpSession);
        }
    }

    /**
     * Returns the current session, even if there is <em>no user authenticated</em> in the current session. Returns
     * {@code null} if there is no current session.
     *
     * This method is <em>not</em> thread-safe in the sense that a concurrent request request may have invalidated the
     * returned session before this method could complete, but after it retrieved the session from the underlying
     * request. Also, a concurrent request may have created a new session after this method checked for session
     * existence but before it returned (i.e. this method would return {@code null} even though at the point it does so,
     * a session could exist).
     *
     * @return The currently ongoing session (authenticated or unauthenticated) or {@code null} if there is no current
     * session.
     */
    @Nullable
    private HttpSession getCurrentSession() {

        // Get the current session. {@link HttpServletRequest#getSession(boolean) getSession(false)} returns
        // {@code null} if there is no ongoing session.
        return httpServletRequest.getSession(false);
    }

    /**
     * Creates a new session, and terminates any existing session to prevent session fixation attacks (see <a
     * href="https://www.owasp.org/index.php/Session_fixation">https://www.owasp.org/index.php/Session_fixation</a>).
     *
     * This method is <em>not</em> thread-safe in the sense that a concurrent request request may have invalidated the
     * returned session before this method could complete, but after it created the session.
     *
     * @return The newly created session.
     */
    @Nonnull
    private HttpSession createNewSession() {

        // Terminate any existing session first to prevent session fixation attacks.
        terminateSession();

        /**
         * {@link HttpServletRequest#getSession()} gets the current session or creates a new session if one does not
         * exist already. Since we've just terminated a potentially existing session, there shouldn't be a session, but
         * a concurrent thread may have created a new one already in between. That does not create a session fixation
         * attack vector, because the session ID will be different from the session ID that came in with current
         * request.
         */
        @Nonnull final HttpSession httpSession = httpServletRequest.getSession();
        assert httpSession != null; // Check explicitly since {@link HttpSession#getSession()} has no codified post-conditions.
        return httpSession;
    }

    /**
     * Returns the session ID of the given {@code httpSession}.
     *
     * @param httpSession The session for which to retrieve the session ID.
     * @return The session ID of the given {@code httpSession}.
     */
    @Nonnull
    private static String getSessionId(@Nonnull final HttpSession httpSession) {
        @Nonnull final String sessionId = httpSession.getId();
        assert sessionId != null; // Check explicitly since {@link HttpSession#getId()} has no codified post-conditions.
        return sessionId;
    }

    /**
     * Sets all data necessary to establish an authenticated session in the given {@code httpSession}. This method is
     * <em>not</em> thread-safe in the sense that one thread may set the data here, but another thread may have already
     * removed the data again before this method returns. <em>However</em>, this method ensures that all data to be set
     * in this call is either set or not (in case a racing thread terminates the session). The session will never
     * contain mixed data from different racing threads, one thread will always win.
     *
     * @param httpSession          The session to set the session data on.
     * @param principal            The {@link Principal} from which to take the user ID.
     * @param authenticationScheme The authentication scheme to set on the session.
     * @return The user ID that was set in the session.
     * @throws IllegalStateException Throws an {@link IllegalStateException} in case the session has been invalidated
     *                               before this method could set the session data.
     */
    @Nonnull
    private static String setSessionData(
            @Nonnull final HttpSession httpSession,
            @Nonnull final Principal principal,
            @Nonnull final String authenticationScheme) throws IllegalStateException {
        assert httpSession != null;
        assert principal != null;
        assert authenticationScheme != null;

        @Nonnull final String userId = principal.getName();
        final SessionData sessionData = new SessionData(userId, authenticationScheme);

        /**
         * Throws an {@link IllegalStateException} in case session has already been invalidated. Don't catch it to allow
         * the caller to deal with this situation, because unlike in the other methods, we can't decided locally what
         * the correct course of action is, since the goal of this method was to set the session data, and we may not be
         * able to.
         */
        httpSession.setAttribute(SESSION_DATA_KEY, sessionData);
        return userId;
    }

    /**
     * Gets all session data from the given {@code httpSession}. This method is <em>not</em> thread-safe in the sense
     * that one thread may get the data here, but another thread may have already removed the data from the session
     * before this method returns, or the current session may have been concurrently invalidated before this method can
     * read the session data. <em>However</em>, this method ensures that either all data is retrieved, or nothing (in
     * case a racing thread terminates the session). The returned session data will never contain mixed data from
     * different racing threads.
     *
     * @param httpSession The session to retrieve the session data from.
     * @return The session data, or {@code null} if not an authenticated session.
     */
    @Nullable
    private static SessionData getSessionData(@Nonnull final HttpSession httpSession) {
        assert httpSession != null;

        @Nullable SessionData sessionData;
        try {
            sessionData = (SessionData) httpSession.getAttribute(SESSION_DATA_KEY);
        } catch (final IllegalStateException e) {
            // To be expected, since a racing HTTP request may have terminated this session before we got here.
            LOG.info("getSessionData: session has already been terminated", e);
            sessionData = null;
        }

        return sessionData;
    }

    /**
     * Clears all session data that was previously set by the SessionManager on the given {@code httpSession} (but not
     * data set by other parties than the SessionManager). Does nothing if no session data is currently set or the
     * session has been concurrently terminated before this method could remove the session data.
     *
     * @param httpSession The {@link HttpSession} to clear.
     */
    private static void clearSessionData(@Nonnull final HttpSession httpSession) {
        assert httpSession != null;

        try {
            httpSession.removeAttribute(SESSION_DATA_KEY);
        } catch (final IllegalStateException e) {
            // To be expected, since a racing HTTP request may have terminated this session before we got here. We
            // carry on as if nothing had happened, because the goal was to remove the session data, and an invalidated
            // session will not hold the session data, hence we have achieved that goal.
            LOG.info("clearSessionData: session has already been terminated", e);
        }
    }

    /**
     * Immutable container to hold session data. The immutability ensures that either all or nothing is set in the
     * session, and racing requests cannot cause the session to contain a mixed set of data from different requests.
     *
     * This class is serializable, to enable containers to replicate session state between multiple nodes.
     *
     * Made package private for unit test.
     */
    static class SessionData implements Serializable {
        /**
         * Version of this class to support serialization. Must be increased iff a change is made to this class that
         * breaks serialisation compatibility with previous versions of this class (see <a
         * href="http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678">http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html#6678</a>
         * for when exactly the version must be changed).
         */
        private static final long serialVersionUID = 1L;

        /**
         * The ID of the authenticated user in the {@link HttpSession}.
         *
         * @serial
         */
        @Nonnull
        private final String userId;

        /**
         * The authentication scheme of the authentication session.
         *
         * @serial
         */
        @Nonnull
        private final String authenticationScheme;

        /**
         * Constructs a session data object.
         *
         * Made package private for unit test.
         *
         * @param userId               The user ID of the user authenticated in this session.
         * @param authenticationScheme The authentication scheme of this session.
         */
        SessionData(
                @Nonnull final String userId,
                @Nonnull final String authenticationScheme) {
            assert userId != null;
            assert authenticationScheme != null;

            this.userId = userId;
            this.authenticationScheme = authenticationScheme;
        }

        /**
         * Returns the ID of the user authenticated in this session.
         *
         * Made package private for unit test.
         *
         * @return The ID of the authenticated user.
         */
        @Nonnull
        String getUserId() {
            return userId;
        }

        /**
         * Returns the authentication scheme of this session.
         *
         * Made package private for unit test.
         *
         * @return The authentication scheme.
         */
        @Nonnull
        String getAuthenticationScheme() {
            return authenticationScheme;
        }
    }
}
