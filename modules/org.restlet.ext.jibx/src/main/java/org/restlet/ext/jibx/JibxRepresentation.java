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

package org.restlet.ext.jibx;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

/**
 * An XML representation based on JiBX that provides easy translation between
 * XML representations and Java objects with JiBX bindings.
 * 
 * @param <T>
 *            The type to wrap.
 * @author Florian Schwarz
 * @see <a href="http://jibx.sourceforge.net/">JiBX project</a>
 * @deprecated Use an XML serialization technology such as Jackson instead.
 */
@Deprecated
public class JibxRepresentation<T> extends WriterRepresentation {

    /**
     * Improves performance by caching contexts which are expensive to create.
     * (All binding factory instances are guaranteed to be thread safe and
     * reusable.)
     */
    private final static Map<String, IBindingFactory> bindingFactories = new ConcurrentHashMap<String, IBindingFactory>();

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
    private static synchronized IBindingFactory getBindingFactory(
            String bindingName, Class<?> bindingClass) throws JiBXException {

        // All binding factory instances are guaranteed to be thread safe and
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
    private volatile Class<?> bindingClass;

    /** The binding name to use. */
    private volatile String bindingName;

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
        setCharacterSet(CharacterSet.UTF_8);
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
    public JibxRepresentation(Representation xmlRepresentation,
            Class<?> bindingClass) {
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
    public JibxRepresentation(Representation xmlRepresentation,
            Class<?> bindingClass, String bindingName) {
        super(xmlRepresentation.getMediaType());
        this.xmlRepresentation = xmlRepresentation;
        this.bindingClass = bindingClass;
        this.bindingName = bindingName;
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
            IBindingFactory jibxBFact = JibxRepresentation.getBindingFactory(
                    this.bindingName, this.bindingClass);
            IUnmarshallingContext uctx = jibxBFact.createUnmarshallingContext();
            return (T) uctx.unmarshalDocument(
                    this.xmlRepresentation.getStream(), null);
        }

        return this.object;
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
     * Marshals the document and writes the representation to a stream of
     * characters.
     * 
     * @param writer
     *            The writer to use when writing.
     * @throws IOException
     *             If any error occurs attempting to write the stream.
     */
    @Override
    public void write(Writer writer) throws IOException {
        try {
            IBindingFactory jibxBFact = JibxRepresentation.getBindingFactory(
                    this.bindingName, this.bindingClass);
            IMarshallingContext mctx = jibxBFact.createMarshallingContext();
            mctx.setOutput(writer);
            ((IMarshallable) getObject()).marshal(mctx);
        } catch (JiBXException e) {
            throw new IOException(e.getMessage());
        }
    }
}
