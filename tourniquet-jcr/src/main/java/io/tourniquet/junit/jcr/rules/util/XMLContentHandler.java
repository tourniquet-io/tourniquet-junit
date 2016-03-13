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
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import io.tourniquet.schemas.jcr_import.ObjectFactory;
import io.tourniquet.schemas.jcr_import.PropertyDescriptor;
import io.tourniquet.schemas.jcr_import.PropertyValueType;

/**
 * Implementation of the {@link DefaultHandler} that creates {@link Node} in a JCR {@link Repository} that are defined
 * in an xml file.
 *
 */
public class XMLContentHandler extends DefaultHandler {

    /**
     * Namespace the content handler uses to identify the correct elements.
     */
    public static final String NS_TQT_IMPORT = "http://tourniquet.io/schemas/jcr-import";

    private static final Logger LOG = LoggerFactory.getLogger(XMLContentHandler.class);
    /**
     * Object Factory for creating temporary model object.
     */
    private static final ObjectFactory FACTORY = new ObjectFactory();

    /**
     * Map of {@link PropertyValueType} to the int values of the {@link PropertyType}.
     */
    private static final Map<PropertyValueType, Integer> JCR_PROPERTIES;

    static {
        final Map<PropertyValueType, Integer> properties = new HashMap<>(); //NOSONAR
        //@formatter:off
        properties.put(PropertyValueType.BINARY,        PropertyType.BINARY);
        properties.put(PropertyValueType.DATE,          PropertyType.DATE);
        properties.put(PropertyValueType.DECIMAL,       PropertyType.DECIMAL);
        properties.put(PropertyValueType.DOUBLE,        PropertyType.DOUBLE);
        properties.put(PropertyValueType.LONG,          PropertyType.LONG);
        properties.put(PropertyValueType.NAME,          PropertyType.NAME);
        properties.put(PropertyValueType.PATH,          PropertyType.PATH);
        properties.put(PropertyValueType.REFERENCE,     PropertyType.REFERENCE);
        properties.put(PropertyValueType.STRING,        PropertyType.STRING);
        properties.put(PropertyValueType.UNDEFINED,     PropertyType.UNDEFINED);
        properties.put(PropertyValueType.URI,           PropertyType.URI);
        properties.put(PropertyValueType.WEAKREFERENCE, PropertyType.WEAKREFERENCE);
        // @formatter:on
        JCR_PROPERTIES = Collections.unmodifiableMap(properties);
    }

    /**
     * The session used for import operations.
     */
    private final Session session;
    /**
     * A stack of the created nodes.
     */
    private final Deque<Node> nodeStack;
    // TODO verify if textstack could be replaced by lastText
    /**
     * A stack of the created text elements.
     */
    private final Deque<String> textStack;
    /**
     * A stack fo the created property descriptors.
     */
    private final Deque<PropertyDescriptor> propertyStack;
    /**
     * The start time in ns.
     */
    // TODO verify if propertyStack could be replaced by lastProperty
    private long startTime;
    /**
     * The root node of the content tree that is created by this handler.
     */
    private Node rootNode;


    /**
     * Creates a new content handler using the specified session for performing the input.
     *
     * @param session
     *         the JCR session bound to a user with sufficient privileges to perform the content loader operation.
     */
    public XMLContentHandler(final Session session) {

        this.session = session;
        this.nodeStack = new ArrayDeque<>();
        this.textStack = new ArrayDeque<>();
        this.propertyStack = new ArrayDeque<>();
    }

    /**
     * Prints out information statements and sets the startTimer.
     */
    @Override
    public void startDocument() throws SAXException {

        LOG.info("BEGIN ContentImport");
        LOG.info("IMPORT USER: {}", this.session.getUserID());
        this.startTime = System.nanoTime();
    }

    /**
     * Persists the changes in the repository and prints out information such as processing time.
     */
    @Override
    public void endDocument() throws SAXException {

        LOG.info("Content Processing finished, saving...");
        try {
            this.session.save();
        } catch (final RepositoryException e) {
            throw new AssertionError("Saving failed", e);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Content imported in {} ms", (System.nanoTime() - this.startTime) / 1_000_000);
            LOG.info("END ContentImport");
        }
    }

