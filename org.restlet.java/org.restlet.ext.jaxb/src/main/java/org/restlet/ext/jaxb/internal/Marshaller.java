/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jaxb.internal;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;

import jakarta.xml.bind.JAXBException;
import org.restlet.Context;
import org.restlet.ext.jaxb.JaxbRepresentation;

/**
 * This is a utility class to assist in marshaling Java content trees into XML.
 * Each {@code marshal} method takes a different target for the XML.
 * 
 * This class is a factory that constructs an instance of itself for multiple
 * uses. The created instance is thread safe and is optimized to be used for
 * multiple, possibly concurrent calls.
 * 
 * @author Overstock.com
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class Marshaller<T> {

    /** The JAXB classloader. */
    private final ClassLoader classLoader;

    /** The JAXB context path. */
    private final String contextPath;

    /** The parent JAXB representation. */
    private final JaxbRepresentation<T> jaxbRepresentation;

    /** Use thread identity to preserve the safety of access to marshalers. */
    private final ThreadLocal<jakarta.xml.bind.Marshaller> marshaller = new ThreadLocal<>() {

        @Override
        protected synchronized jakarta.xml.bind.Marshaller initialValue() {
            jakarta.xml.bind.Marshaller m = null;

            try {
                m = JaxbRepresentation.getContext(getContextPath(),
                        getClassLoader()).createMarshaller();

                m.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, getJaxbRepresentation()
                        .isFormattedOutput());

                if (getJaxbRepresentation().getSchemaLocation() != null) {
                    m.setProperty(jakarta.xml.bind.Marshaller.JAXB_SCHEMA_LOCATION,
                            getJaxbRepresentation().getSchemaLocation());
                }
                if (getJaxbRepresentation().getNoNamespaceSchemaLocation() != null) {
                    m.setProperty(jakarta.xml.bind.Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
                            getJaxbRepresentation()
                                    .getNoNamespaceSchemaLocation());
                }

                if (Marshaller.this.jaxbRepresentation.getCharacterSet() != null) {
                    m.setProperty(jakarta.xml.bind.Marshaller.JAXB_ENCODING,
                            Marshaller.this.jaxbRepresentation
                                    .getCharacterSet().getName());
                }

                if (getJaxbRepresentation().getNamespacePrefixMapper() != null) {
                    m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                            getJaxbRepresentation().getNamespacePrefixMapper());
                }

                m.setProperty(jakarta.xml.bind.Marshaller.JAXB_FRAGMENT, getJaxbRepresentation()
                        .isFragment());
            } catch (Exception e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Problem creating Marshaller", e);
                return null;
            }

            return m;
        }
    };

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
     * Returns the JAXB classloader.
     * 
     * @return The JAXB classloader.
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
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
    private jakarta.xml.bind.Marshaller getMarshaller() throws JAXBException {
        final jakarta.xml.bind.Marshaller m = this.marshaller.get();
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
     *            The root of the content tree to be marshaled.
     * @param stream
     *            The target output stream writes the XML to.
     * @throws JAXBException
     *             If any unexpected problem occurs during marshaling.
     */
    public void marshal(Object jaxbElement, OutputStream stream)
            throws JAXBException {
        marshal(jaxbElement, new OutputStreamWriter(stream));
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
        getMarshaller().setEventHandler(
                getJaxbRepresentation().getValidationEventHandler());
        getMarshaller().marshal(jaxbElement, writer);
    }

}
