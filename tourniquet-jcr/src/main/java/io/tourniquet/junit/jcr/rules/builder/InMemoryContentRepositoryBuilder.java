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

import io.tourniquet.junit.jcr.rules.InMemoryContentRepository;
import org.junit.rules.TemporaryFolder;

/**
 * A Builder for an {@link io.tourniquet.junit.jcr.rules.InMemoryContentRepository}. The {@link io.tourniquet.junit.jcr.rules.InMemoryContentRepository} requires a
 * {@link TemporaryFolder} as outer rule. The rule will create a repository with pure in-memory persistence that is
 * fast to instantiate and tear down and the content will be lost on each restart. The builder allows to activate
 * security in the rule, so that user management and access control can be properly used.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class InMemoryContentRepositoryBuilder extends ContentRepositoryBuilder<InMemoryContentRepository> {

    private transient final TemporaryFolder temporaryFolder;

    private transient boolean securityEnabled;

    public InMemoryContentRepositoryBuilder(final TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder;
    }

    @Override
    public InMemoryContentRepository build() {

        InMemoryContentRepository repository = new InMemoryContentRepository(temporaryFolder);
        repository.setCndUrl(getCndModelResource());
        repository.setSecurityEnabled(this.securityEnabled);
        return repository;
    }

    public InMemoryContentRepositoryBuilder withSecurityEnabled() {
        this.securityEnabled = true;
        return this;


    }
}
