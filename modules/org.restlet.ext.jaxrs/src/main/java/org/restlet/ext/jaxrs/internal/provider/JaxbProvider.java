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

package org.restlet.ext.jaxrs.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.restlet.Context;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Provider for JAXB objects.
 * 
 * @author Stephan Koops
 */
@Provider
@Produces({ "application/xml", MediaType.TEXT_XML, "application/*+xml" })
@Consumes({ "application/xml", MediaType.TEXT_XML, "application/*+xml" })
public class JaxbProvider extends AbstractJaxbProvider<Object> {

    /**
     * Specifies that the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     */
    private volatile boolean expandingEntityRefs;

    private final Logger logger = Context.getCurrentLogger();

    /** Limits potential XML overflow attacks. */
    private boolean secureProcessing;

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
     * Default constructor.
     */
    public JaxbProvider() {
        this.expandingEntityRefs = false;
        this.secureProcessing = true;
        this.validatingDtd = false;
        this.xIncludeAware = false;
    }

    @Override
    Logger getLogger() {
        return this.logger;
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

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    /**
     * Indicates if it limits potential XML overflow attacks.
     * 
     * @return True if it limits potential XML overflow attacks.
     */
    public boolean isSecureProcessing() {
        return secureProcessing;
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

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
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
     * @see MessageBodyReader#readFrom(Class, Type, MediaType, Annotation[],
     *      MultivaluedMap, InputStream)
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setXIncludeAware(isXIncludeAware());
            spf.setNamespaceAware(true);
            spf.setValidating(isValidatingDtd());
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,
                    isSecureProcessing());
            spf.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    isExpandingEntityRefs());
            spf.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    isExpandingEntityRefs());

            XMLReader reader = spf.newSAXParser().getXMLReader();
            JAXBContext jaxbContext = getJaxbContext(type);
            Unmarshaller um = jaxbContext.createUnmarshaller();
            return um.unmarshal(new SAXSource(reader, new InputSource(
                    entityStream)));
        } catch (Exception e) {
            throw new IOException("Could not unmarshal to " + type.getName(), e);
        }
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
     * Indicates if it limits potential XML overflow attacks.
     * 
     * @param secureProcessing
     *            True if it limits potential XML overflow attacks.
     */
    public void setSecureProcessing(boolean secureProcessing) {
        this.secureProcessing = secureProcessing;
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

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpResponseHeaders,
            OutputStream entityStream) throws IOException {
        marshal(object, entityStream);
    }
}
