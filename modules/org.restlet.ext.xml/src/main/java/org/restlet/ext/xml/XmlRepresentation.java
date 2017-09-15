/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Representation based on an XML document. It knows how to evaluate XPath
 * expressions and how to manage a namespace context. This class also offers
 * convenient methods to validate the document against a specified XML scheme.<br>
 * <br>
 * SECURITY WARNING: Using XML parsers configured to not prevent nor limit
 * document type definition (DTD) entity resolution can expose the parser to an
 * XML Entity Expansion injection attack.
 * 
 * @see <a
 *      href="https://github.com/restlet/restlet-framework-java/wiki/XEE-security-enhancements">XML
 *      Entity Expansion injection attack</a>
 * @author Jerome Louvel
 */
public abstract class XmlRepresentation extends WriterRepresentation
// [ifndef android]
        implements javax.xml.namespace.NamespaceContext
// [enddef]
{
    /**
     * True for expanding entity references when parsing XML representations;
     * default value provided by system property
     * "org.restlet.ext.xml.expandingEntityRefs", false by default.
     */
    public final static boolean XML_EXPANDING_ENTITY_REFS = Boolean
            .getBoolean("org.restlet.ext.xml.expandingEntityRefs");

    /**
     * True for validating DTD documents when parsing XML representations;
     * default value provided by system property
     * "org.restlet.ext.xml.validatingDtd", false by default.
     */
    public final static boolean XML_VALIDATING_DTD = Boolean
            .getBoolean("org.restlet.ext.xml.validatingDtd");

    // [ifdef android] method
    /**
     * Appends the text content of a given node and its descendants to the given
     * buffer.
     * 
     * @param node
     *            The node.
     * @param sb
     *            The buffer.
     */
    private static void appendTextContent(Node node, StringBuilder sb) {
        switch (node.getNodeType()) {
        case Node.TEXT_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.CDATA_SECTION_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.COMMENT_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.ENTITY_REFERENCE_NODE:
            if (node.getNodeName().startsWith("#")) {
                int ch = Integer.parseInt(node.getNodeName().substring(1));
                sb.append((char) ch);
            }
            break;
        case Node.ELEMENT_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        case Node.ATTRIBUTE_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        case Node.ENTITY_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        default:
            break;
        }
    }

    // [ifndef android] method
    /**
     * Returns a SAX source.
     * 
     * @param xmlRepresentation
     *            The XML representation to wrap.
     * @return A SAX source.
     * @throws IOException
     */
    public static javax.xml.transform.sax.SAXSource getSaxSource(
            Representation xmlRepresentation) throws IOException {
        javax.xml.transform.sax.SAXSource result = null;

        if (xmlRepresentation != null) {
            result = new javax.xml.transform.sax.SAXSource(new InputSource(
                    xmlRepresentation.getStream()));

            if (xmlRepresentation.getLocationRef() != null) {
                result.setSystemId(xmlRepresentation.getLocationRef()
                        .getTargetRef().toString());
            }
        }

        return result;
    }

    // [ifndef android] method
    /**
     * Returns the wrapped schema.
     * 
     * @return The wrapped schema.
     * @throws IOException
     */
    private static javax.xml.validation.Schema getSchema(
            Representation schemaRepresentation) throws Exception {
        javax.xml.validation.Schema result = null;

        if (schemaRepresentation != null) {
            final javax.xml.transform.stream.StreamSource streamSource = new javax.xml.transform.stream.StreamSource(
                    schemaRepresentation.getStream());
            result = javax.xml.validation.SchemaFactory.newInstance(
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

    // [ifdef android] method
    /**
     * Returns the text content of a given node and its descendants.
     * 
     * @param node
     *            The node.
     * @return The text content of a given node.
     */
    public static String getTextContent(Node node) {
        StringBuilder sb = new StringBuilder();
        appendTextContent(node, sb);
        return sb.toString();
    }

    /**
     * Specifies that the parser will convert CDATA nodes to text nodes and
     * append it to the adjacent (if any) text node. By default the value of
     * this is set to false.
     */
    private volatile boolean coalescing;

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

    /**
     * Specifies that the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     */
    private volatile boolean expandingEntityRefs;

    /**
     * Indicates if the parser will ignore comments. By default the value of
     * this is set to false.
     */
    private volatile boolean ignoringComments;

    /**
     * Indicates if the parser will ignore extra white spaces in element
     * content. By default the value of this is set to false.
     */
    private volatile boolean ignoringExtraWhitespaces;

    /** Indicates if processing is namespace aware. */
    private volatile boolean namespaceAware;

    /** Internal map of namespaces. */
    private volatile Map<String, String> namespaces;

    // [ifndef android] member
    /**
     * A (compiled) {@link javax.xml.validation.Schema} to use when validating
     * this type of XML representations.
     * 
     * @see DocumentBuilderFactory#setSchema(javax.xml.validation.Schema)
     */
    private volatile javax.xml.validation.Schema schema;

    /**
     * Indicates the desire for validating this type of XML representations
     * against a DTD. Note that for XML schema or Relax NG validation, use the
     * "schema" property instead.
     * 
     * @see DocumentBuilderFactory#setValidating(boolean)
     */
    private volatile boolean validatingDtd;

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations. By default the value of this is set to
     * false.
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
        this(mediaType, UNKNOWN_SIZE);
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
        this.coalescing = false;
        this.entityResolver = null;
        this.errorHandler = null;
        this.expandingEntityRefs = XML_EXPANDING_ENTITY_REFS;
        this.ignoringComments = false;
        this.ignoringExtraWhitespaces = false;
        this.namespaceAware = false;
        this.namespaces = null;
        this.validatingDtd = XML_VALIDATING_DTD;
        this.xIncludeAware = false;
        // [ifndef android] line
        this.schema = null;
    }

    // [ifndef android] method
    /**
     * Evaluates an XPath expression as a boolean. If the evaluation fails, null
     * will be returned.
     * 
     * @return The evaluation result.
     */
    public Boolean getBoolean(String expression) {
        return (Boolean) internalEval(expression,
                javax.xml.xpath.XPathConstants.BOOLEAN);
    }

    /**
     * Returns the XML representation as a DOM document.
     * 
     * @return The DOM document.
     */
    protected Document getDocument() throws Exception {
        return getDocumentBuilder().parse(getInputSource());
    }

    /**
     * Returns a document builder properly configured.
     * 
     * @return A document builder properly configured.
     */
    protected DocumentBuilder getDocumentBuilder() throws IOException {
        DocumentBuilder result = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(isNamespaceAware());
            dbf.setValidating(isValidatingDtd());
            dbf.setCoalescing(isCoalescing());
            dbf.setExpandEntityReferences(isExpandingEntityRefs());
            dbf.setIgnoringComments(isIgnoringComments());
            dbf.setIgnoringElementContentWhitespace(isIgnoringExtraWhitespaces());

            try {
                dbf.setXIncludeAware(isXIncludeAware());
            } catch (UnsupportedOperationException uoe) {
                Context.getCurrentLogger().log(Level.FINE,
                        "The JAXP parser doesn't support XInclude.", uoe);
            }

            // [ifndef android]
            javax.xml.validation.Schema xsd = getSchema();

            if (xsd != null) {
                dbf.setSchema(xsd);
            }
            // [enddef]

            result = dbf.newDocumentBuilder();
            result.setEntityResolver(getEntityResolver());
            result.setErrorHandler(getErrorHandler());
        } catch (ParserConfigurationException pce) {
            throw new IOException("Couldn't create the empty document: "
                    + pce.getMessage());
        }

        return result;
    }

    // [ifndef android] method
    /**
     * Returns a DOM source.
     * 
     * @return A DOM source.
     * @throws IOException
     */
    public javax.xml.transform.dom.DOMSource getDomSource() throws IOException {
        javax.xml.transform.dom.DOMSource result = null;
        Node document = null;

        try {
            document = getDocumentBuilder().parse(getInputSource());
        } catch (SAXException se) {
            throw new IOException("Couldn't read the XML representation. "
                    + se.getMessage());
        }

        if (document != null) {
            result = new javax.xml.transform.dom.DOMSource(document);

            if (getLocationRef() != null) {
                result.setSystemId(getLocationRef().getTargetRef().toString());
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
     * Returns the XML representation as a SAX input source.
     * 
     * @return The SAX input source.
     */
    public abstract InputSource getInputSource() throws IOException;

    /**
     * Returns the map of namespaces. Namespace prefixes are keys and URI
     * references are values.
     * 
     * @return The map of namespaces.
     */
    public Map<String, String> getNamespaces() {
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
        return (this.namespaces == null) ? null : this.namespaces.get(prefix);
    }

    // [ifndef android] method
    /**
     * Evaluates an XPath expression as a DOM Node. If the evaluation fails,
     * null will be returned.
     * 
     * @return The evaluation result.
     */
    public Node getNode(String expression) {
        return (Node) internalEval(expression,
                javax.xml.xpath.XPathConstants.NODE);
    }

    // [ifndef android] method
    /**
     * Evaluates an XPath expression as a DOM NodeList. If the evaluation fails,
     * null will be returned.
     * 
     * @return The evaluation result.
     */
    public NodeList getNodes(String expression) {
        final org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) internalEval(
                expression, javax.xml.xpath.XPathConstants.NODESET);
        return (nodes == null) ? null : new NodeList(nodes);
    }

    // [ifndef android] method
    /**
     * Evaluates an XPath expression as a number. If the evaluation fails, null
     * will be returned.
     * 
     * @return The evaluation result.
     */
    public Double getNumber(String expression) {
        return (Double) internalEval(expression,
                javax.xml.xpath.XPathConstants.NUMBER);
    }

    /**
     * {@inheritDoc
     * javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String}
     */
    public String getPrefix(String namespaceURI) {
        String result = null;
        boolean found = false;

        for (Iterator<String> iterator = getNamespaces().keySet().iterator(); iterator
                .hasNext() && !found;) {
            String key = iterator.next();
            if (getNamespaces().get(key).equals(namespaceURI)) {
                found = true;
                result = key;
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

        for (Iterator<String> iterator = getNamespaces().keySet().iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (getNamespaces().get(key).equals(namespaceURI)) {
                result.add(key);
            }
        }

        return Collections.unmodifiableList(result).iterator();
    }

    // [ifndef android] method
    /**
     * Returns a SAX source.
     * 
     * @return A SAX source.
     * @throws IOException
     */
    public javax.xml.transform.sax.SAXSource getSaxSource() throws IOException {
        return getSaxSource(this);
    }

    // [ifndef android] method
    /**
     * Return the possibly null {@link javax.xml.validation.Schema} to use for
     * this type of XML representations.
     * 
     * @return the {@link javax.xml.validation.Schema} object of this type of
     *         XML representations.
     */
    public javax.xml.validation.Schema getSchema() {
        return schema;
    }

    // [ifndef android] method
    /**
     * Returns a stream of XML markup.
     * 
     * @return A stream of XML markup.
     * @throws IOException
     */
    public javax.xml.transform.stream.StreamSource getStreamSource()
            throws IOException {
        final javax.xml.transform.stream.StreamSource result = new javax.xml.transform.stream.StreamSource(
                getStream());

        if (getLocationRef() != null) {
            result.setSystemId(getLocationRef().getTargetRef().toString());
        }

        return result;
    }

    // [ifndef android] method
    /**
     * Evaluates an XPath expression as a string.
     * 
     * @return The evaluation result.
     */
    public String getText(String expression) {
        return (String) internalEval(expression,
                javax.xml.xpath.XPathConstants.STRING);
    }

    // [ifndef android] method
    /**
     * Evaluates an XPath expression and returns the result as in the given
     * return type.
     * 
     * @param returnType
     *            The qualified name of the return type.
     * @return The evaluation result.
     */
    private Object internalEval(String expression,
            javax.xml.namespace.QName returnType) {
        try {
            Object result = null;
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(this);
            Document xmlDocument = getDocument();

            if (xmlDocument != null) {
                result = xpath.evaluate(expression, xmlDocument, returnType);
            } else {
                throw new Exception(
                        "Unable to obtain a DOM document for the XML representation. "
                                + "XPath evaluation cancelled.");
            }

            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Indicates if the parser should be coalescing text. If true the parser
     * will convert CDATA nodes to text nodes and append it to the adjacent (if
     * any) text node. By default the value of this is set to false.
     * 
     * @return True if parser should be coalescing text.
     */
    public boolean isCoalescing() {
        return coalescing;
    }

    /**
     * Indicates if the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     * 
     * @return True if the parser will expand entity reference nodes.
     */
    public boolean isExpandingEntityRefs() {
        return expandingEntityRefs;
    }

    /**
     * Indicates if the parser will ignore comments. By default the value of
     * this is set to false.
     * 
     * @return True if the parser will ignore comments.
     */
    public boolean isIgnoringComments() {
        return ignoringComments;
    }

    /**
     * Indicates if the parser will ignore extra white spaces in element
     * content. Note that the {@link #isValidatingDtd()} must be true when this
     * property is 'true' as validation is needed for it to work. By default the
     * value of this is set to false.
     * 
     * @return True if the parser will ignore extra white spaces.
     */
    public boolean isIgnoringExtraWhitespaces() {
        return ignoringExtraWhitespaces;
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
    public boolean isValidatingDtd() {
        return validatingDtd;
    }

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations. By default the value of this is set to
     * false.
     * 
     * @return The current value of the xIncludeAware flag.
     */
    public boolean isXIncludeAware() {
        return xIncludeAware;
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
     * Indicates if the parser should be coalescing text. If true the parser
     * will convert CDATA nodes to text nodes and append it to the adjacent (if
     * any) text node. By default the value of this is set to false.
     * 
     * @param coalescing
     *            True if parser should be coalescing text.
     */
    public void setCoalescing(boolean coalescing) {
        this.coalescing = coalescing;
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
     * Indicates if the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     * 
     * @param expandEntityRefs
     *            True if the parser will expand entity reference nodes.
     */
    public void setExpandingEntityRefs(boolean expandEntityRefs) {
        this.expandingEntityRefs = expandEntityRefs;
    }

    /**
     * Indicates if the parser will ignore comments. By default the value of
     * this is set to false.
     * 
     * @param ignoringComments
     *            True if the parser will ignore comments.
     */
    public void setIgnoringComments(boolean ignoringComments) {
        this.ignoringComments = ignoringComments;
    }

    /**
     * Indicates if the parser will ignore extra white spaces in element
     * content. Note that the {@link #setValidatingDtd(boolean)} will be invoked
     * with 'true' if setting this property to 'true' as validation is needed
     * for it to work.
     * 
     * @param ignoringExtraWhitespaces
     *            True if the parser will ignore extra white spaces in element
     *            content.
     */
    public void setIgnoringExtraWhitespaces(boolean ignoringExtraWhitespaces) {
        if (this.ignoringExtraWhitespaces != ignoringExtraWhitespaces) {
            if (ignoringExtraWhitespaces) {
                setValidatingDtd(true);
            }

            this.ignoringExtraWhitespaces = ignoringExtraWhitespaces;
        }
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
     * Sets the map of namespaces.
     * 
     * @param namespaces
     *            The map of namespaces.
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    // [ifndef android] method
    /**
     * Set a (compiled) {@link javax.xml.validation.Schema} to use when parsing
     * and validating this type of XML representations.
     * 
     * @param schema
     *            The (compiled) {@link javax.xml.validation.Schema} object to
     *            set.
     */
    public void setSchema(javax.xml.validation.Schema schema) {
        this.schema = schema;
    }

    // [ifndef android] method
    /**
     * Set a schema representation to be compiled and used when parsing and
     * validating this type of XML representations.
     * 
     * @param schemaRepresentation
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
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @param validating
     *            The new validation flag to set.
     */
    public void setValidatingDtd(boolean validating) {
        this.validatingDtd = validating;
    }

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations. By default the value of this is set to
     * false.
     * 
     * @param includeAware
     *            The new value of the xIncludeAware flag.
     */
    public void setXIncludeAware(boolean includeAware) {
        xIncludeAware = includeAware;
    }

    // [ifndef android] method
    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schema
     *            The XML schema to use.
     */
    public void validate(javax.xml.validation.Schema schema) throws Exception {
        validate(schema, null);
    }

    // [ifndef android] method
    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schema
     *            The XML schema to use.
     * @param result
     *            The Result object that receives (possibly augmented) XML.
     */
    public void validate(javax.xml.validation.Schema schema,
            javax.xml.transform.Result result) throws Exception {
        schema.newValidator().validate(getSaxSource(), result);
    }

    // [ifndef android] method
    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schemaRepresentation
     *            The XML schema representation to use.
     */
    public void validate(Representation schemaRepresentation) throws Exception {
        validate(schemaRepresentation, null);
    }

    // [ifndef android] method
    /**
     * Validates the XML representation against a given schema.
     * 
     * @param schemaRepresentation
     *            The XML schema representation to use.
     * @param result
     *            The Result object that receives (possibly augmented) XML.
     */
    public void validate(Representation schemaRepresentation,
            javax.xml.transform.Result result) throws Exception {
        validate(getSchema(schemaRepresentation), result);
    }

}
