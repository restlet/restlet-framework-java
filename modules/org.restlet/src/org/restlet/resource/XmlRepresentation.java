/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathConstants;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.util.NodeSet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Representation based on an XML document. It knows how to evaluate XPath
 * expressions and how to manage a namespace context. This class also offers
 * convenient methods to validate the document against a specified XML scheme.
 * 
 * @author Jerome Louvel
 */
public abstract class XmlRepresentation extends OutputRepresentation implements
        NamespaceContext {

    /**
     * Returns a SAX source.
     * 
     * @param xmlRepresentation
     *            The XML representation to wrap.
     * @return A SAX source.
     * @throws IOException
     */
    public static SAXSource getSaxSource(Representation xmlRepresentation)
            throws IOException {
        SAXSource result = null;

        if (xmlRepresentation != null) {
            result = new SAXSource(new InputSource(xmlRepresentation
                    .getStream()));

            if (xmlRepresentation.getIdentifier() != null) {
                result.setSystemId(xmlRepresentation.getIdentifier()
                        .getTargetRef().toString());
            }
        }

        return result;
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
            final StreamSource streamSource = new StreamSource(
                    schemaRepresentation.getStream());
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
            if (MediaType.APPLICATION_W3C_SCHEMA.equals(schemaRepresentation
                    .getMediaType())) {
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

    /**
     * A SAX {@link EntityResolver} to use when resolving external entity
     * references while parsing this type of XML representations.
     * 
     * @see DocumentBuilder#setEntityResolver(EntityResolver)
     */
    private volatile EntityResolver entityResolver;

    /**
     * A SAX {@link ErrorHandler} to use for signaling SAX exceptions while
     * parsing this type of XML representations.
     * 
     * @see DocumentBuilder#setErrorHandler(ErrorHandler)
     */
    private volatile ErrorHandler errorHandler;

    /** Indicates if processing is namespace aware. */
    private volatile boolean namespaceAware;

    /** Internal map of namespaces. */
    private volatile Map<String, String> namespaces;

    /**
     * A (compiled) {@link Schema} to use when validating this type of XML
     * representations.
     * 
     * @see DocumentBuilderFactory#setSchema(Schema)
     */
    private volatile Schema schema;

    /**
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @see DocumentBuilderFactory#setValidating(boolean)
     */
    private volatile boolean validating;

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations.
     * 
     * @see DocumentBuilderFactory#setXIncludeAware(boolean)
     */
    private volatile boolean xIncludeAware;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
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
     *            The representation's mediaType.
     * @param expectedSize
     *            The expected input stream size.
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
     *            The qualified name of the return type.
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
     * Returns a document builder properly configured.
     * 
     * @return A document builder properly configured.
     */
    protected DocumentBuilder getDocumentBuilder() throws IOException {
        DocumentBuilder result = null;

        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            dbf.setNamespaceAware(isNamespaceAware());
            dbf.setValidating(isValidating());
            dbf.setXIncludeAware(isXIncludeAware());
            Schema xsd = getSchema();

            if (xsd != null) {
                dbf.setSchema(xsd);
            }

            result = dbf.newDocumentBuilder();
            result.setEntityResolver(getEntityResolver());
            result.setErrorHandler(getErrorHandler());
        } catch (ParserConfigurationException pce) {
            throw new IOException("Couldn't create the empty document: "
                    + pce.getMessage());
        }

        return result;
    }

    /**
     * Returns a DOM source.
     * 
     * @return A DOM source.
     * @throws IOException
     */
    public DOMSource getDomSource() throws IOException {
        DOMSource result = null;
        Node document = null;

        try {
            document = getDocumentBuilder().parse(getStream());
        } catch (SAXException se) {
            throw new IOException("Couldn't read the XML representation. "
                    + se.getMessage());
        }

        if (document != null) {
            result = new DOMSource(document);

            if (getIdentifier() != null) {
                result.setSystemId(getIdentifier().getTargetRef().toString());
            }
        }

        return result;
    }

    /**
     * Return the possibly null current SAX {@link EntityResolver}.
     * 
     * @return The possibly null current SAX {@link EntityResolver}.
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Return the possibly null current SAX {@link ErrorHandler}.
     * 
     * @return The possibly null current SAX {@link ErrorHandler}.
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns the map of namespaces.
     * 
     * @return The map of namespaces.
     */
    private Map<String, String> getNamespaces() {
        if (this.namespaces == null) {
            this.namespaces = new HashMap<String, String>();
        }
        return this.namespaces;
    }

    /**
     * {@inheritDoc
     * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String}
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
        final NodeList nodes = (NodeList) internalEval(expression,
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
     * {@inheritDoc
     * javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String}
     */
    public String getPrefix(String namespaceURI) {
        String result = null;

        for (final Entry<String, String> entry : getNamespaces().entrySet()) {
            if ((result == null) && entry.getValue().equals(namespaceURI)) {
                result = entry.getKey();
            }
        }

        return result;
    }

    /**
     * {@inheritDoc
     * javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String}
     */
    public Iterator<String> getPrefixes(String namespaceURI) {
        final List<String> result = new ArrayList<String>();

        for (final Entry<String, String> entry : getNamespaces().entrySet()) {
            if (entry.getValue().equals(namespaceURI)) {
                result.add(entry.getKey());
            }
        }

        return Collections.unmodifiableList(result).iterator();
    }

    /**
     * Returns a SAX source.
     * 
     * @return A SAX source.
     * @throws IOException
     */
    public SAXSource getSaxSource() throws IOException {
        return getSaxSource(this);
    }

    /**
     * Return the possibly null {@link Schema} to use for this type of XML
     * representations.
     * 
     * @return the {@link Schema} object of this type of XML representations.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Returns a stream of XML markup.
     * 
     * @return A stream of XML markup.
     * @throws IOException
     */
    public StreamSource getStreamSource() throws IOException {
        final StreamSource result = new StreamSource(getStream());

        if (getIdentifier() != null) {
            result.setSystemId(getIdentifier().getTargetRef().toString());
        }

        return result;
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
     *            The qualified name of the return type.
     * @return The evaluation result.
     */
    private Object internalEval(String expression, QName returnType) {
        try {
            return evaluate(expression, returnType);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @return True if the schema-based validation is enabled.
     */
    public boolean isValidating() {
        return validating;
    }

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations.
     * 
     * @return The current value of the xIncludeAware flag.
     */
    public boolean isXIncludeAware() {
        return xIncludeAware;
    }

    /**
     * Puts a new mapping between a prefix and a namespace URI.
     * 
     * @param prefix
     *            The namespace prefix.
     * @param namespaceURI
     *            The namespace URI.
     */
    public void putNamespace(String prefix, String namespaceURI) {
        getNamespaces().put(prefix, namespaceURI);
    }

    /**
     * Releases the namespaces map.
     */
    @Override
    public void release() {
        if (this.namespaces != null) {
            this.namespaces.clear();
            this.namespaces = null;
        }
        super.release();
    }

    /**
     * Set the {@link EntityResolver} to use when resolving external entity
     * references encountered in this type of XML representations.
     * 
     * @param entityResolver
     *            the {@link EntityResolver} to set.
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * Set the {@link ErrorHandler} to use when signaling SAX event exceptions.
     * 
     * @param errorHandler
     *            the {@link ErrorHandler} to set.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Indicates if processing is namespace aware.
     * 
     * @param namespaceAware
     *            Indicates if processing is namespace aware.
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Set a schema representation to be compiled and used when parsing and
     * validating this type of XML representations.
     * 
     * @param schema
     *            The schema representation to set.
     */
    public void setSchema(Representation schemaRepresentation) {
        try {
            this.schema = getSchema(schemaRepresentation);
        } catch (Exception e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to compile the schema representation", e);
        }
    }

    /**
     * Set a (compiled) {@link Schema} to use when parsing and validating this
     * type of XML representations.
     * 
     * @param schema
     *            The (compiled) {@link Schema} object to set.
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @param validating
     *            The new validation flag to set.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations.
     * 
     * @param includeAware
     *            The new value of the xIncludeAware flag.
     */
    public void setXIncludeAware(boolean includeAware) {
        xIncludeAware = includeAware;
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schemaRepresentation
     *            The XML schema representation to use.
     */
    public void validate(Representation schemaRepresentation) throws Exception {
        validate(schemaRepresentation, null);
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schemaRepresentation
     *            The XML schema representation to use.
     * @param result
     *            The Result object that receives (possibly augmented) XML.
     */
    public void validate(Representation schemaRepresentation, Result result)
            throws Exception {
        validate(getSchema(schemaRepresentation), result);
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schema
     *            The XML schema to use.
     */
    public void validate(Schema schema) throws Exception {
        validate(schema, null);
    }

    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schema
     *            The XML schema to use.
     * @param result
     *            The Result object that receives (possibly augmented) XML.
     */
    public void validate(Schema schema, Result result) throws Exception {
        schema.newValidator().validate(getSaxSource(), result);
    }

}
