/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
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

package io.tourniquet.junit.util;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 */
public class ResourceClassLoader extends URLClassLoader {

    public ResourceClassLoader() {

        super(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs());
    }

    public Class<?> loadClassFromResource(final String classname, String resourceName) throws Exception {

        URL resource = new ResourceResolver(true).resolve(resourceName);
        try (InputStream is = resource.openStream()) {
            int size = is.available();
            byte[] classData = new byte[size];
            is.read(classData);
            Class<?> loadedClass = defineClass(classname, classData, 0, size);
            resolveClass(loadedClass);
            return loadedClass;
        }
    }
}
