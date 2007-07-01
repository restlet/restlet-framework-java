/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathConstants;

import org.restlet.data.MediaType;
import org.restlet.util.NodeSet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Representation based on an XML document. It knows how to evaluate XPath
 * expressions and how to manage a namespace context.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class XmlRepresentation extends OutputRepresentation implements
        NamespaceContext {
    /** Internal map of namespaces. */
    private Map<String, String> namespaces;

    /** Indicates if processing is namespace aware. */
    private boolean namespaceAware;

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's mediaType.
     */
    public XmlRepresentation(MediaType mediaType) {
        super(mediaType);
        this.namespaces = null;
        this.namespaceAware = false;
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *                The representation's mediaType.
     * @param expectedSize
     *                The expected input stream size.
     */
    public XmlRepresentation(MediaType mediaType, long expectedSize) {
        super(mediaType, expectedSize);
        this.namespaces = null;
        this.namespaceAware = false;
    }

    /**
     * Evaluates an XPath expression and returns the result as in the given
     * return type.
     * 
     * @param returnType
     *                The qualified name of the return type.
     * @return The evaluation result.
     * @see javax.xml.xpath.XPathException
     * @see javax.xml.xpath.XPathConstants
     */
    public abstract Object evaluate(String expression, QName returnType)
            throws Exception;

    /**
     * Evaluates an XPath expression as a boolean. If the evaluation fails, null
     * will be returned.
     * 
     * @return The evaluation result.
     */
    public Boolean getBoolean(String expression) {
        return (Boolean) internalEval(expression, XPathConstants.BOOLEAN);
    }

    /**
     * Returns the map of namespaces.
     * 
     * @return The map of namespaces.
     */
    private Map<String, String> getNamespaces() {
        if (this.namespaces == null)
            this.namespaces = new HashMap<String, String>();
        return this.namespaces;
    }

    /**
     * {@inheritDoc javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String}
     */
    public String getNamespaceURI(String prefix) {
        return this.namespaces.get(prefix);
    }

    /**
     * Evaluates an XPath expression as a DOM Node. If the evaluation fails,
     * null will be returned.
     * 
     * @return The evaluation result.
     */
    public Node getNode(String expression) {
        return (Node) internalEval(expression, XPathConstants.NODE);
    }

    /**
     * Evaluates an XPath expression as a DOM NodeList. If the evaluation fails,
     * null will be returned.
     * 
     * @return The evaluation result.
     */
    public NodeSet getNodes(String expression) {
        NodeList nodes = (NodeList) internalEval(expression,
                XPathConstants.NODESET);
        return (nodes == null) ? null : new NodeSet(nodes);
    }

    /**
     * Evaluates an XPath expression as a number. If the evaluation fails, null
     * will be returned.
     * 
     * @return The evaluation result.
     */
    public Double getNumber(String expression) {
        return (Double) internalEval(expression, XPathConstants.NUMBER);
    }

    /**
     * {@inheritDoc javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String}
     */
    public String getPrefix(String namespaceURI) {
        String result = null;

        for (Entry<String, String> entry : getNamespaces().entrySet()) {
            if ((result == null) && entry.getValue().equals(namespaceURI))
                result = entry.getKey();
        }

        return result;
    }

    /**
     * {@inheritDoc javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String}
     */
    public Iterator<String> getPrefixes(String namespaceURI) {
        List<String> result = new ArrayList<String>();

        for (Entry<String, String> entry : getNamespaces().entrySet()) {
            if (entry.getValue().equals(namespaceURI))
                result.add(entry.getKey());
        }

        return Collections.unmodifiableList(result).iterator();
    }

    /**
     * Returns an XML source or transformation instructions.
     * 
     * @return An XML source or transformation instructions.
     * @throws IOException
     */
    public Source getSource() throws IOException {
        return new StreamSource(getStream());
    }

    /**
     * Evaluates an XPath expression as a string.
     * 
     * @return The evaluation result.
     */
    public String getText(String expression) {
        return (String) internalEval(expression, XPathConstants.STRING);
    }

    /**
     * Evaluates an XPath expression and returns the result as in the given
     * return type.
     * 
     * @param returnType
     *                The qualified name of the return type.
     * @return The evaluation result.
     */
    private Object internalEval(String expression, QName returnType) {
        try {
            return evaluate(expression, returnType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Indicates if processing is namespace aware.
     * 
     * @return True if processing is namespace aware.
     */
    public boolean isNamespaceAware() {
        return this.namespaceAware;
    }

    /**
     * Puts a new mapping between a prefix and a namespace URI.
     * 
     * @param prefix
     *                The namespace prefix.
     * @param namespaceURI
     *                The namespace URI.
     */
    public void putNamespace(String prefix, String namespaceURI) {
        getNamespaces().put(prefix, namespaceURI);
    }

    /**
     * Indicates if processing is namespace aware.
     * 
     * @param namespaceAware
     *                Indicates if processing is namespace aware.
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schemaRepresentation
     *                The XML schema representation to use.
     */
    public void validate(Representation schemaRepresentation) throws Exception {
        validate(schemaRepresentation, null);
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schema
     *                The XML schema to use.
     */
    public void validate(Schema schema) throws Exception {
        validate(schema, null);
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schemaRepresentation
     *                The XML schema representation to use.
     * @param result
     *                The Result object that receives (possibly augmented) XML.
     */
    public void validate(Representation schemaRepresentation, Result result)
            throws Exception {
        validate(getSchema(schemaRepresentation), result);
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schema
     *                The XML schema to use.
     * @param result
     *                The Result object that receives (possibly augmented) XML.
     */
    public void validate(Schema schema, Result result) throws Exception {
        StreamSource streamSource = new StreamSource(getStream());
        schema.newValidator().validate(streamSource, result);
    }

    /**
     * Returns the wrapped schema.
     * 
     * @return The wrapped schema.
     * @throws IOException
     */
    private static Schema getSchema(Representation schemaRepresentation)
            throws Exception {
        Schema result = null;

        if (schemaRepresentation != null) {
            StreamSource streamSource = new StreamSource(schemaRepresentation
                    .getStream());
            result = SchemaFactory.newInstance(
                    getSchemaLanguageUri(schemaRepresentation)).newSchema(
                    streamSource);
        }

        return result;
    }

    /**
     * Returns the schema URI for the current schema media type.
     * 
     * @return The schema URI.
     */
    private static String getSchemaLanguageUri(
            Representation schemaRepresentation) {
        String result = null;

        if (schemaRepresentation != null) {
            if (MediaType.APPLICATION_W3C_SCHEMA_XML
                    .equals(schemaRepresentation.getMediaType())) {
                result = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            } else if (MediaType.APPLICATION_RELAXNG_COMPACT
                    .equals(schemaRepresentation.getMediaType())) {
                result = XMLConstants.RELAXNG_NS_URI;
            } else if (MediaType.APPLICATION_RELAXNG_XML
                    .equals(schemaRepresentation.getMediaType())) {
                result = XMLConstants.RELAXNG_NS_URI;
            }
        }

        return result;
    }

}
