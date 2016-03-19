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

package io.tourniquet.junit.jcr.rules.builder;

import java.net.URL;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.jcr.rules.ContentLoader;
import io.tourniquet.junit.jcr.rules.ContentRepository;

/**
 * Builder for creating {@link io.tourniquet.junit.jcr.rules.ContentLoader} TestRules. A content loader can be used to
 * prefill a JCR Content Repository.
 *
 */
public class ContentLoaderBuilder implements Builder<ContentLoader> {

    private final ContentLoader contentLoader;

    public ContentLoaderBuilder(final ContentRepository repository) {
        contentLoader = new ContentLoader(repository);
    }

    /**
     * Uses the content descriptor specified by the URL.
     *
     * @param contentDescriptorUrl
     *            the URL referencing a content descriptor file
     * @return this builder
     */
    public ContentLoaderBuilder fromUrl(final URL contentDescriptorUrl) {

        contentLoader.setContentDefinition(contentDescriptorUrl);
        return this;
    }

    @Override
    public ContentLoader build() {
        return contentLoader;
    }

}
