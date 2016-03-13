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

package io.tourniquet.junit.jcr.rules.util;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.net.URL;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Loads content from an external XML source into the JCR Repository.
 *
 */
public class XMLContentLoader {

    /**
     * Flag to indicate the xml processing should resolve namespaces.
     */
    private boolean namespaceAware = true;

    /**
     * The schema to validate the input against.
     */
    private Schema schema;

    /**
     * Loads the content from the specified contentDefinition into the JCRRepository, using the specified session.
     *  @param session
     *         the session used to import the date. The user bound to the session must have the required privileges to
     *         perform the import operation.
     * @param contentDef
     */
    public Node loadContent(final Session session, final URL contentDef) {

        final SAXParserFactory factory = this.getSAXParserFactory();
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            final SAXParser parser = factory.newSAXParser();
            final InputSource source = new InputSource(contentDef.openStream());
            final XMLContentHandler handler = new XMLContentHandler(session);
            parser.parse(source, handler);
            return handler.getRootNode();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError("Loading Content to JCR Repository failed", e);
        }
    }

    /**
     * Creates a new {@link SAXParserFactory} using the configured parameters.
     *
     * @return an instance of a SAXParserFactory to create new SAX parsers. The factory is configured according to the
     * settings for input schema validation and namespace awareness.
     */
    private SAXParserFactory getSAXParserFactory() {

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(this.namespaceAware);
        factory.setSchema(this.schema);
        return factory;
    }

    /**
     * Configures the loader to be aware of namespaces.
     *
     * @param namespaceAware
     *         <code>true</code> if namespaces should be recognized. Default is <code>true</code>
     */
    public void setNamespaceAware(final boolean namespaceAware) {

        this.namespaceAware = namespaceAware;
    }

    // TODO link to published schema

    /**
     * Sets a specific schema to validate the input against. Default is the tourniquetLoader schema.
     *
     * @param schema
     *         schema to validate the input against
     */
    public void setSchema(final Schema schema) {

        this.schema = schema;
    }

}
