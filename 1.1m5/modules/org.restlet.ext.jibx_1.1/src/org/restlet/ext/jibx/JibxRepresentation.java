/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jibx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;
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
 * @see <a href="http://jibx.sourceforge.net/">JiBX project< /a>
 * @author Florian Schwarz
 * @param <T>
 *            The type to wrap.
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
     *            The name of the binding to use.
     * @param bindingClass
     *            Target class for binding.
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
    private volatile Class bindingClass;

    /** The binding name to use. */
    private volatile String bindingName;

    /** The document encoding to use for marshalling (default is UTF-8). */
    private volatile String encoding = "UTF-8";

    /** The wrapped Java object. */
    private volatile T object;

    /** The source XML representation. */
    private volatile Representation xmlRepresentation;

    /**
     * Creates a JIBX representation from an existing Java object. This allows a
     * translation from a Java object to a XML representation.
     * 
     * @param mediaType
     *            The representation's media type (usually
     *            MediaType.APPLICATION_XML or MediaType.TEXT_XML).
     * @param object
     *            The Java object.
     */
    public JibxRepresentation(MediaType mediaType, T object) {
        this(mediaType, object, null);
    }

    /**
     * Creates a JIBX representation from an existing Java object. This allows a
     * translation from a Java object to a XML representation.
     * 
     * @param mediaType
     *            The representation's media type (usually
     *            MediaType.APPLICATION_XML or MediaType.TEXT_XML).
     * @param object
     *            The Java object.
     * @param bindingName
     *            The name of the JIBX binding to use.
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
     *            The XML wrapped in a representation.
     * @param bindingClass
     *            The Target Java class for binding.
     */
    @SuppressWarnings("unchecked")
    public JibxRepresentation(Representation xmlRepresentation,
            Class bindingClass) {
        this(xmlRepresentation, bindingClass, null);
    }

    /**
     * Creates a new JIBX representation, that can be used to convert the input
     * XML into a Java object. The XML is not validated.
     * 
     * @param xmlRepresentation
     *            The XML wrapped in a representation.
     * @param bindingClass
     *            The Target Java class for binding.
     * @param bindingName
     *            The name of the JIBX binding to use.
     */
    @SuppressWarnings("unchecked")
    public JibxRepresentation(Representation xmlRepresentation,
            Class bindingClass, String bindingName) {
        super(xmlRepresentation.getMediaType());
        this.xmlRepresentation = xmlRepresentation;
        this.bindingClass = bindingClass;
        this.bindingName = bindingName;
    }

    @Override
    public Object evaluate(String expression, QName returnType)
            throws Exception {
        Object result = null;
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(this);

        final Document xmlDocument = getDocumentBuilder().parse(
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
     * Returns the document encoding to use for marshalling. The default value
     * is UTF-8.
     * 
     * @return The document encoding to use for marshalling.
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Returns the wrapped Java object.
     * 
     * @return The wrapped Java object.
     * @throws JiBXException
     *             If unmarshalling XML with JIBX fails.
     * @throws IOException
     *             If any error occurs attempting to get the stream of the
     *             xmlRepresentation.
     */
    @SuppressWarnings("unchecked")
    public T getObject() throws JiBXException, IOException {
        if ((this.object == null) && (this.xmlRepresentation != null)) {
            // Try to unmarshal the wrapped XML representation
            final IBindingFactory jibxBFact = JibxRepresentation
                    .getBindingFactory(this.bindingName, this.bindingClass);
            final IUnmarshallingContext uctx = jibxBFact
                    .createUnmarshallingContext();
            return (T) uctx.unmarshalDocument(this.xmlRepresentation
                    .getStream(), null);
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
     *            The Java object to set.
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Marshals the document and writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     * @throws IOException
     *             If any error occurs attempting to write the stream.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            final IBindingFactory jibxBFact = JibxRepresentation
                    .getBindingFactory(this.bindingName, this.bindingClass);
            final IMarshallingContext mctx = jibxBFact
                    .createMarshallingContext();
            mctx
                    .marshalDocument(getObject(), this.encoding, null,
                            outputStream);
        } catch (final JiBXException e) {
            throw new IOException(e.getMessage());
        }
    }
}
