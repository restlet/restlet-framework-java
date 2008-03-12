package org.restlet.ext.jibx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.XmlRepresentation;
import org.w3c.dom.Document;

/**
 * An XML representation based on JIBX that provides easy translation between
 * XML representations and Java objects with JIBX bindings.
 * 
 * @see <a href="http://jibx.sourceforge.net/">JiBX project</a>
 * @author Florian Schwarz
 * @param <T>
 *                The type to wrap.
 */
public class JibxRepresentation<T> extends XmlRepresentation {

    /**
     * Improves performance by caching contexts which are expensive to create.
     * (All binding factory instances are guaranteed to be threadsafe and
     * reusable.)
     */
    private final static Map<String, IBindingFactory> bindingFactories = new TreeMap<String, IBindingFactory>();

    /**
     * Get a cached binding factory.
     * 
     * @param bindingName
     *                The name of the binding to use.
     * @param bindingClass
     *                Target class for binding.
     * @return A binding factory.
     * @throws JiBXException
     */
    @SuppressWarnings("unchecked")
    private static synchronized IBindingFactory getBindingFactory(
            String bindingName, Class bindingClass) throws JiBXException {

        // All binding factory instances are guaranteed to be threadsafe and
        // reusable.

        IBindingFactory jibxBFact = bindingFactories.get(bindingName
                + bindingClass.toString());

        if (jibxBFact == null) {
            if (bindingName == null) {
                jibxBFact = BindingDirectory.getFactory(bindingClass);
            } else {
                jibxBFact = BindingDirectory.getFactory(bindingName,
                        bindingClass);
            }

            bindingFactories.put(bindingName + bindingClass.toString(),
                    jibxBFact);
        }

        return jibxBFact;
    }

    /** Class for target binding. */
    @SuppressWarnings("unchecked")
    private Class bindingClass;

    /** The binding name to use. */
    private String bindingName;

    /** The document encoding to use for marshalling (default is UTF-8). */
    private String encoding = "UTF-8";

    /** The wrapped Java object. */
    private T object;

    /** The source XML representation. */
    private Representation xmlRepresentation;

    /**
     * Creates a JIBX representation from an existing Java object. This allows a
     * translation from a Java object to a XML representation.
     * 
     * @param mediaType
     *                The representation's media type (usually
     *                MediaType.APPLICATION_XML or MediaType.TEXT_XML).
     * @param object
     *                The Java object.
     */
    public JibxRepresentation(MediaType mediaType, T object) {
        this(mediaType, object, null);
    }

    /**
     * Creates a JIBX representation from an existing Java object. This allows a
     * translation from a Java object to a XML representation.
     * 
     * @param mediaType
     *                The representation's media type (usually
     *                MediaType.APPLICATION_XML or MediaType.TEXT_XML).
     * @param object
     *                The Java object.
     * @param bindingName
     *                The name of the JIBX binding to use.
     */
    public JibxRepresentation(MediaType mediaType, T object, String bindingName) {
        super(mediaType);
        this.object = object;
        this.bindingClass = object.getClass();
        this.bindingName = bindingName;
    }

    /**
     * Creates a new JIBX representation, that can be used to convert the input
     * XML into a Java object. The XML is not validated.
     * 
     * @param xmlRepresentation
     *                The XML wrapped in a representation.
     * @param bindingClass
     *                The Target Java class for binding.
     * @param bindingName
     *                The name of the JIBX binding to use.
     */
    @SuppressWarnings("unchecked")
    public JibxRepresentation(Representation xmlRepresentation,
            Class bindingClass, String bindingName) {
        super(xmlRepresentation.getMediaType());
        this.xmlRepresentation = xmlRepresentation;
        this.bindingClass = bindingClass;
        this.bindingName = bindingName;
    }

    /**
     * Creates a new JIBX representation, that can be used to convert the input
     * XML into a Java object. The XML is not validated.
     * 
     * @param xmlRepresentation
     *                The XML wrapped in a representation.
     * @param bindingClass
     *                The Target Java class for binding.
     */
    @SuppressWarnings("unchecked")
    public JibxRepresentation(Representation xmlRepresentation,
            Class bindingClass) {
        this(xmlRepresentation, bindingClass, null);
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        Object result = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(this);

        Document xmlDocument = getDocumentBuilder().parse(
                this.xmlRepresentation.getStream());

        if (xmlDocument != null) {
            result = xpath.evaluate(expression, xmlDocument, returnType);
        } else {
            throw new Exception(
                    "Unable to obtain a DOM document for the XML representation. "
                            + "XPath evaluation cancelled.");
        }
        return result;
    }

    /**
     * Returns a document builder properly configured.
     * 
     * @return A document builder properly configured.
     */
    private DocumentBuilder getDocumentBuilder() throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(isNamespaceAware());
            dbf.setValidating(false);
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new IOException("Couldn't create the empty document: "
                    + pce.getMessage());
        }
    }

    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns the wrapped Java object.
     * 
     * @return The wrapped Java object.
     * @throws JiBXException
     *                 If unmarshalling XML with JIBX fails.
     * @throws IOException
     *                 If any error occurs attempting to get the stream of the
     *                 xmlRepresentation.
     */
    @SuppressWarnings("unchecked")
    public T getObject() throws JiBXException, IOException {
        if ((this.object == null) && (this.xmlRepresentation != null)) {
            // Try to unmarshal the wrapped XML representation
            IBindingFactory jibxBFact = JibxRepresentation.getBindingFactory(
                    bindingName, bindingClass);
            IUnmarshallingContext uctx = jibxBFact.createUnmarshallingContext();
            return (T) uctx.unmarshalDocument(xmlRepresentation.getStream(),
                    null);
        }
        return this.object;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the wrapped Java object.
     * 
     * @param object
     *                The Java object to set.
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Marshals the document and writes the representation to a byte stream.
     * 
     * @param outputStream
     *                The output stream.
     * @throws IOException
     *                 If any error occurs attempting to write the stream.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            IBindingFactory jibxBFact = JibxRepresentation.getBindingFactory(
                    bindingName, bindingClass);
            IMarshallingContext mctx = jibxBFact.createMarshallingContext();
            mctx.marshalDocument(getObject(), encoding, null, outputStream);
        } catch (JiBXException e) {
            throw new IOException(e.getMessage());
        }
    }
}
