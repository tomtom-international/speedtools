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

package com.tomtom.speedtools.json;

/**
 * Implemented by objects that are able to render themselves in JSON.
 */
public interface JsonRenderable {

    /**
     * Provide a Json representation of a domain object. Typical implementations look like this:
     *
     * public String toJson() { return Json.toJson(this); }
     *
     * public String toString() { return Json.toStringJson(this); }
     *
     * @return Json representation of the object.
     */
    public String toJson();
}
