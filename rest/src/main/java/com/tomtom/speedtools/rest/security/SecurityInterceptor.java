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

import com.tomtom.speedtools.rest.CustomServlet3AsyncHttpRequest;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * By default, authentication for all classes is enabled and no methods have individual authentication set
 * explicitly (because they are covered by the class-level authentication).
 *
 * If you disabled authentication for a specific class, all methods that jave their authentication explicitly
 * set, will still require authentication.
 */
@Provider
@ServerInterceptor
@SecurityPrecedence
public class SecurityInterceptor implements PreProcessInterceptor, AcceptedByMethod {
    @Nonnull
    private static final Logger LOG = LoggerFactory.getLogger(SecurityInterceptor.class);

    public SecurityInterceptor() {
        // Ready for injection.
    }

    /**
     * Enable or disabled authorization for an entire class. By default authorization is enabled for
     * all classes.
     *
     * <pre>
     *      disableAuthenticationForClass(SomeResource.class);
     * </pre>
     *
     * @param declaring Class.
     */
    public void disableAuthenticationForClass(@Nonnull final Class<?> declaring) {
        LOG.debug("disableAuthenticationForClass: {}", declaring.getName());
        disabledForClasses.add(declaring.getName());
    }

    public void enableAuthenticationForClass(@Nonnull final Class<?> declaring) {
        LOG.debug("enableAuthenticationForClass: {}", declaring.getName());
        disabledForClasses.remove(declaring.getName());
    }

    /**
     * Enabled or disabled authorization for a single method. This overrides class-level
     * requirements.
     *
     * <pre>
     *      enableAuthenticationForMethod(SomeResource.class, "someMethod");
     * </pre>
     *
     * @param declaring  Class.
     * @param methodName Method name.
     */
    public void enableAuthenticationForMethod(@Nonnull final Class<?> declaring, @Nonnull final String methodName) {
        final String name = declaring.getName() + '.' + methodName;
        LOG.debug("enableAuthenticationForMethod: {}", name);
        checkMethodExists(declaring, methodName);
        enabledForMethods.add(name);
        disabledForMethods.remove(name);
    }

    public void disableAuthenticationForMethod(@Nonnull final Class<?> declaring, @Nonnull final String methodName) {
        final String name = declaring.getName() + '.' + methodName;
        LOG.debug("disableAuthenticationForMethod: {}", name);
        checkMethodExists(declaring, methodName);
        enabledForMethods.remove(name);
        disabledForMethods.add(name);
    }

    /**
     * Allow, but do not require, authentication for a method.
     * <pre>
     *      optionalAuthorizationForMethod(SomeResource.class, "getImage");
     * </pre>
     *
     * @param declaring  Class.
     * @param methodName Method name.
     */
    public void optionalAuthorizationForMethod(@Nonnull final Class<?> declaring, @Nonnull final String methodName) {
        final String name = declaring.getName() + '.' + methodName;
        LOG.debug("optionalAuthorizationForMethod: {}", name);
        checkMethodExists(declaring, methodName);
        optionalForMethods.add(name);
    }

    public void strictAuthorizationForMethod(@Nonnull final Class<?> declaring, @Nonnull final String methodName) {
        final String name = declaring.getName() + '.' + methodName;
        LOG.debug("strictAuthorizationForMethod: {}", name);
        checkMethodExists(declaring, methodName);
        optionalForMethods.remove(declaring.getName() + '.' + methodName);
    }

    @SuppressWarnings("MethodMayBeStatic")
    private void checkMethodExists(@Nonnull final Class<?> declaring, @Nonnull final String methodName) {
        for (final Method method : declaring.getMethods()) {
            if (method.getName().equals(methodName)) {
                return;
            }
        }
        LOG.error("checkMethodExists: method {}.{} does not exist", declaring.getName(), methodName);
        throw new IllegalStateException("Error configuring security for methods in classes.\n" +
                "Method " + declaring.getName() + '.' + methodName + " does not exist!\n" +
                "You may need to check the method name strings in your SecurityInterceptor configurer.");
    }

