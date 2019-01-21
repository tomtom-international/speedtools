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

package com.tomtom.speedtools.rest;

import scala.concurrent.Future;

import javax.annotation.Nonnull;

/**
 * This class defines a resource handler for API resources. The handler processes the request asynchronously by
 * returning a Future.
 */
public interface ResourceHandler {

    /**
     * The process() call calls the actual processor.
     * <p>
     * Note that actual implementations of resource handlers should specify the &gt;?&lt; type parameter of the
     * Future to match the specific response type.
     *
     * @return Future with {@link javax.ws.rs.container.AsyncResponse} object.
     * @throws Exception When something went wrong. Will be translated to a "Internal Server Error" response (500).
     */
    @SuppressWarnings({"ProhibitedExceptionDeclared", "RedundantThrows"})
    @Nonnull
    public Future<?> process() throws Exception;
}
