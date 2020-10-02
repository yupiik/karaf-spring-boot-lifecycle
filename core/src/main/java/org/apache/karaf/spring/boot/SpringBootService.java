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

import java.net.URI;
import java.util.Map;

public interface SpringBootService {

    String install(URI uri, String stack) throws Exception;

    String install(String name, URI uri, String stack) throws Exception;
    // void install(URI uri, String[] stacks) throws Exception;

    void start(String name, String[] args) throws Exception;

    void stop(String name) throws Exception;

    void stopAll();

    Map<String, Boolean> list() throws Exception;

    // String[] listStacks() throws Exception;

    // void addStack(URI uri) throws Exception;

    // void removeStack(String name) throws Exception;

}
