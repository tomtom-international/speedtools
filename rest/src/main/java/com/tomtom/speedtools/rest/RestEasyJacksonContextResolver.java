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

package com.tomtom.speedtools.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;

import com.tomtom.speedtools.json.Json;

/**
 * This class is the Jackson context resolver for RestEasy. You can extend this class and mark it @Provider to have it
 * found by Resteasy (if auto-scan is switched on).
 */
@Produces(MediaType.APPLICATION_JSON)
public class RestEasyJacksonContextResolver implements ContextResolver<ObjectMapper> {
    private static final Logger LOG = LoggerFactory.getLogger(RestEasyJacksonContextResolver.class);

    public RestEasyJacksonContextResolver() {
        LOG.debug("RestEasyJacksonContextResolver: created RestEasy context resolver");
    }

    @Nonnull
    @Override
    public ObjectMapper getContext(@Nullable final Class<?> type) {
        return Json.getCurrentJsonObjectMapper();
    }
}
