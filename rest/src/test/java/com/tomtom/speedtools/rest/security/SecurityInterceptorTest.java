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
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.MethodInjector;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.metadata.ResourceLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.ArgumentMatchers.any;

public class SecurityInterceptorTest {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityInterceptorTest.class);

    private final SecurityInterceptor securityInterceptor = new SecurityInterceptor();

    private final InjectorFactory injectorFactory = Mockito.mock(InjectorFactory.class);
    private final ResourceFactory resourceFactory = Mockito.mock(ResourceFactory.class);
    private final ResteasyProviderFactory resteasyProviderFactory = Mockito.mock(ResteasyProviderFactory.class);
    private final MethodInjector methodInjector = Mockito.mock(MethodInjector.class);

    @Mock
    private CustomServlet3AsyncHttpRequest asyncHttpRequest;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(httpServletRequest.getSession()).thenReturn(null);
        Mockito.when(httpServletRequest.getSession(false)).thenReturn(null);
        Mockito.when(asyncHttpRequest.getHttpServletRequest()).thenReturn(httpServletRequest);
        Mockito.when(asyncHttpRequest.getHttpServletRequest()).thenReturn(httpServletRequest);
        Mockito.when(injectorFactory.createMethodInjector(any(ResourceLocator.class), any(ResteasyProviderFactory.class))).thenReturn(methodInjector);
    }

    @Test
    public void testAuthentication() throws NoSuchMethodException {
        LOG.info("testAuthentication");

        // Check if call is intercepted.
        Assert.assertTrue(securityInterceptor.accept(SampleResource.class, SampleResource.class.getMethods()[0]));
        Assert.assertTrue(securityInterceptor.accept(SampleResource.class, SampleResource.class.getMethods()[1]));
        Assert.assertTrue(securityInterceptor.accept(SampleResource.class,
                SampleResource.class.getMethod("getOne", AsyncResponse.class, SecurityContext.class)));

        // Remove authentication for entire package.
        securityInterceptor.disableAuthenticationForClass(SampleResource.class);

        // Check if call will not be intercepted.
        Assert.assertFalse(securityInterceptor.accept(SampleResource.class, SampleResource.class.getMethods()[0]));
        Assert.assertFalse(securityInterceptor.accept(SampleResource.class, SampleResource.class.getMethods()[1]));
        Assert.assertFalse(securityInterceptor.accept(SampleResource.class,
                SampleResource.class.getMethod("getOne", AsyncResponse.class, SecurityContext.class)));

        // Remove authentication for entire package.
        securityInterceptor.enableAuthenticationForClass(SampleResource.class);

        // Check if call is intercepted.
        Assert.assertTrue(securityInterceptor.accept(SampleResource.class, SampleResource.class.getMethods()[0]));
        Assert.assertTrue(securityInterceptor.accept(SampleResource.class, SampleResource.class.getMethods()[1]));
        Assert.assertTrue(securityInterceptor.accept(SampleResource.class,
                SampleResource.class.getMethod("getOne", AsyncResponse.class, SecurityContext.class)));

        // Remove authentication for "getOne".
        securityInterceptor.disableAuthenticationForMethod(SampleResource.class, "getOne");

        // Check if call will not be intercepted.
        Assert.assertFalse(securityInterceptor.accept(SampleResource.class,
                SampleResource.class.getMethod("getOne", AsyncResponse.class, SecurityContext.class)));
        Assert.assertTrue(securityInterceptor.accept(SampleResource.class,
                SampleResource.class.getMethod("getTwo", AsyncResponse.class, SecurityContext.class)));

    }
}
