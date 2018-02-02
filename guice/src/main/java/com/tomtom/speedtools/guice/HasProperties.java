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

package com.tomtom.speedtools.guice;

/**
 * Tagging interface that classes with configuration parameters can inherit from. Doing so consistently will aid finding
 * all configurable classes in a system.
 *
 * Properties can use values from environment variables, with the following syntax:
 * <pre>
 *     property=${VAR:=default}
 * </pre>
 *
 * This uses the environment variable 'VAR' in a property value, with a
 * default value of 'default', if the environment variables wasn't set.
 *
 * You can also combine this with other text and other environment variables, like:
 * <pre>
 *     myPath=${HOME}/${SOURCE_BASEDIR:=sources}
 * </pre>
 *
 * Default values are literal strings. Environment variables cannot be used nested for default values,
 * but you can assign either the null string (no characters), or the value of the `Properties.emptyString`,
 * which is normally `{empty}`.
 *
 * Assigning the null string leaves the property undefined, resulting in a start-up error which
 * notifies you you have forgotten to set abn environment variable.
 *
 * Assigning the value of `Properties.emptyString` sets the property to an empty string "".
 */
public interface HasProperties {

    // Special property to identify empty strings. Properties with this value may be left empty.
    public static String PROPERTIES_EMPTY_STRING = "Properties.emptyString";
}