    /**
     * This sets specify authorization requirements for classes and individual methods.
     */
    private final Set<String> disabledForClasses = new HashSet<>();
    private final Set<String> enabledForMethods = new HashSet<>();
    private final Set<String> disabledForMethods = new HashSet<>();

    /**
     * This array registers all resources that support authorization checks, but are not required. The operations itself
     * determine whether authentication is required. Only if the user is logged in the SecurityContext will be added.
     */
    private final Set<String> optionalForMethods = new HashSet<>();

    @SuppressWarnings("rawtypes")
    @Override
    public boolean accept(@Nonnull final Class declaring, @Nonnull final Method method) {
        assert declaring != null;
        assert method != null;

        final String className = declaring.getName();
        final String methodName = className + '.' + method.getName();

        // Check if authorization is explicitly enabled for this method.
        for (final String enabledMethod : enabledForMethods) {
            if (enabledMethod.equals(methodName)) {
                LOG.debug("accept: authorization required for method, class={}, method={}", className, methodName);
                return true;
            }
        }

        // Check if authorization is explicitly disabled for this method.
        for (final String disabledMethod : disabledForMethods) {
            if (disabledMethod.equals(methodName)) {
                LOG.debug("accept: NO authorization required for method, class={}, method={}", className, methodName);
                return false;
            }
        }

        // Check if authorization is explicitly disabled for this class.
        for (final String disabledClass : disabledForClasses) {
            if (disabledClass.equals(className)) {
                LOG.debug("accept: NO authorization required for class, class={}, method={}", className, methodName);
                return false;
            }
        }

        // Authorization required.
        LOG.debug("accept: authorization required by default, class={}, method={}", className, methodName);
        return true;
    }

    /**
     * This implementation determines whether a user is logged in. In case a session has not been started this method
     * will return an unauthorized response. In case a session is present but lacks the username it also returns an
     * unauthorized response.
     *
     * When the username is present, an instance of SecurityContextImpl is created and pushed onto RestEasy's context
     * stack. Rest methods that are target of this interceptor and rely on the SecurityContext being injected, will get
     * this custom implementation.
     *
     * @param request Initial HttpRequest.
     * @param method  The Java Method that will be invoked.
     * @return An Unauthorized response in case not logged in, else null
     */
    @Nullable
    @Override
    public ServerResponse preProcess(@Nonnull final HttpRequest request, @Nonnull final ResourceMethodInvoker method) {
        assert request != null;
        assert method != null;

        // Check whether this is an authenticated session, and get the {@link SecurityContext} for it.
        @Nullable final SecurityContext securityContext = SessionManager.createSecurityContextForSession(
                ((CustomServlet3AsyncHttpRequest) request).getHttpServletRequest());

        if (securityContext == null) {

            // Assume authentication is required, unless.
            boolean authenticationRequired = true;

            final String methodName = method.getResourceClass().getName() + '.' + method.getMethod().getName();
            for (final String excludedMethod : optionalForMethods) {
                if (excludedMethod.equals(methodName)) {

                    // No authentication required.
                    authenticationRequired = false;
                }
            }

            if (authenticationRequired) {

                // In case not logged in, send 401 response.
                final ServerResponse response = new ServerResponse();
                response.setStatus(HttpResponseCodes.SC_UNAUTHORIZED);
                final MultivaluedMap<String, Object> headers = new Headers<>();
                headers.add("Content-Type", "text/plain");
                response.setMetadata(headers);
                response.setEntity("Error 401 Unauthorized: " + request.getUri().getPath());
                return response;
            }
        } else {

            // Push SecurityContext to RestEasy context stack
            ResteasyProviderFactory.pushContext(SecurityContext.class, securityContext);
        }
        return null;
    }
}

