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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;

import org.restlet.Context;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.StringRepresentation;

/**
 * This is a utility class to assist in unmarshaling XML into a new Java content
 * tree.
 * 
 * Each {@code unmarshal} method takes a different source for the XML. This
 * class caches information to improve unmarshaling performance across calls
 * using the same schema (package).
 * 
 * @author Overstock.com
 */
public class Unmarshaller<T> {

    /** The JAXB context path. */
    private final String contextPath;

    /**
     * Use thread identity to preserve safety of access to unmarshallers.
     */
    private final ThreadLocal<javax.xml.bind.Unmarshaller> unmarshaller = new ThreadLocal<javax.xml.bind.Unmarshaller>() {
        @Override
        protected synchronized javax.xml.bind.Unmarshaller initialValue() {
            javax.xml.bind.Unmarshaller m = null;
            try {
                m = JaxbRepresentation.getContext(getContextPath(),
                        getClassLoader()).createUnmarshaller();
            } catch (Exception e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Problem creating Unmarshaller", e);
                return null;
            }
            return m;
        }
    };

    /** The JAXB classloader. */
    private final ClassLoader classLoader;

    /**
     * Constructor.
     * 
     * @param contextPath
     *            The JAXB context path.
     * @param classloader
     *            The JAXB classloader.
     */
    public Unmarshaller(String contextPath, ClassLoader classloader) {
        this.contextPath = contextPath;
        this.classLoader = classloader;
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
     * Returns the JAXB unmarshaller.
     * 
     * @return The JAXB unmarshaller.
     * @throws JAXBException
     */
    private javax.xml.bind.Unmarshaller getUnmarshaller() throws JAXBException {
        final javax.xml.bind.Unmarshaller m = this.unmarshaller.get();
        if (m == null) {
            Context.getCurrentLogger()
                    .warning("Unable to locate unmarshaller.");
            throw new JAXBException("Unable to locate unmarshaller.");
        }
        return m;
    }

    /**
     * Sets the validation handler for this unmarshaller.
     * 
     * @param handler
     *            A validation handler.
     * @throws JAXBException
     *             If an error was encountered while setting the event handler.
     */
    public void setEventHandler(ValidationEventHandler handler)
            throws JAXBException {
        getUnmarshaller().setEventHandler(handler);
    }

    /**
     * Unmarshal XML data from the specified input stream and return the
     * resulting Java content tree.
     * 
     * @param stream
     *            The source input stream.
     * @return The newly created root object of the Java content tree.
     * @throws JAXBException
     *             If any unexpected problem occurs during unmarshaling.
     * @throws IOException
     *             If an error occurs accessing the string representation.
     */
    public Object unmarshal(InputStream stream) throws JAXBException {
        return getUnmarshaller().unmarshal(stream);
    }

    /**
     * Unmarshal XML data from the specified reader and return the resulting
     * Java content tree.
     * 
     * @param reader
     *            The source reader.
     * @return The newly created root object of the Java content tree.
     * @throws JAXBException
     *             If any unexpected problem occurs during unmarshaling.
     * @throws IOException
     *             If an error occurs accessing the string representation.
     */
    public Object unmarshal(Reader reader) throws JAXBException {
        return getUnmarshaller().unmarshal(reader);
    }

    /**
     * Unmarshal XML data from the specified Restlet string representation and
     * return the resulting Java content tree.
     * 
     * @param rep
     *            The source string representation.
     * @return The newly created root object of the Java content tree.
     * @throws JAXBException
     *             If any unexpected problem occurs during unmarshaling.
     * @throws IOException
     *             If an error occurs accessing the string representation.
     */
    public Object unmarshal(StringRepresentation rep) throws JAXBException,
            IOException {
        return getUnmarshaller().unmarshal(rep.getReader());
    }
}