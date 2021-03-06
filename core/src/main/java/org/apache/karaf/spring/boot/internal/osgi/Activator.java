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
package org.apache.karaf.spring.boot.internal.osgi;

import org.apache.karaf.spring.boot.FatJarUrlHandler;
import org.apache.karaf.spring.boot.SpringBootService;
import org.apache.karaf.spring.boot.internal.SpringBootServiceImpl;
import org.apache.karaf.util.tracker.BaseActivator;
import org.apache.karaf.util.tracker.annotation.ProvideService;
import org.apache.karaf.util.tracker.annotation.Services;
import org.osgi.service.url.URLStreamHandlerService;

import java.util.Hashtable;

@Services(
        provides = @ProvideService(URLStreamHandlerService.class)
)
public class Activator extends BaseActivator {
    private SpringBootServiceImpl springBootService;

    @Override
    protected void doStart() {
        final FatJarUrlHandler fatJarUrlHandler = new FatJarUrlHandler();
        final Hashtable<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("url.handler.protocol", "spring-boot");
        register(URLStreamHandlerService.class, fatJarUrlHandler, serviceProperties);
        springBootService = new SpringBootServiceImpl(bundleContext);
        register(SpringBootService.class, springBootService);
    }

    @Override
    protected void doStop() {
        try {
            springBootService.stopAll();
        } finally {
            super.doStop();
        }
    }
}