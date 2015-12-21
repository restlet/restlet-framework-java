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

package org.restlet.ext.jaxb.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.restlet.Context;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

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

    /** The JAXB classloader. */
    private final ClassLoader classLoader;

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
     * Unmarshal XML data from the specified Restlet string representation and
     * return the resulting Java content tree.
     * 
     * @param jaxbRep
     *            The source JAXB representation.
     * @return The newly created root object of the Java content tree.
     * @throws JAXBException
     *             If any unexpected problem occurs during unmarshaling.
     * @throws IOException
     *             If an error occurs accessing the string representation.
     */
    public Object unmarshal(JaxbRepresentation<?> jaxbRep)
            throws JAXBException, IOException {
        return unmarshal(jaxbRep, jaxbRep.getReader());
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
    public Object unmarshal(JaxbRepresentation<?> jaxbRep, InputStream stream)
            throws JAXBException {
        return unmarshal(jaxbRep, new InputStreamReader(stream));
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
    public Object unmarshal(JaxbRepresentation<?> jaxbRep, Reader reader)
            throws JAXBException {
        SAXSource ss = null;

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();

            // Keep before the external entity preferences
            spf.setNamespaceAware(true);
            spf.setValidating(jaxbRep.isValidatingDtd());
            spf.setXIncludeAware(jaxbRep.isXIncludeAware());
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,
                    jaxbRep.isSecureProcessing());
            spf.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    jaxbRep.isExpandingEntityRefs());
            spf.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    jaxbRep.isExpandingEntityRefs());
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            ss = new SAXSource(xmlReader, new InputSource(reader));
        } catch (Exception e) {
            throw new JAXBException("Unable to create customized SAX source", e);
        }

        getUnmarshaller().setEventHandler(jaxbRep.getValidationEventHandler());
        return getUnmarshaller().unmarshal(ss);
    }
}
