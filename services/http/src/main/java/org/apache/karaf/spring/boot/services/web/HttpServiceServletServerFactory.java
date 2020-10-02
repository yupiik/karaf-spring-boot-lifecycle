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
package org.apache.karaf.spring.boot.services.web;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletSecurityElement;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class HttpServiceServletServerFactory implements ServletWebServerFactory {
    private final BundleContext context;

    private HttpServiceServletServerFactory(final BundleContext context) {
        this.context = context;
    }

    @Override
    public WebServer getWebServer(final ServletContextInitializer... initializers) {
        return new HttpServiceWebServer(initializers, context);
    }

    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnClass({Servlet.class, HttpService.class, ServletRequest.class})
    @ConditionalOnProperty(name = "karaf.spring-boot.use-http-service", havingValue = "true", matchIfMissing = true)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(value = ServletWebServerFactory.class, search = SearchStrategy.CURRENT)
    @Configuration(proxyBeanMethods = false)
    public static class SpringConfiguration {
        private final ClassLoader loader;

        public SpringConfiguration() {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            while (!"KarafLauncherLoader".equals(loader.getClass().getSimpleName())) {
                loader = loader.getParent();
            }
            this.loader = requireNonNull(loader, "Didn't find KarafLauncherLoader");
        }

        @Bean
        public HttpServiceServletServerFactory httpServiceServletWebServerFactory() {
            try {
                final Method getBundleContext = loader.getClass().getDeclaredMethod("getContext");
                if (!getBundleContext.isAccessible()) {
                    getBundleContext.setAccessible(true);
                }
                final BundleContext ctx = BundleContext.class.cast(getBundleContext.invoke(loader));
                return new HttpServiceServletServerFactory(ctx);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static class HttpServiceWebServer implements WebServer {
        private final BundleContext context;
        private HttpServiceServletContext registrar;

        private HttpServiceWebServer(final ServletContextInitializer[] initializers,
                                     final BundleContext context) {
            this.context = context;
            init(initializers);
        }

        private void init(final ServletContextInitializer... initializers) {
            // todo: create a servletcontexthelper to have a dedicated context? make it configurable? + make it active at start() call
            registrar = new HttpServiceServletContext(context);
            Stream.of(initializers).forEach(it -> {
                try {
                    it.onStartup(registrar);
                } catch (final ServletException e) {
                    throw new IllegalStateException(e);
                }
            });
            // do the actual registrations
            registrar.servletRegistrations.forEach(it -> it.callback.accept(it.config));
        }

        @Override
        public void start() throws WebServerException {
            // it is started by another bundle
        }

        @Override
        public void stop() throws WebServerException {
            if (registrar == null) {
                return;
            }
            registrar.osgiRegistrations.forEach(it -> {
                try {
                    it.unregister();
                } catch (final IllegalStateException ise) {
                    // no-op
                }
            });
        }

        @Override
        public int getPort() {
            // todo: make it conditional to bundle startup
            final ServiceTracker<HttpService, HttpService> serviceTracker = new ServiceTracker<>(context, HttpService.class, null);
            serviceTracker.open();
            final ServiceReference<HttpService> serviceReference = serviceTracker.getServiceReference();
            serviceTracker.close();
            return ofNullable(serviceReference)
                    .map(it -> it.getProperty("org.osgi.service.http.port"))
                    .map(String::valueOf)
                    .map(Integer::parseInt)
                    .orElse(80);
        }
    }

    private static class HttpServiceServletContext implements ServletContext { // todo: complete impl
        private final BundleContext context;
        private final Collection<DynamicRegistration> servletRegistrations = new ArrayList<>();
        private final Collection<ServiceRegistration<?>> osgiRegistrations = new ArrayList<>();

        private HttpServiceServletContext(final BundleContext ctx) {
            this.context = ctx;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(final String s, final Servlet servlet) {
            final DynamicRegistration registration = new DynamicRegistration(props -> osgiRegistrations.add(context.registerService(Servlet.class, servlet, props)));
            final String clazz = servlet.getClass().getName();
            registration.config.put("karaf.servlet.class", clazz);
            registration.config.put("osgi.http.whiteboard.servlet.name", clazz); // default
            registration.config.put(Constants.SERVICE_RANKING, 0);
            servletRegistrations.add(registration);
            return registration;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(final String s, final String s1) {
            try {
                return addServlet(s, Thread.currentThread().getContextClassLoader().loadClass(s1).asSubclass(Servlet.class));
            } catch (final ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public ServletRegistration.Dynamic addServlet(final String s, final Class<? extends Servlet> aClass) {
            try {
                return addServlet(s, createServlet(aClass));
            } catch (final ServletException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public <T extends Servlet> T createServlet(final Class<T> aClass) throws ServletException {
            return newInstance(aClass);
        }

        @Override
        public ServletRegistration getServletRegistration(final String s) {
            return servletRegistrations.stream().filter(it -> s.equals(it.config.get("osgi.http.whiteboard.servlet.name"))).findFirst().orElse(null);
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return emptyMap();
        }

        @Override
        public FilterRegistration.Dynamic addFilter(final String s, final String s1) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(final String s, final Filter filter) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(final String s, final Class<? extends Filter> aClass) {
            return null;
        }

        @Override
        public <T extends Filter> T createFilter(final Class<T> aClass) throws ServletException {
            return null;
        }

        @Override
        public FilterRegistration getFilterRegistration(final String s) {
            return null;
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return emptyMap();
        }

        @Override
        public String getContextPath() {
            return "";
        }

        @Override
        public ServletContext getContext(final String s) {
            return this;
        }

        @Override
        public int getMajorVersion() {
            return 3;
        }

        @Override
        public int getMinorVersion() {
            return 1;
        }

        @Override
        public int getEffectiveMajorVersion() {
            return 3;
        }

        @Override
        public int getEffectiveMinorVersion() {
            return 1;
        }

        @Override
        public String getMimeType(final String s) {
            return null;
        }

        @Override
        public Set<String> getResourcePaths(final String s) {
            return null;
        }

        @Override
        public URL getResource(final String s) throws MalformedURLException {
            return null;
        }

        @Override
        public InputStream getResourceAsStream(final String s) {
            return null;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(final String s) {
            return null;
        }

        @Override
        public RequestDispatcher getNamedDispatcher(final String s) {
            return null;
        }

        @Override
        public Servlet getServlet(final String s) throws ServletException {
            return null;
        }

        @Override
        public Enumeration<Servlet> getServlets() {
            return emptyEnumeration();
        }

        @Override
        public Enumeration<String> getServletNames() {
            return emptyEnumeration();
        }

        @Override
        public void log(final String s) {

        }

        @Override
        public void log(final Exception e, final String s) {

        }

        @Override
        public void log(final String s, final Throwable throwable) {

        }

        @Override
        public String getRealPath(final String s) {
            return s;
        }

        @Override
        public String getServerInfo() {
            return "HttpService/1.0";
        }

        @Override
        public String getInitParameter(final String s) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return emptyEnumeration();
        }

        @Override
        public boolean setInitParameter(final String s, final String s1) {
            return false;
        }

        @Override
        public Object getAttribute(final String s) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return emptyEnumeration();
        }

        @Override
        public void setAttribute(final String s, final Object o) {

        }

        @Override
        public void removeAttribute(final String s) {

        }

        @Override
        public String getServletContextName() {
            return "";
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            return null;
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> set) {

        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return null;
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return null;
        }

        @Override
        public void addListener(final String s) {

        }

        @Override
        public <T extends EventListener> void addListener(final T t) {

        }

        @Override
        public void addListener(final Class<? extends EventListener> aClass) {

        }

        @Override
        public <T extends EventListener> T createListener(final Class<T> aClass) throws ServletException {
            return newInstance(aClass);
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public void declareRoles(final String... strings) {

        }

        @Override
        public String getVirtualServerName() {
            return "localhost"; // todo: read props of httpservice
        }

        private <T> T newInstance(final Class<T> aClass) throws ServletException {
            try {
                return aClass.getConstructor().newInstance();
            } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new ServletException(e);
            } catch (final InvocationTargetException e) {
                throw new ServletException(e.getTargetException());
            }
        }
    }

    // todo: populate config with http whiteboard setup
    private static class DynamicRegistration implements ServletRegistration.Dynamic {
        private final Hashtable<String, Object> config = new Hashtable<>();
        private final Consumer<Dictionary<String, Object>> callback;

        private DynamicRegistration(final Consumer<Dictionary<String, Object>> callback) {
            this.callback = callback;
        }

        @Override
        public void setLoadOnStartup(final int i) {
            // no-op
        }

        @Override
        public void setMultipartConfig(final MultipartConfigElement multipartConfigElement) {
            config.put("osgi.http.whiteboard.servlet.multipart.enabled", true);
            config.put("osgi.http.whiteboard.servlet.multipart.fileSizeThreshold", multipartConfigElement.getFileSizeThreshold());
            config.put("osgi.http.whiteboard.servlet.multipart.location", multipartConfigElement.getLocation());
            config.put("osgi.http.whiteboard.servlet.multipart.maxFileSize", multipartConfigElement.getMaxFileSize());
            config.put("osgi.http.whiteboard.servlet.multipart.maxRequestSize", multipartConfigElement.getMaxRequestSize());
        }

        @Override
        public void setRunAsRole(final String s) {
            // no-op
        }

        @Override
        public Set<String> setServletSecurity(final ServletSecurityElement servletSecurityElement) {
            return emptySet();
        }

        @Override
        public void setAsyncSupported(final boolean b) {
            config.put("osgi.http.whiteboard.servlet.asyncSupported", b);
        }

        @Override
        public Set<String> addMapping(final String... strings) {
            final Object patterns = config.get("osgi.http.whiteboard.servlet.pattern");
            if (patterns == null) {
                config.put("osgi.http.whiteboard.servlet.pattern", strings);
            } else {
                config.put("osgi.http.whiteboard.servlet.pattern", Stream.concat(
                        Stream.of(String[].class.cast(patterns)),
                        Stream.of(strings))
                        .toArray(String[]::new));
            }
            return Stream.of(strings).collect(toSet());
        }

        @Override
        public Collection<String> getMappings() {
            final Object patterns = config.get("osgi.http.whiteboard.servlet.pattern");
            if (patterns == null) {
                return emptySet();
            }
            return asList(String[].class.cast(patterns));
        }

        @Override
        public String getRunAsRole() {
            return null;
        }

        @Override
        public String getName() {
            return String.valueOf(config.get("osgi.http.whiteboard.servlet.name"));
        }

        @Override
        public String getClassName() {
            return String.valueOf(config.get("karaf.servlet.class"));
        }

        @Override
        public boolean setInitParameter(final String s, final String s1) {
            config.put("servlet.init." + s, s1);
            return true;
        }

        @Override
        public String getInitParameter(final String s) {
            return ofNullable(config.get("servlet.init." + s)).map(String::valueOf).orElse(null);
        }

        @Override
        public Set<String> setInitParameters(final Map<String, String> map) {
            map.forEach(this::setInitParameter);
            return map.keySet();
        }

        @Override
        public Map<String, String> getInitParameters() {
            return config.keySet().stream()
                    .filter(it -> it.startsWith("servlet.init."))
                    .collect(toMap(it -> it.substring("servlet.init.".length()), key -> String.valueOf(config.get(key))));
        }
    }
}
