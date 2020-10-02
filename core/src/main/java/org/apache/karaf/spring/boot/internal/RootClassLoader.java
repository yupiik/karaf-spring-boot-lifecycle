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

public class RootClassLoader extends ClassLoader {
    public RootClassLoader(final ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (name != null && name.startsWith("org.springframework.")) {
            throw new ClassNotFoundException(name);
        }
        try {
            return super.loadClass(name, resolve);
        } catch (final ClassNotFoundException cnfe) {
            return getSystemClassLoader().loadClass(name);
        }
    }
}
