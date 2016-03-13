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

/**
 *
 */
package io.tourniquet.junit.jcr.rules;

import java.net.URL;

import org.junit.rules.TemporaryFolder;

/**
 * Repository that supports a fully functional repository including persistence. The default configuration used by the
 * repository is a pure in memory persistence. If actual persistence is required, an according configuration has to be
 * configured.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class StandaloneContentRepository extends JackrabbitContentRepository {

    public StandaloneContentRepository(final TemporaryFolder workingDirectory) {

        super(workingDirectory);
        // set the default configuration
        super.setConfigUrl(getClass().getResource("inMemoryRepository.xml"));
    }

    /**
     * Sets the URL pointing to the configuration to use. The configuration has to be a valid Jackrabbit configuration.
     * <p/>
     * {@see http://jackrabbit.apache.org/jackrabbit-configuration.html}
     */
    @Override
    public void setConfigUrl(final URL configUrl) {

        super.setConfigUrl(configUrl);
    }

    /**
     * Sets the URL pointing to the node type definition to be loaded upon initialization.
     *
     * @param cndUrl
     *         resource locator for the CND node type definitions, {@see http://jackrabbit.apache
     *         .org/rules/node-type-notation.html}
     */
    @Override
    public void setCndUrl(final URL cndUrl) {

        super.setCndUrl(cndUrl);
    }


}
