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
package org.apache.karaf.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class SpringBootServiceIT {
    @Inject
    private BundleContext ctx;

    @Inject
    private HttpService httpService;

    @Inject
    private SpringBootService springBootService;

    @Test
    public void run() throws Exception {
        final Path springBootApp = Paths.get("src/test/resources/rest-service-0.0.1-SNAPSHOT.jar");
        assertTrue(Files.exists(springBootApp));
        final String install = springBootService.install(null, springBootApp.toUri(), null);
        try {
            springBootService.start(install, new String[0]);
            assertEquals("{\"id\":1,\"content\":\"Hello, World!\"}", getWithRetry());
        } finally {
            springBootService.stop(install);
            // todo: uninstall
        }
    }

    private String getWithRetry() throws MalformedURLException {
        final ServiceReference<HttpService> ref = ctx.getServiceReference(HttpService.class);
        final URL url = new URL("http://localhost:" + ref.getProperty("org.osgi.service.http.port") + "/greeting");
        for (int i = 0; i < 240; i++) {
            try {
                final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        return reader.lines().collect(joining("\n")).trim();
                    }
                }
            } catch (final IOException ioe) {
                // wait
            }
            try {
                Thread.sleep(250);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return "failed";
    }
}
