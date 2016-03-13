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
 * Abstract Builder for {@link io.tourniquet.junit.jcr.rules.ContentRepository} rules.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 * @param <T>
 *            the Type of the {@link io.tourniquet.junit.jcr.rules.ContentRepository}
 */
public abstract class ContentRepositoryBuilder<T extends ContentRepository> implements Builder<T> {

    private transient URL cndModelResource;

    /**
     * Creates a {@link JCRSessionBuilder} for building a {@link io.tourniquet.junit.jcr.rules.ActiveSession} with the {@link ContentRepository} as
     * outer rule.
     *
     * @return
     */
    public JCRSessionBuilder aroundSession() {
        return new JCRSessionBuilder(build());
    }

    /**
     * Creates a {@link ContentLoaderBuilder} for building a {@link ContentLoader} with the {@link ContentRepository} as
     * outer rule.
     *
     * @return
     */
    public ContentLoaderBuilder aroundPreparedContent() {
        return new ContentLoaderBuilder(build());
    }

    /**
     * Specifies the content repository should be initialized with node types specified in <a href="http://jackrabbit
     * .apache.org/rules/node-type-notation.html">CND format</a> in the resource specified.
     * @param cndModel
     *  a URL pointing to the CND file.
     * @return
     *  this builder
     */
    public ContentRepositoryBuilder<T> withNodeTypes(URL cndModel){
        this.cndModelResource = cndModel;
        return this;
    }

    /**
     * The URL of the cnd file for initializing the node types of the repository
     * @return
     */
    protected URL getCndModelResource() {
        return cndModelResource;
    }


}
