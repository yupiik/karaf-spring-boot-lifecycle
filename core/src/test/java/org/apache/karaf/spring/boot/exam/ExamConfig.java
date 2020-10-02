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
package org.apache.karaf.spring.boot.exam;

import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

public class ExamConfig implements ConfigurationFactory {
    private final String karafVersion = System.getProperty("karaf.version", "4.3.0.RC1");
    private final String asmVersion = System.getProperty("asm.version", "9.0");

    @Override
    public Option[] createConfiguration() {
        configureLogs();
        try {
            return Stream.of(
                    karafDistributionConfiguration()
                            .runEmbedded(true)
                            .frameworkUrl(normalize(findKaraf()).getURL())
                            .useDeployFolder(false),
                    editConfigurationFilePut( // ensure we see log service logs
                            "etc/system.properties",
                            "org.ops4j.pax.logging.DefaultServiceLog.level",
                            "INFO"),
                    editConfigurationFilePut( // ensure we have logs
                            "etc/org.ops4j.pax.logging.cfg",
                            "log4j2.rootLogger.level",
                            "INFO"),
                    keepRuntimeFolder(),
                    bundle(maven().groupId("org.ow2.asm").artifactId("asm").version(asmVersion).getURL()),
                    bundle(maven().groupId("org.ow2.asm").artifactId("asm-tree").version(asmVersion).getURL()),
                    bundle(maven().groupId("org.ow2.asm").artifactId("asm-commons").version(asmVersion).getURL()),
                    // pax-web requires more work, let's just switch to felix.jetty
                    bundle(maven().groupId("org.osgi").artifactId("org.osgi.service.http").version("1.2.1").getURL()),
                    bundle(maven().groupId("org.osgi").artifactId("org.osgi.service.http.whiteboard").version("1.1.0").getURL()),
                    bundle(maven().groupId("org.apache.felix").artifactId("org.apache.felix.http.servlet-api").version("1.1.2").getURL()),
                    bundle(maven().groupId("org.apache.felix").artifactId("org.apache.felix.http.jetty").version("4.0.20").getURL()),
                    bundle(findModule().normalize().toAbsolutePath().toUri().toURL().toExternalForm()).start())
                    .toArray(Option[]::new);
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private Path findModule() throws IOException {
        return Files.list(Paths.get("target/"))
                .filter(it -> {
                    final String name = it.getFileName().toString();
                    return name.startsWith("org.apache.karaf.spring-boot.core-") && name.endsWith(".jar") &&
                            !name.contains("-source") && !name.contains("-javadoc");
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ensure to build the module before running IT"));
    }

    private UrlProvisionOption normalize(final Path path) throws MalformedURLException {
        return url(path
                .normalize()
                .toAbsolutePath()
                .toUri()
                .toURL()
                .toExternalForm());
    }

    private Path m2() {
        return ofNullable(System.getProperty("test.m2"))
                .map(Paths::get)
                .orElseGet(() -> Paths.get(System.getProperty("user.home")).resolve(".m2/repository"));
    }

    private Path findKaraf() {
        return m2()
                .resolve("org/apache/karaf/apache-karaf")
                .resolve(karafVersion)
                .resolve("apache-karaf-" + karafVersion + ".tar.gz");
    }

    private void configureLogs() {
        System.setProperty("karaf.log.console", "INFO");
        final LogManager logManager = LogManager.getLogManager();
        logManager.addLogger(KeepRef.KARAF_MAIN_LOGGER);
        logManager.addLogger(KeepRef.KARAF_LOCK_LOGGER);
        logManager.addLogger(KeepRef.KARAF_SHUTDOWN_LOGGER);
    }

    private static final class KeepRef { // keep a ref to ensure it is not gc after we configured it
        private static final Logger KARAF_LOCK_LOGGER = newLogger("org.apache.karaf.main.lock.SimpleFileLock");
        private static final Logger KARAF_MAIN_LOGGER = newLogger("org.apache.karaf.main.Main");
        private static final Logger KARAF_SHUTDOWN_LOGGER = newLogger("org.apache.karaf.main.ShutdownSocketThread");

        private KeepRef() {
            // no-op
        }

        private static Logger newLogger(final String name) {
            return new Logger(name, null) { // fake same output as pax bootstrap
                {
                    super.setLevel(Level.INFO);
                    super.setUseParentHandlers(false);
                    super.addHandler(new Handler() {
                        private final Formatter formatter = new Formatter() {
                            @Override
                            public String format(final LogRecord record) {
                                final String throwable;
                                if (record.getThrown() != null) {
                                    final StringWriter sw = new StringWriter();
                                    try (final PrintWriter pw = new PrintWriter(sw)) {
                                        pw.println();
                                        record.getThrown().printStackTrace(pw);
                                    }
                                    throwable = sw.toString();
                                } else {
                                    throwable = "";
                                }
                                return "[" + Thread.currentThread().getName() + "] " + record.getLevel() + ' ' + record.getSourceClassName() + " - " + record.getMessage() + throwable;
                            }
                        };

                        @Override
                        public void publish(final LogRecord record) {
                            System.out.println(formatter.format(record));
                        }

                        @Override
                        public void flush() {
                            System.out.flush();
                        }

                        @Override
                        public void close() throws SecurityException {
                            flush();
                        }
                    });
                }

                @Override
                public void addHandler(final Handler handler) throws SecurityException {
                    // no-op
                }
            };
        }
    }

}
