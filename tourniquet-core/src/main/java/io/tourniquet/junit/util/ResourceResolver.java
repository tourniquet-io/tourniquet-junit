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

import java.net.URL;

/**
 * A resolver to locate resources on the local classpath.
 * Created by Gerald Muecke on 24.11.2015.
 */
public class ResourceResolver {

    private final boolean failOnMissingResource;

    /**
     * Creates a new ResourceResolver that doesn't fail if the resource is missing.
     */
    public ResourceResolver() {
        this(false);
    }

    /**
     * Creates a new ResourceResolver that will fail with an {@link AssertionError} if the resource can not
     * be located.
     * @param failOnMissingResource
     */
    public ResourceResolver(boolean failOnMissingResource) {
        this.failOnMissingResource = failOnMissingResource;
    }

    /**
     * Resolves a URL of the resource specified. The resource path should be absolute, as there is no hint provided,
     * where to start the search. The resource is looked up using either the context classloader or the classloader
     * of the resolver itself.
     * @param resource
     *  the resource to be resolved
     * @return
     *  the URL of the resource or null if it wasnt found.
     * @throws java.lang.AssertionError
     *  If the resolver is configured to fail on missing resource
     */
    public URL resolve(String resource){
        return resolve(resource, getClass());
    }

    /**
     * Resolves a URL of the resource specified using the provided class as hint to start the search. For searching
     * the resource the consumer's classloader is used.
     * @param resource
     *  the path the resource. The path can be either absolute or relative to the consumers location.
     * @param consumer
     *  the consumer class of the resource. It's classloader is used for the lookup and it's location is used
     *  as reference point for resolving relative path names
     * @return
     */
    public URL resolve(String resource, Class consumer){

        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        final String resourcePath = resolvePath(resource, consumer);
        URL contentUrl = null;
        if (ccl != null) {
            contentUrl = ccl.getResource(resourcePath);
        }
        if (contentUrl == null) {
            contentUrl = consumer.getResource(resourcePath);
        }
        if(failOnMissingResource && contentUrl == null){
            throw new AssertionError("Resource " + resource + " not found");
        }
        return contentUrl;
    }

    /**
     * Resolves a resource relative to a consumer class. If the resource path starts with a '/', it will be used as
     * is, because it does not denote a relative path. If the resource path is a relative path, it will be resolved
     * relative to the package of the consumer class.
     * @param resource
     *  the path to the resource to resolve. It may be absolute or relative.
     * @param consumer
     *  the consumer that denotes the base to resolve a relative path.
     * @return
     *  an absolute path to the resource
     */
    private String resolvePath(final String resource, final Class consumer) {

        if(resource.startsWith("/")) {
            //absolute path
            return resource;
        }
        final StringBuilder buf = new StringBuilder(32);
        buf.append('/').append(consumer.getPackage().getName().replaceAll("\\.", "/"))
           .append('/').append(resource);
        return buf.toString();
    }
}