    /**
     * Depending on the element, which has to be in the correct namespace, the method either creates a new node, adds a
     * mixin type or creates a property (properties are not yet written to the node).
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {

        LOG.trace("startElement uri={} localName={} qName={} attributes={}", uri, localName, qName, attributes);

        if (this.isNotTourniquetNamespace(uri)) {
            return;
        }
        switch (localName) {
            case "rootNode":
                this.startElementRootNode(attributes);
                break;
            case "node":
                this.startElementNode(attributes);
                break;
            case "mixin":
                this.startElementMixin(attributes);
                break;
            case "property":
                this.startElementProperty(attributes);
                break;
            default:
                break;
        }
    }

    /**
     * Depending on the element, which has to be in the correct namespace, the method adds a property to the node or
     * removes completed nodes from the node stack.
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {

        LOG.trace("endElement uri={} localName={} qName={}", uri, localName, qName);
        if (this.isNotTourniquetNamespace(uri)) {
            return;
        }
        switch (localName) {
            case "rootNode":
                LOG.debug("Closing rootNode");
                this.nodeStack.pop();
                break;
            case "node":
                LOG.debug("Closing node");
                this.nodeStack.pop();
                break;
            case "mixin":
                LOG.debug("Closing mixin");
                break;
            case "property":
                this.endElementProperty();
                break;
            default:
                break;
        }
    }

    private void endElementProperty() {

        LOG.debug("Closing property");
        final PropertyDescriptor propDesc = this.propertyStack.pop();
        try {
            propDesc.setValue(this.parseValue(propDesc.getJcrType(), this.textStack.pop()));
            this.addProperty(this.nodeStack.peek(), propDesc);
        } catch (final RepositoryException e) {
            throw new AssertionError("Could set property value", e);
        }
    }

    private Object parseValue(final PropertyValueType valueType, final String valueAsText) throws RepositoryException {
        // TODO handle ref property
        LOG.debug("Parsing type={} from='{}'", valueType, valueAsText);
        final ValueFactory valFactory = this.session.getValueFactory();
        Value value;
        switch (valueType) {
            case BINARY:
                value = valFactory.createValue(valFactory.createBinary(new ByteArrayInputStream(Base64.decodeBase64(
                        valueAsText.getBytes(StandardCharsets.UTF_8)))));
                break;
            case REFERENCE:
                // TODO resolve IDs
                value = null;
                break;
            case WEAKREFERENCE:
                // TODO resolve IDs
                value = null;
                break;
            default:
                value = valFactory.createValue(valueAsText, this.getPropertyType(valueType));
        }

        return value;
    }

    /**
     * Adds a property to the node. The property's name, type and value is defined in the {@link PropertyDescriptor}.
     *
     * @param node
     *         the node to which the property should be added
     * @param propDesc
     *         the {@link PropertyDescriptor} containing the details of the property
     *
     * @throws RepositoryException
     *         if the path of the node can not be determined or the property value can not be set
     */
    private void addProperty(final Node node, final PropertyDescriptor propDesc) throws RepositoryException {

        LOG.info("Node {} adding property {}", node.getPath(), propDesc.getName());
        node.setProperty(propDesc.getName(), (Value) propDesc.getValue());
    }

    /**
     * Converts the valueType to an int representing the {@link PropertyType} of the property.
     *
     * @param valueType
     *         the value type to be converted
     *
     * @return the int value of the corresponding {@link PropertyType}
     */
    private int getPropertyType(final PropertyValueType valueType) {

        return JCR_PROPERTIES.get(valueType);
    }

    /**
     * Detects text by trimming the effective content of the char array.
     */
    @Override
    public void characters(final char[] chr, final int start, final int length) throws SAXException {

        final String text = new String(chr).substring(start, start + length);
        LOG.trace("characters; '{}'", text);
        final String trimmedText = text.trim();
        LOG.info("text: '{}'", trimmedText);
        this.textStack.push(trimmedText);
    }

    /**
     * Converts the {@link SAXParseException} into an {@link AssertionError} to force the test to fail.
     * @param parseException
     *  the exception that occured during parsing
     * @throws SAXException
     *  is not thrown.
     */
    @Override
    public void error(final SAXParseException parseException) throws SAXException {

        throw new AssertionError("parse error", parseException);
    }

