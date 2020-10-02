/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.spring.boot.internal.factories;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SpringFactoriesRewriter {
    private static final Map<Pattern, Function<URL, Stream<URL>>> MAPPERS = new HashMap<>();

    static {
        MAPPERS.put(Pattern.compile("spring-boot-autoconfigure-\\p{Digit}+\\.\\p{Digit}+\\.\\p{Digit}+\\..+\\.jar"), url ->
        {
            try {
                return Stream.of(url, new URL(
                        url.getProtocol(), url.getHost(), url.getPort(), url.getFile() + "_karaf.jar",
                        new StringURLStreamHandler(("" +
                                "org.springframework.boot.autoconfigure.EnableAutoConfiguration=" +
                                "org.apache.karaf.spring.boot.services.web.HttpServiceServletServerFactory$SpringConfiguration" +
                                "").getBytes(StandardCharsets.UTF_8))));
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    private SpringFactoriesRewriter() {
        // no-op
    }

    // used from generated classes
    public static Stream<URL> rewrite(final URL factoriesUrl) {
        return Stream.of(factoriesUrl)
                .map(URLWithJarName::new)
                .flatMap(url -> MAPPERS.entrySet().stream()
                        .filter(it -> it.getKey().matcher(url.jarName).matches())
                        .flatMap(it -> it.getValue().apply(url.url)));
    }

    // here we can rewrite the factories if needed
    private static Stream<URL> wrap(final URL url, final Function<String, String> rewriter) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[512];
        int read;
        try (final InputStream stream = url.openStream()) {
            while ((read = stream.read(tmp)) >= 0) {
                buffer.write(tmp, 0, read);
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return Stream.of(newInMemoryURL(url, buffer.toByteArray()));
    }

    private static URL newInMemoryURL(final URL url, final byte[] content) {
        try {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), new StringURLStreamHandler(content));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static class URLWithJarName {
        private final String jarName;
        private final URL url;

        private URLWithJarName(final URL url) {
            this.url = url;
            final String string = url.toExternalForm();
            final int resource = string.lastIndexOf(".jar!");
            if (resource < 0) {
                throw new IllegalArgumentException(string);
            }
            final String base = string.substring(0, resource + ".jar".length()).replace(File.separatorChar, '/');
            final int lastSep = base.lastIndexOf('/');
            if (lastSep < 0) {
                throw new IllegalArgumentException(base);
            }
            this.jarName = base.substring(lastSep + 1);
        }
    }

    private static class StringURLStreamHandler extends URLStreamHandler {
        private final byte[] content;

        private StringURLStreamHandler(final byte[] content) {
            this.content = content;
        }

        @Override
        protected URLConnection openConnection(final URL u) {
            return new StringURLConnection(u, content);
        }
    }

    private static class StringURLConnection extends URLConnection {
        private final byte[] content;

        private StringURLConnection(final URL u, final byte[] content) {
            super(u);
            this.content = content;
        }

        @Override
        public void connect() {
            // no-op
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }
    }
}
