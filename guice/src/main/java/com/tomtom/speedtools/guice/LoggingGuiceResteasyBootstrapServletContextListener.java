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

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public class LoggingGuiceResteasyBootstrapServletContextListener extends GuiceResteasyBootstrapServletContextListener {
    private static final Logger LOG =
            LoggerFactory.getLogger(LoggingGuiceResteasyBootstrapServletContextListener.class);

    @Override
    public void contextInitialized(@Nonnull final ServletContextEvent event) {
        assert event != null;

        @Nullable final ServletContext servletContext = event.getServletContext();
        LOG.debug("contextInitialized: servlet={}", (servletContext == null) ? null :
                servletContext.getServletContextName());
        try {
            super.contextInitialized(event);
        } catch (final RuntimeException e) {
            LOG.error("contextInitialized: Unexpected runtime exception, {}", e);
            //noinspection ProhibitedExceptionThrown
            throw e;
        } catch (final Throwable e) {
            LOG.error("contextInitialized: Unexpected throwable exception, {}", e);
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }


    @Override
    public void contextDestroyed(@Nonnull final ServletContextEvent event) {
        assert event != null;

        @Nullable final ServletContext servletContext = event.getServletContext();
        LOG.debug("contextDestroyed: servlet={}", (servletContext == null) ? null :
                servletContext.getServletContextName());
        try {
            super.contextDestroyed(event);
        } catch (final RuntimeException e) {
            LOG.error("contextDestroyed: Unexpected runtime exception, {}", e);
            //noinspection ProhibitedExceptionThrown
            throw e;
        } catch (final Throwable e) {
            LOG.error("contextDestroyed: Unexpected throwable exception, {}", e);
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }
}