    /**
     * Checks if the specified uri is not of the namespace this {@link XMLContentHandler} is able to process.
     *
     * @param uri
     *         the uri to check
     *
     * @return <code>false</code> if the namespace is processable by this {@link XMLContentHandler}
     */
    private boolean isNotTourniquetNamespace(final String uri) {

        return !NS_TQT_IMPORT.equals(uri);
    }

    /**
     * Invoked on rootNode element.
     *
     * @param attributes
     *         the DOM attributes of the root node element
     *
     */
    private void startElementRootNode(final Attributes attributes) {

        LOG.debug("Found rootNode");
        try {
            this.rootNode = this.newNode(null, attributes);
            this.nodeStack.push(this.rootNode);
        } catch (final RepositoryException e) {
            throw new AssertionError("Could not create node", e);
        }
    }

    /**
     * Invoked on node element.
     *
     * @param attributes
     *         the DOM attributes of the node element
     *
     * @throws SAXException
     *  if the node for the new element can not be added
     */
    private void startElementNode(final Attributes attributes) {

        LOG.debug("Found node");
        try {
            this.nodeStack.push(this.newNode(this.nodeStack.peek(), attributes));
        } catch (final RepositoryException e) {
            throw new AssertionError("Could not create node", e);
        }
    }

    /**
     * Invoked on mixin element.
     *
     * @param attributes
     *         the DOM attributes of the mixin element
     *
     * @throws SAXException
     *  if the mixin type can not be added
     */
    private void startElementMixin(final Attributes attributes) {

        LOG.debug("Found mixin declaration");
        try {
            this.addMixin(this.nodeStack.peek(), attributes);
        } catch (final RepositoryException e) {
            throw new AssertionError("Could not add mixin type", e);
        }
    }

    /**
     * Invoked on property element.
     *
     * @param attributes
     *         the DOM attributes of the property element
     */
    private void startElementProperty(final Attributes attributes) {

        LOG.debug("Found property");
        this.propertyStack.push(this.newPropertyDescriptor(attributes));
    }

    /**
     * Creates the {@link Node} in the repository from the given attributes.
     *
     * @param parent
     *  the parent node to which a new child node should be added
     * @param attributes
     *         the attributes containing the basic information required to create the node
     *
     * @return the newly creates {@link Node}
     *
     * @throws RepositoryException
     *  if the new node can not be created
     */
    private Node newNode(final Node parent, final Attributes attributes) throws RepositoryException {

        Node parentNode;
        if (parent == null) {
            parentNode = this.session.getRootNode();
        } else {
            parentNode = parent;
        }
        // TODO handle path parameters

        final String name = attributes.getValue("name");
        final String primaryType = attributes.getValue("primaryType");

        LOG.info("Node {} adding child node {}(type={})", parentNode.getPath(), name, primaryType);
        return parentNode.addNode(name, primaryType);
    }

    private void addMixin(final Node node, final Attributes attributes) throws RepositoryException {

        final String mixinType = attributes.getValue("name");
        LOG.info("Node {} adding mixin {}", node.getPath(), mixinType);
        node.addMixin(mixinType);
    }

    /**
     * Creates a new {@link PropertyDescriptor} from the attributes.
     *
     * @param attributes
     *         the attributes defining the name and jcrType of the property
     *
     * @return a {@link PropertyDescriptor} instance
     */
    private PropertyDescriptor newPropertyDescriptor(final Attributes attributes) {

        final PropertyDescriptor propDesc = FACTORY.createPropertyDescriptor();
        LOG.debug("property name={}", attributes.getValue("name"));
        LOG.debug("property jcrType={}", attributes.getValue("jcrType"));
        propDesc.setName(attributes.getValue("name"));
        propDesc.setJcrType(PropertyValueType.fromValue(attributes.getValue("jcrType")));
        return propDesc;
    }

    /**
     * Accessor for the root {@link Node} created by this handler for the root element.
     * @return
     *  the Node representing the root node of the content created by this handler.
     */
    public Node getRootNode() {

        return rootNode;
    }
}
