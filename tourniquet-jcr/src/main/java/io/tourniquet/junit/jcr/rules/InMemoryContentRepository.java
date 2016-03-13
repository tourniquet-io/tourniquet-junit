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

package io.tourniquet.junit.jcr.rules;

import java.net.URL;

import io.tourniquet.junit.rules.RuleSetup;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.rules.TemporaryFolder;

/**
 * The {@link InMemoryContentRepository} rule is intended for self-sufficient unit tests. It is based on the
 * {@link TransientRepository} of Jackrabitt that is an in-memory repository. Nevertheless it requires a filesystem
 * location to put the configuration file (repository.xml) to.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class InMemoryContentRepository extends JackrabbitContentRepository{

    private static final String SIMPLE_SECURITY_INMEMORY_CONFIG = "inMemoryRepository.xml";

    private static final String SECURITY_ENABLED_INMEMORY_CONFIG = "securityEnabledInMemoryRepository.xml";

    private transient boolean securityEnabled;

    public InMemoryContentRepository(final TemporaryFolder workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public URL getConfigUrl() {

        String cfgResName;
        if(securityEnabled){
            cfgResName = SECURITY_ENABLED_INMEMORY_CONFIG;
        } else {
            cfgResName = SIMPLE_SECURITY_INMEMORY_CONFIG;
        }

        return getClass().getResource(cfgResName);
    }

    @Override
    public void setCndUrl(final URL cndUrl) {

        super.setCndUrl(cndUrl);
    }

    /**
     * Enables security for this rule. With enabled security, the repository supports user management and access
     * management. Without security enabled, no users or groups can be added. There are only the anonymous user
     * with universal read access and the admin user with universal write access. Which is sufficient for read-oriented
     * test-cases
     * @param securityEnabled
     *  <code>true</code> to enable security. <code>false</code> to disable security (default)
     */
    @RuleSetup
    public void setSecurityEnabled(final boolean securityEnabled) {
        this.securityEnabled = securityEnabled;

    }
}
