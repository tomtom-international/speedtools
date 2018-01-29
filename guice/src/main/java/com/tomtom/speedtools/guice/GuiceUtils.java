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

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.tomtom.speedtools.objects.Immutables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.naming.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for Guice dependency injection.
 */
public final class GuiceUtils {
    private static final Logger LOG = LoggerFactory.getLogger(GuiceUtils.class);

    private static final String CLASSPATH_URL_PREFIX = "classpath:";
    private static final String JNDI_URL_PREFIX = "jndi:";
    static final String JNDI_PROPETY_PREFIX_PARAM = "?prefix=";

    // These properties should not be printed.
    private static final Set<String> PROPERTY_VALUES_TO_HIDE = Immutables.setOf("password", "secret");
    private static final String PROPERTY_VALUES_HIDDEN = "******** (hidden)";

    /**
     * Private ctor. Prevent construction.
     */
    private GuiceUtils() {
        super();
        assert false;
    }

    /**
     * Property file urls are typically file:// or classpath: urls. Secondary property files may only contain properties
     * found in the primary property file.
     *
     * For more information on the format of properties, {@link HasProperties}.
     *
     * @param binder                      Guice binder.
     * @param primaryPropertySourceUrl    Primary property URL.
     * @param secondaryPropertySourceUrls Zero or more secondary property URLs.
     */
    public static void bindProperties(
            @Nonnull final Binder binder,
            @Nonnull final String primaryPropertySourceUrl,
            @Nonnull final String... secondaryPropertySourceUrls) {
        assert binder != null;
        assert primaryPropertySourceUrl != null;
        assert secondaryPropertySourceUrls != null;

        LOG.info("bindProperties:");
        LOG.info("------------------------------------------------------------------------------------");

        // Load primary properties.
        final Properties properties = new Properties();
        loadUrl(binder, properties, primaryPropertySourceUrl);

        // Load secondary properties.
        for (final String url : secondaryPropertySourceUrls) {
            final Properties secondaryProperties = new Properties();
            loadUrl(binder, secondaryProperties, url);

            // Check that properties are defined in primary property file and copy them to properties.
            for (final String name : secondaryProperties.stringPropertyNames()) {

                // Property should be defined in primary property file.
                if (!properties.containsKey(name)) {
                    final String msg = "Unknown property " + name + " in property file " + url + '.';
                    LOG.error("{}", msg);
                    binder.addError(msg);
                }

                // Copy secondary properties to primary.
                properties.setProperty(name, secondaryProperties.getProperty(name));
            }
        }

        // Get specific empty string property.
        final String emptyString = properties.getProperty(HasProperties.PROPERTIES_EMPTY_STRING);
        if ((emptyString == null) || emptyString.trim().isEmpty()) {
            final String msg = "Missing value for special 'empty string' property " +
                    HasProperties.PROPERTIES_EMPTY_STRING + '.';
            LOG.error("{}", msg);
            binder.addError(msg);
        }
        LOG.info("  (In the property files, empty strings were specified as '{}' by {}.)",
                emptyString, HasProperties.PROPERTIES_EMPTY_STRING);

        // Expand environment variables (and collect errors).
        expandProperties(binder, properties);

        // Check that all properties have been set.
        for (final String name : properties.stringPropertyNames()) {
            final String value = properties.getProperty(name);
            if ((value == null) || value.trim().isEmpty()) {
                final String msg = "Missing value for property " + name + " (use '" + emptyString +
                        "' for empty properties instead, defined by " +
                        HasProperties.PROPERTIES_EMPTY_STRING + ')';
                LOG.error("{}", msg);
                binder.addError(msg);
            } else if (value.equals(emptyString)) {
                properties.setProperty(name, "");
            } else {
                // Leave as-is.
            }
        }

        // Always output properties when starting application.
        final Collection<String> propertyNames = properties.stringPropertyNames();
        final List<String> sortedPropertyNames = new ArrayList<>();
        sortedPropertyNames.addAll(propertyNames);
        Collections.sort(sortedPropertyNames);
        int n = 1;
        for (final String propertyName : sortedPropertyNames) {
            boolean found = false;
            for (final String hide : PROPERTY_VALUES_TO_HIDE) {
                if (propertyName.endsWith(hide)) {
                    found = true;
                    break;
                }
            }
            final String propertyValue;
            if (found) {
                propertyValue = PROPERTY_VALUES_HIDDEN;
            } else {
                propertyValue = properties.getProperty(propertyName);
            }
            LOG.info("  {}. {} = {}", n, propertyName, propertyValue);
            ++n;
        }
        LOG.info("------------------------------------------------------------------------------------");

        Names.bindProperties(binder, properties);
    }

    private static void loadUrl(
            @Nonnull final Binder binder,
            @Nonnull final Properties properties,
            @Nonnull final String url) {
        assert binder != null;
        assert properties != null;
        assert url != null;

        try {
            loadUrl(properties, url.trim());
        } catch (final IOException | NamingException e) {
            binder.addError(e);
        }
    }

