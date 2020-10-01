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
package org.apache.karaf.spring.boot.internal;

import org.apache.karaf.spring.boot.internal.shared.ApplicationContextCapturer;
import org.objectweb.asm.util.ASMifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class SpringApplicationContextCapture implements SpringApplicationRunListener {
    private final Object application;
    private final String[] args;
    private ConfigurableApplicationContext context;

    public SpringApplicationContextCapture(final SpringApplication application, final String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void started(final ConfigurableApplicationContext context) {
        this.context = context;
        ApplicationContextCapturer.set(this);
    }

    public static class Dumper {
        public static void main(String[] args) throws IOException {
            ASMifier.main(new String[]{
                    "target/test-classes/org/apache/karaf/spring/boot/internal/SpringApplicationContextCapture.class"});
        }
    }
}