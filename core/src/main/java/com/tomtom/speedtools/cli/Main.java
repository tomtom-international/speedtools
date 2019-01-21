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

package com.tomtom.speedtools.cli;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Stub to execute a main method on the command line from a WAR file. The first argument on the command line is the
 * fully specified name of the "Main" class. The second argument is the name of the (public static void) "main" method.
 */
public final class Main {

    private Main() {
        // Prevent instantiation.
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public static void main(@Nonnull final String[] args)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IOException {
        assert args != null;

        if (args.length < 2) {
            throw new IllegalStateException("Argument 1 must be full class name, argument 2 must be method name.");
        }
        final String mainClassName = args[0];
        final String mainClassMethod = args[1];
        final String[] otherArgs = Arrays.copyOfRange(args, 2, args.length - 1);

        final List<URL> newUrls = new ArrayList<>();
        URL.setURLStreamHandlerFactory(new NestedJarURLStreamHandlerFactory());
        final String warFile = getWarFile();
        try (final JarFile jarFile = new JarFile(warFile)) {
            //noinspection ForLoopWithMissingComponent
            for (final Enumeration<JarEntry> entryEnum = jarFile.entries(); entryEnum.hasMoreElements(); ) {
                final JarEntry entry = entryEnum.nextElement();
                if (entry.getName().endsWith(".jar")) {
                    newUrls.add(new URL(
                            "jar:nestedjar:file:" + warFile + "~/" + entry.getName() + "!/"));

                }
            }
        }
        try (final URLClassLoader newClassLoader = new URLClassLoader(newUrls.toArray(new URL[newUrls.size()]))) {
            final Class<?> mainClass = newClassLoader.loadClass(mainClassName);
            final Method method = mainClass.getMethod(mainClassMethod, String[].class);
            method.invoke(null, (Object) otherArgs);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("The program failed. An exception was thrown during execution.",
                    e.getCause());
        }
    }

    @Nonnull
    private static String getWarFile() {
        final URLClassLoader classLoader = (URLClassLoader) Main.class.getClassLoader();
        for (final URL url : classLoader.getURLs()) {
            if (url.toString().endsWith(".war")) {
                return url.getFile();
            }
        }
        throw new IllegalStateException("Should be run from the war file.");
    }

    static class NestedJarURLConnection extends URLConnection {
        private final URLConnection connection;
        public final static char SEPARATOR_CHAR = '~';
        public final static String SEPARATOR = SEPARATOR_CHAR + "/";

        @SuppressWarnings("OverlyBroadThrowsClause")
        NestedJarURLConnection(@Nonnull final URL url)
                throws IOException {
            super(url);
            assert url != null;
            connection = new URL(url.getFile()).openConnection();
        }

        @Override
        public void connect() throws IOException {
            if (!connected) {
                connection.connect();
                connected = true;
            }
        }

        @Override
        @Nonnull
        public InputStream getInputStream() throws IOException {
            connect();
            return connection.getInputStream();
        }
    }

    static class NestedJarURLStreamHandlerFactory implements URLStreamHandlerFactory {

        @Nullable
        @Override
        public URLStreamHandler createURLStreamHandler(@Nonnull final String protocol) {
            assert protocol != null;
            if (protocol.equals("nestedjar")) {
                return new JarJarURLStreamHandler();
            }
            return null;
        }
    }

    static class JarJarURLStreamHandler extends URLStreamHandler {

        @SuppressWarnings("DuplicateThrows")
        @Nonnull
        @Override
        protected URLConnection openConnection(@Nonnull final URL u) throws IOException {
            assert u != null;
            return new NestedJarURLConnection(u);
        }

        @Override
        protected void parseURL(@Nonnull final URL u, @Nonnull final String spec, final int start, final int limit) {
            assert u != null;
            assert spec != null;
            final String file = "jar:" + spec.substring(start, limit).replaceFirst("\\~/", "!/");
            setURL(u, "nestedjar", "", -1, null, null, file, null, null);

        }
    }
}
