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

package com.tomtom.speedtools.rest.security;

import com.tomtom.speedtools.domain.Uid;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.SecurityContext;
import java.io.*;
import java.security.Principal;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class SessionManagerTest {
    private static final Logger LOG = LoggerFactory.getLogger(SessionManagerTest.class);

    @Nonnull
    private static final String REFERENCE_SESSION_ID = "1234";
    @Nonnull
    private static final String REFERENCE_PERSON_ID = Uid.fromString("1-2-3-4-5").toString();

    @Nonnull
    private static final String SESSION_DATA_KEY = "com.tomtom.speedtools.rest.security.SESSIONDATA";

    @Mock
    private HttpServletRequest httpServletRequest;

    private HttpSession httpSession;

    private Identity identity;
    private SessionManager impl;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        httpSession = Mockito.spy(new HttpSessionFake());

        // All mocked sessions contain the reference session ID.
        Mockito.doReturn(REFERENCE_SESSION_ID).when(httpSession).getId();

        // Create an empty identity with the  Account ID set.
        identity = new Identity(new Uid<>(REFERENCE_PERSON_ID));

        // Create the class under test.
        impl = new SessionManager(httpServletRequest);
    }

    /*
     * Starting a web session must set the user identifier and the authentication scheme (as {@link
     * AuthenticationScheme#WEB}) as {@link SessionManager.SessionData} in the {@link HttpSession}.
     */

    @Test
    public void testStartWebSession() {
        LOG.info("testStartWebSession");

        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);

        // Creating a new session must invalidate the old session, hence we have to expect this call.
        Mockito.doNothing().when(httpSession).invalidate();

        // A successfully created session must carry the reference session ID.
        assertThat(impl.startWebSession(identity), is(REFERENCE_SESSION_ID));

        // Check that the session carries the session data.
        verify(httpSession).setAttribute(eq(SESSION_DATA_KEY), anyObject());

        // Check that the existing session was invalidated.
        verify(httpSession).invalidate();

        @Nullable final SecurityContext securityContext =
                SessionManager.createSecurityContextForSession(httpServletRequest);

        // Check that the authentication scheme matches the type of session that was intended to be created.
        assert securityContext != null;
        assertThat(securityContext.getAuthenticationScheme(), is(AuthenticationScheme.USERNAME.toString()));

        // Check that the session carries the Identity ID of the authenticated user for which the session was established.
        @Nullable final Principal principal = securityContext.getUserPrincipal();
        assertThat(principal, is(notNullValue()));
        assertThat(principal.getName(), is(REFERENCE_PERSON_ID));
    }

    /**
     * Starting a web session must set the user identifier and the authentication scheme (as {@link
     * AuthenticationScheme#APPTOKEN}) as {@link SessionManager.SessionData} in the {@link HttpSession}.
     */
    @Test
    public void testStartAppSession() {
        LOG.info("testStartAppSession");

        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);

        // Creating a new session must invalidate the old session, hence we have to expect this call.
        Mockito.doNothing().when(httpSession).invalidate();

        // A successfully created session must carry the reference session ID.
        assertThat(impl.startAppSession(identity), is(REFERENCE_SESSION_ID));

        // Check that the session carries the session data.
        verify(httpSession).setAttribute(eq(SESSION_DATA_KEY), anyObject());

        // Check that the existing session was invalidated.
        verify(httpSession).invalidate();

        @Nullable final SecurityContext securityContext =
                SessionManager.createSecurityContextForSession(httpServletRequest);

        // Check that the authentication scheme matches the type of session that was intended to be created.
        assert securityContext != null;
        assertThat(securityContext.getAuthenticationScheme(), is(AuthenticationScheme.APPTOKEN.toString()));

        // Check that the session carries the Identity ID of the authenticated user for which the session was established.
        @Nullable final Principal principal = securityContext.getUserPrincipal();
        assertThat(principal, is(notNullValue()));
        assertThat(principal.getName(), is(REFERENCE_PERSON_ID));
    }

    /**
     * Terminating a session must remove the {@link SessionManager.SessionData}  from an ongoing {@link HttpSession},
     * and then invalidate the session.
     */
    @Test
    public void testTerminateSession() {
        LOG.info("testTerminateSession");

        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);
        Mockito.doNothing().when(httpSession).invalidate();

        impl.terminateSession();

        verify(httpServletRequest).getSession(false);

        // Check that the session data was removed from the session.
        verify(httpSession).removeAttribute(SESSION_DATA_KEY);

        // Check that the session was invalidated.
        verify(httpSession).invalidate();
    }

    /**
     * Terminating a session where no current session is established must not throw and simply do nothing.
     */
    @Test
    public void testTerminateSessionNoCurrentSessionDoesNotThrow() {
        LOG.info("testTerminateSessionNoCurrentSessionDoesNotThrow");

        Mockito.when(httpServletRequest.getSession(false)).thenReturn(null);

        impl.terminateSession();

        verify(httpServletRequest).getSession(false);
    }

    /**
     * Retrieving the current session ID for an ongoing {@link HttpSession} with an authenticated web user must result
     * in the session ID being returned.
     */
    @Test
    public void testGetCurrentSessionIdAuthenticatedWebUser() {
        LOG.info("testGetCurrentSessionIdAuthenticatedWebUser");

        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);

        // Creating a new session must invalidate the old session, hence we have to expect this call.
        Mockito.doNothing().when(httpSession).invalidate();

        // Establish an authenticated web session.
        final String sessionId = impl.startWebSession(identity);
        assertThat(sessionId, is(REFERENCE_SESSION_ID));

        // Check that the returned session ID is the same as when the authenticated session was established.
        assertThat(impl.getCurrentSessionId(), is(sessionId));

        // Called once during {@link SessionManager#startWebSession(Identity)} and once during
        // {@link SessionManager#getCurrentSessionId()}.
        verify(httpServletRequest, Mockito.times(2)).getSession(false);
    }

    /**
     * Retrieving the current session ID for an ongoing {@link HttpSession} with an authenticated app user must result
     * in the session ID being returned.
     */
    @Test
    public void testGetCurrentSessionIdAuthenticatedAppUser() {
        LOG.info("testGetCurrentSessionIdAuthenticatedAppUser");

        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);

        // Creating a new session must invalidate the old session, hence we have to expect this call.
        Mockito.doNothing().when(httpSession).invalidate();

        // Establish an authenticated app session.
        final String sessionId = impl.startAppSession(identity);
        assertThat(sessionId, is(REFERENCE_SESSION_ID));

        // Check that the returned session ID is the same as when the authenticated session was established.
        assertThat(impl.getCurrentSessionId(), is(sessionId));

        // Called once during {@link SessionManager#startWebSession(Identity)} and once during
        // {@link SessionManager#getCurrentSessionId()}.
        verify(httpServletRequest, Mockito.times(2)).getSession(false);
    }

    /**
     * Retrieving the current session ID for an ongoing {@link HttpSession} without an authenticated user must result in
     * the session ID being returned.
     */
    @Test
    public void testGetCurrentSessionIdNoAuthenticatedUser() {
        LOG.info("testGetCurrentSessionIdNoAuthenticatedUser");

        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);

        // Check that the returned session ID matches the reference session ID.
        assertThat(impl.getCurrentSessionId(), is(REFERENCE_SESSION_ID));

        verify(httpServletRequest).getSession(false);
    }

    /**
     * Retrieving the current session ID for where no current session is established must not throw and simply return
     * {@code null}.
     */
    @Test
    public void testGetCurrentSessionIdNoCurrentSession() {
        LOG.info("testGetCurrentSessionIdNoCurrentSession");

        Mockito.when(httpServletRequest.getSession(false)).thenReturn(null);

        assertThat(impl.getCurrentSessionId(), is(nullValue()));

        verify(httpServletRequest).getSession(false);
    }

    /**
     * Creating a security context for an ongoing session with an authenticated user must result in {@link
     * SecurityContext} that contains the {@link Principal} of the authenticated user.
     */
    @Test
    public void testCreateSecurityContextForSession() {
        LOG.info("testCreateSecurityContextForSession");

        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);

        @Nonnull final SessionManager.SessionData sessionData = Mockito.mock(SessionManager.SessionData.class);
        Mockito.doReturn(REFERENCE_PERSON_ID).when(sessionData).getUserId();
        Mockito.doReturn(AuthenticationScheme.USERNAME).when(sessionData).getAuthenticationScheme();

        Mockito.when(httpSession.getAttribute(SESSION_DATA_KEY)).thenReturn(sessionData);

        // Create security context and validate it.
        final SecurityContext securityContext = SessionManager.createSecurityContextForSession(httpServletRequest);

        assertThat(securityContext, is(notNullValue()));
        assertThat(securityContext, is(instanceOf(SecurityContextImpl.class)));

        // Get authentication scheme from security context and ensure that it matches the intended session type.
        assert securityContext != null;
        final String authenticationScheme = securityContext.getAuthenticationScheme();

        assertThat(authenticationScheme, is(notNullValue()));
        assertThat(securityContext.getAuthenticationScheme(), is(AuthenticationScheme.USERNAME.toString()));

        // Get the principal for the authenticated user and ensure that it matches the Identity for which the session
        // was established.
        final Principal userPrincipal = securityContext.getUserPrincipal();

        assertThat(userPrincipal, is(notNullValue()));
        assertThat(userPrincipal.getName(), is(REFERENCE_PERSON_ID));

        verify(httpServletRequest).getSession(false);
        verify(httpSession).getAttribute(SESSION_DATA_KEY);
    }

    /**
     * Creating a security context for an ongoing session without an authenticated user must not throw and simply result
     * in {@code null} being returned.
     */
    @Test
    public void testCreateSecurityContextForSessionNoAuthenticatedUser() {
        LOG.info("testCreateSecurityContextForSessionNoAuthenticatedUser");

        Mockito.when(httpServletRequest.getSession(false)).thenReturn(httpSession);
        Mockito.when(httpSession.getAttribute(SESSION_DATA_KEY)).thenReturn(null);

        assertThat(SessionManager.createSecurityContextForSession(httpServletRequest), is(nullValue()));

        verify(httpServletRequest).getSession(false);
        verify(httpSession).getAttribute(SESSION_DATA_KEY);
    }

    /**
     * Creating a security context when no current session is established must not throw and simply result in {@code
     * null} being returned.
     */
    @Test
    public void testCreateSecurityContextForSessionNoCurrentSession() {
        LOG.info("testCreateSecurityContextForSessionNoCurrentSession");

        Mockito.when(httpServletRequest.getSession(false)).thenReturn(null);

        assertThat(SessionManager.createSecurityContextForSession(httpServletRequest), is(nullValue()));

        verify(httpServletRequest).getSession(false);
    }

    /**
     * {@link SessionManager.SessionData} must be serializable so that session state can be replicated amongst multiple
     * application container nodes.
     */
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testWebSessionDataSerialization() throws IOException, ClassNotFoundException {
        LOG.info("testWebSessionDataSerialization");

        serializeSessionData(AuthenticationScheme.USERNAME);
    }

    /**
     * {@link SessionManager.SessionData} must be serializable so that session state can be replicated amongst multiple
     * application container nodes.
     */
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testAppSessionDataSerialization() throws IOException, ClassNotFoundException {
        LOG.info("testAppSessionDataSerialization");

        serializeSessionData(AuthenticationScheme.APPTOKEN);
    }

    private static void serializeSessionData(
            @Nonnull final AuthenticationScheme authenticationScheme) throws IOException, ClassNotFoundException {
        assert authenticationScheme != null;

        // Create reference session data to serialize.
        final SessionManager.SessionData sessionData =
                new SessionManager.SessionData(REFERENCE_PERSON_ID, authenticationScheme);

        // Serialize the session data.
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        try {
            objectOutputStream.writeObject(sessionData);
        } finally {
            objectOutputStream.close();
        }

        final byte[] objectBuffer = byteArrayOutputStream.toByteArray();

        // Deserialize the session data.
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(objectBuffer);
        @Nullable final SessionManager.SessionData deserializedSessionData;
        final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        try {
            deserializedSessionData = (SessionManager.SessionData) objectInputStream.readObject();
        } finally {
            objectInputStream.close();
        }

        // Check the de-serialized data.
        assertThat(deserializedSessionData, is(notNullValue()));
        assertThat(deserializedSessionData.getUserId(), is(REFERENCE_PERSON_ID));
        assertThat(deserializedSessionData.getAuthenticationScheme(), is(authenticationScheme));
    }
}
