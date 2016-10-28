/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.guice;


import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;
import org.eclipse.jetty.jndi.InitialContextFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class GuiceUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(GuiceUtilsTest.class);

    private static final String PROPERTY_FILE = GuiceUtilsTest.class.getPackage().getName().replaceAll("\\.",
            "/") + "/guiceutilstest.properties";

    @BeforeClass
    public static void setUp() throws NamingException {
        System.setProperty("java.naming.factory.initial", InitialContextFactory.class.getName());
        final InitialContext ctx = new InitialContext();

        try {
            // Set up the java (ee) env sub context.
            ctx.createSubcontext("java:comp/env");

            // Define some dummy properties with speedtools prefix.
            ctx.bind("java:comp/env/speedtools.prop.1", "value");
            ctx.bind("java:comp/env/speedtools.prop.2", "value");
            ctx.bind("java:comp/env/speedtools.prop.3", 100);
            ctx.bind("java:comp/env/speedtools.prop.4", 1L);

            //Define some properties without speedtools prefix.
            ctx.bind("java:comp/env/prop.1", "value");
            ctx.bind("java:comp/env/prop.2", "value");
        } finally {
            ctx.close();
        }
    }

    @Test
    public void testLoadClasspathUrl() throws IOException, NamingException {
        LOG.info("testLoadClasspathUrl");

        final Properties props = new Properties();

        try {
            GuiceUtils.loadUrl(props, "classpath:non-existing-test-config.properties");
            Assert.fail("Did not expect this");
        } catch (final IOException e) {
            // Expected exception.
        }

        GuiceUtils.loadUrl(props, "classpath:" + PROPERTY_FILE);
        Assert.assertEquals(3, props.size());
    }

    @Test
    public void testLoadJndiUrl() throws IOException, NamingException {
        LOG.info("testLoadJndiProperties");

        final Properties props = new Properties();

        GuiceUtils.loadUrl(props, "jndi:java:comp/env" + GuiceUtils.JNDI_PROPETY_PREFIX_PARAM + "speedtools");

        Assert.assertEquals(4, props.size());
        LOG.info("testLoadJndiProperties passed with prefix");

        props.clear();
        GuiceUtils.loadUrl(props, "jndi:java:comp/env");

        Assert.assertEquals(6, props.size());
        LOG.info("testLoadJndiProperties passed without prefix");

        props.clear();
        GuiceUtils.loadUrl(props, "jndi:java:comp");

        Assert.assertEquals(0, props.size());
        LOG.info("testLoadJndiProperties passed without properties");

    }

    @Test
    public void testLoadFileUrl() throws IOException, NamingException {
        LOG.info("testLoadFileProperties");

        final Properties props = new Properties();

        try {
            GuiceUtils.loadUrl(props, "file:non/existing/path/but/if/it/exists/it/is/really/weird.txt");
            Assert.fail("Did not expect to find this");
        } catch (final FileNotFoundException e) {
            // Expected exception.
        }

        // Find file path to classpath resource.
        final URL url = this.getClass().getClassLoader().getResource(PROPERTY_FILE);
        assert url != null;
        final String fileLocation = url.getFile();

        GuiceUtils.loadUrl(props, "file:" + fileLocation);

        Assert.assertEquals(3, props.size());
    }

    @Test
    public void testLoadNonExistingUrl() throws IOException, NamingException {
        LOG.info("testLoadNonExistingUrl");

        final Properties props = new Properties();

        try {
            GuiceUtils.loadUrl(props, "bla:non/existing/protocol");
            Assert.fail("Did not expect to find this");
        } catch (final MalformedURLException e) {
            // Expected exception.
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testPropertiesModule() {
        // Create Mock Binder.
        final Binder binder = Mockito.mock(Binder.class);

        // Bind mock methods. Only the ones that are called in the process are required.
        Mockito.when(binder.skipSources(Names.class)).thenReturn(binder);

        Mockito.when(binder.bind(Mockito.any(Key.class))).thenReturn(Mockito.mock(LinkedBindingBuilder.class));

        final Module module = new GuiceConfigurationModule("classpath:" + PROPERTY_FILE);
        Assert.assertNotNull(module);

        module.configure(binder);
        Mockito.verify(binder, Mockito.times(0)).addError(Mockito.any(Throwable.class));


        // This will cause a NamingException that will be caught and added to the binder.
        final Module badModule = new GuiceConfigurationModule("jndi:" + PROPERTY_FILE);
        Assert.assertNotNull(badModule);

        badModule.configure(binder);
        Mockito.verify(binder, Mockito.times(1)).addError(Mockito.any(Throwable.class));

        // Difficult to test IOException flow in GuiceUtils.bindProperties.
    }
}
