/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.ext.jaxb.internal;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;

import org.restlet.Context;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.StringRepresentation;

/**
 * This is a utility class to assist in marshaling Java content trees into XML.
 * Each {@code marshal} method takes a different target for the XML.
 * 
 * This class is a factory that constructs an instance of itself for multiple
 * uses. The created instance is thread safe and is optimized to be used for
 * multiple, possibly concurrent calls.
 * 
 * @author Overstock.com
 */
public class Marshaller<T> {

    /** The parent JAXB representation. */
    private final JaxbRepresentation<T> jaxbRepresentation;

    /** Use thread identity to preserve safety of access to marshalers. */
    private final ThreadLocal<javax.xml.bind.Marshaller> marshaller = new ThreadLocal<javax.xml.bind.Marshaller>() {

        @Override
        protected synchronized javax.xml.bind.Marshaller initialValue() {
            javax.xml.bind.Marshaller m = null;

            try {
                m = JaxbRepresentation.getContext(getContextPath(),
                        getClassLoader()).createMarshaller();
                m.setProperty("jaxb.formatted.output", getJaxbRepresentation()
                        .isFormattedOutput());

                if (getJaxbRepresentation().getSchemaLocation() != null) {
                    m.setProperty("jaxb.schemaLocation",
                            getJaxbRepresentation().getSchemaLocation());
                }
                if (getJaxbRepresentation().getNoNamespaceSchemaLocation() != null) {
                    m.setProperty("jaxb.noNamespaceSchemaLocation",
                            getJaxbRepresentation()
                                    .getNoNamespaceSchemaLocation());
                }

                if (Marshaller.this.jaxbRepresentation.getCharacterSet() != null) {
                    m.setProperty("jaxb.encoding",
                            Marshaller.this.jaxbRepresentation
                                    .getCharacterSet().getName());
                }

                m.setProperty("jaxb.fragment", getJaxbRepresentation()
                        .isFragment());
            } catch (Exception e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Problem creating Marshaller", e);
                return null;
            }

            return m;
        }
    };

    /** The JAXB context path. */
    private final String contextPath;

    /** The JAXB classloader. */
    private final ClassLoader classLoader;

    // This is a factory class.
    public Marshaller(JaxbRepresentation<T> jaxbRepresentation) {
        this(jaxbRepresentation, null, null);
    }

    /**
     * Constructor.
     * 
     * @param jaxbRepresentation
     *            The JAXB representation to marshal.
     * @param contextPath
     *            The JAXB context path.
     * @param classLoader
     *            The JAXB classloader.
     */
    public Marshaller(JaxbRepresentation<T> jaxbRepresentation,
            String contextPath, ClassLoader classLoader) {
        this.jaxbRepresentation = jaxbRepresentation;
        this.contextPath = contextPath;
        this.classLoader = classLoader;
    }

    /**
     * Returns the JAXB context path.
     * 
     * @return The JAXB context path.
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Returns the JAXB classloader.
     * 
     * @return The JAXB classloader.
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * Returns the parent JAXB representation.
     * 
     * @return The parent JAXB representation.
     */
    public JaxbRepresentation<T> getJaxbRepresentation() {
        return jaxbRepresentation;
    }

    /**
     * Returns the JAXB marshaller.
     * 
     * @return The JAXB marshaller.
     * @throws JAXBException
     */
    private javax.xml.bind.Marshaller getMarshaller() throws JAXBException {
        final javax.xml.bind.Marshaller m = this.marshaller.get();
        if (m == null) {
            Context.getCurrentLogger().warning("Unable to locate marshaller.");
            throw new JAXBException("Unable to locate marshaller.");
        }
        return m;
    }

    /**
     * Marshals the content tree rooted at {@code jaxbElement} into an output
     * stream.
     * 
     * @param jaxbElement
     *            The root of the content tree to be marshalled.
     * @param stream
     *            The target output stream write the XML to.
     * @throws JAXBException
     *             If any unexpected problem occurs during marshalling.
     */
    public void marshal(Object jaxbElement, OutputStream stream)
            throws JAXBException {
        getMarshaller().marshal(jaxbElement, stream);
    }

    /**
     * Marshal the content tree rooted at {@code jaxbElement} into a Restlet
     * String representation.
     * 
     * @param jaxbElement
     *            The root of the content tree to be marshaled.
     * @param rep
     *            The target string representation write the XML to.
     * @throws JAXBException
     *             If any unexpected problem occurs during marshaling.
     */
    public void marshal(Object jaxbElement, StringRepresentation rep)
            throws JAXBException {
        final StringWriter writer = new StringWriter();
        marshal(jaxbElement, writer);
        rep.setText(writer.toString());
    }

    /**
     * Marshal the content tree rooted at {@code jaxbElement} into a writer.
     * 
     * @param jaxbElement
     *            The root of the content tree to be marshaled.
     * @param writer
     *            The target writer to write the XML to.
     * @throws JAXBException
     *             If any unexpected problem occurs during marshaling.
     */
    public void marshal(Object jaxbElement, Writer writer) throws JAXBException {
        getMarshaller().marshal(jaxbElement, writer);
    }

    /**
     * Sets the validation handler for this marshaler.
     * 
     * @param handler
     *            A validation handler.
     * @throws JAXBException
     *             If an error was encountered while setting the event handler.
     */
    public void setEventHandler(ValidationEventHandler handler)
            throws JAXBException {
        getMarshaller().setEventHandler(handler);
    }

}