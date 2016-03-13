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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.net.URL;

import io.tourniquet.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.tourniquet.junit.rules.RuleSetup;
import io.tourniquet.junit.rules.RuleSetup.RequirementLevel;
import io.tourniquet.junit.jcr.rules.util.XMLContentLoader;

/**
 * The ContentLoader is a testRule to prefill a {@link ContentRepository} with a node structure before the test.
 * <p>Experimental!</p>
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class ContentLoader extends ExternalResource<ContentRepository> {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ContentLoader.class);

    private final ContentRepository repository;
    private URL contentDef;
    private Node rootNode;

    public ContentLoader(final ContentRepository repository) {

        super(repository);
        this.repository = repository;
    }

    /**
     * Sets the locator pointing to the content definition.
     *
     * @param contentDef
     *         the url of the content definition.
     */
    @RuleSetup(RequirementLevel.OPTIONAL)
    public void setContentDefinition(final URL contentDef) {

        assertStateBefore(State.INITIALIZED);
        this.contentDef = contentDef;
    }

    @Override
    protected void before() throws Throwable {

        if (this.contentDef != null) {
            this.rootNode = loadContent(this.contentDef);
        }
    }

    @Override
    protected void after() {

        if (this.rootNode != null) {
            try {
                this.rootNode.refresh(false);
                this.rootNode.remove();
            } catch (RepositoryException e) {
                LOG.warn("Could not remove root node", e);
            }
        }
    }

    /**
     * Loads content from an external content definition into the underlying repository.
     *
     * @param contentDefinition
     *         URL pointing to the resource that defines the content to be loaded.
     *
     * @return The root node, defined by the content's document element, is returned.
     *
     * @throws RepositoryException
     */
    public Node loadContent(URL contentDefinition) throws RepositoryException {

        LOG.info("Loading Content");
        final Session session = repository.getAdminSession();
        final XMLContentLoader loader = new XMLContentLoader();
        return loader.loadContent(session, contentDefinition);
    }
}