    static void loadUrl(@Nonnull final Properties properties, @Nonnull final String url)
            throws IOException, NamingException {
        assert properties != null;
        assert url != null;

        LOG.info("loadUrl: Loading properties from: {}", url);
        final InputStream is = openUrl(url);
        try {
            if (is == null) {
                LOG.error("loadUrl: Properties not found at: {}", url);
                throw new IOException("Properties not found at " + url);
            }
            properties.load(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    private static InputStream openUrl(@Nonnull final String url) throws IOException, NamingException {
        assert url != null;

        if (url.startsWith(CLASSPATH_URL_PREFIX)) {
            int index = CLASSPATH_URL_PREFIX.length();
            while ((index < url.length()) && (url.charAt(index) == '/')) {
                index++;
            }
            return GuiceUtils.class.getClassLoader().getResourceAsStream(url.substring(index));
        }

        if (url.startsWith(JNDI_URL_PREFIX)) {
            final String propertyPrefix;
            final String path;
            final Properties collectedProperties = new Properties();

            // Check for prefix param.
            if (url.contains("?")) {
                propertyPrefix =
                        url.substring(url.indexOf(JNDI_PROPETY_PREFIX_PARAM) + JNDI_PROPETY_PREFIX_PARAM.length());
                path = url.substring(JNDI_URL_PREFIX.length(), (url.indexOf(JNDI_PROPETY_PREFIX_PARAM)));
            } else {
                propertyPrefix = "";
                path = url.substring(JNDI_URL_PREFIX.length());
            }

            final InitialContext ic = new InitialContext();
            try {
                final NamingEnumeration<NameClassPair> props = ic.list(path);
                //noinspection NestedTryStatement
                try {
                    while (props.hasMore()) {
                        final NameClassPair prop = props.next();
                        final String propName = prop.getName();
                        if (propName.startsWith(propertyPrefix)) {
                            final Object propValue = ic.lookup(path + '/' + propName);

                            // Skip sub context.
                            if (!(propValue instanceof Context)) {
                                // Add property to collected properties.
                                collectedProperties.setProperty(propName,
                                        propValue.toString()); //Explicitly store String value.
                            }
                        }

                    }
                } finally {
                    props.close();
                }
            } finally {
                ic.close();
            }

            // Convert Properties to InputStream
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            collectedProperties.store(out, "Loaded JNDI properties");
            return new ByteArrayInputStream(out.toByteArray());
        }
        return new URL(url).openStream();
    }

     // This regular expression matches a full env.var substitution pattern, like "${VAR}" and "${VAR:=123}".
    static final Pattern REGEX_FULL_ENVVAR = Pattern.compile("\\$\\{.*?\\}");

    // This regular expression matches the env.var name and its value in separate groups.
    static final Pattern REGEX_ONLY_NAME = Pattern.compile("\\$\\{(.*?)(:=.*)?\\}");

    static void expandProperties(
            @Nonnull final Binder binder,
            @Nonnull final Properties properties) {

        // Process all properties.
        for (final String name : properties.stringPropertyNames()) {
            boolean appliedSubstitution;
            do {

                // If a substitution is applied, we need to execute this loop once more, for the next env.var.
                appliedSubstitution = false;
                final String value = properties.getProperty(name);

                // The property value must not be null or empty for the regex checking.
                if ((value != null) && !value.trim().isEmpty()) {

                    // Check if a substitution pattern is found.
                    final Matcher matchFullSpec = REGEX_FULL_ENVVAR.matcher(value);
                    if (matchFullSpec.find()) {

                        // If a substitution pattern is found, use that to fetch the variable name and default value.
                        final String matchedAssignment = value.substring(matchFullSpec.start(), matchFullSpec.end());

                        // Check if we can find the env.var name and its default value (optional).
                        final Matcher matchedName = REGEX_ONLY_NAME.matcher(matchedAssignment);

                        // The regex has two groups. Group 1 is the name, group 2 is the default value (prefix with ':=').
                        if (matchedName.find() && (matchedName.groupCount() == 2)) {
                            final String envVarName = matchedName.group(1);
                            final String suppliedDefaultValue = (matchedName.group(2) == null) ? null : matchedName.group(2).substring(2);

                            // Safety check to see if the name does not contain a $, { or }. Nested env.vars are not allowed.
                            if (envVarName.matches(".*?[${}].*")) {
                                final String msg = "Property " + name + " uses incorrect syntax: " + envVarName;
                                LOG.error("{}", msg);
                                binder.addError(msg);
                                properties.remove(name);
                            } else {

                                // Get the actual env.var value.
                                @SuppressWarnings("CallToSystemGetenv") final String valueFromEnv = System.getenv(envVarName);
                                final String valueToBeUsed;
                                if (valueFromEnv == null) {
                                    if (suppliedDefaultValue == null) {

                                        // The env.var does not exist and there's no default. Remove the property.
                                        valueToBeUsed = null;
                                    } else {

                                        // The env.var does not exist but there's a default. Use the default.
                                        valueToBeUsed = suppliedDefaultValue;
                                    }
                                } else {

                                    // The env.var exists. Use it.
                                    valueToBeUsed = valueFromEnv;
                                }

                                // If the value to be used was not found, the entire property gets undefined.
                                if (valueToBeUsed == null) {
                                    properties.remove(name);
                                } else {

                                    // Otherwise, substitute the env.var with its value.
                                    final String substitutedString = matchFullSpec.replaceFirst(valueToBeUsed);
                                    properties.setProperty(name, substitutedString);

                                    // Execute the loop again, to resolve other env.vars.
                                    appliedSubstitution = true;
                                }
                            }
                        }
                    }
                }
            } while (appliedSubstitution);
        }
    }
}
