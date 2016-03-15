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

package io.tourniquet.junit.http.rules;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * A {@link ResourceHttpHandler} that serves the content of a URL. It's intended for serving classpath resources
 * referenced by URL but it may also serves resource from the local filesystem or from a network resource.
 */
public class UrlResourceHandler extends ResourceHttpHandler{

    private final URL resource;

    public UrlResourceHandler(URL resource ){
        this.resource = resource;
    }

    @Override
    protected void writeResource(final OutputStream outputStream) throws IOException {
            try(InputStream inputStream = resource.openStream()){
                IOUtils.copy(inputStream, outputStream);
            }
    }
}
