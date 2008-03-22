/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.internal.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.restlet.ext.jaxrs.XsltSource;

// REQUEST again: where get XSLT for javax.xml.transform.Source to OutputStream?

/**
 * JAX-RS Provider to convert a {@link Source} to an object and vice versa.
 * 
 * @author Stephan Koops
 */
@Provider
public class XsltProvider implements MessageBodyWriter<Source>,
        MessageBodyReader<Source> {

    private Logger logger = Logger.getLogger(XsltProvider.class.getName());

    private Map<XsltSource, Transformer> transformerCache = new HashMap<XsltSource, Transformer>();

    private final TransformerFactory transformerFactory = TransformerFactory
            .newInstance();

    // LATER make XsltProvider resettable and self reloading.
    // wait for caching capabilities of Restlet.

    public long getSize(Source object) {
        return -1;
    }

    /**
     * Returns the {@link Source}; looks in the annotatiosn for instructions
     * for it.
     * 
     * @param annotations
     * @return The Source, or null, if no info was found. If invalid
     *         instructions where found, they were logged and null is returned.
     */
    private Transformer getTransformer(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(XsltSource.class))
                return getTransformer((XsltSource) annotation);
        }
        return null;
    }

    /**
     * Loads the given YsltSource and return it's content as byte[]. Returns
     * null, if it could not be read.
     * 
     * @param annotation
     */
    private Transformer getTransformer(XsltSource xsltSource) {
        Transformer transformer = transformerCache.get(xsltSource);
        if (transformer == null) {
            try {
                Source source = loadSource(xsltSource);
                if (source == null)
                    return null;
                transformer = transformerFactory.newTransformer(source);
                transformerCache.put(xsltSource, transformer);
            } catch (TransformerConfigurationException e) {
                String message = "Could not create transformer for "
                        + xsltSource;
                logger.log(Level.WARNING, message, e);
                return null;
            }
        }
        return transformer;
    }

    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return Source.class.isAssignableFrom(type);
    }

    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        if (!Source.class.isAssignableFrom(type))
            return false;
        Transformer transformer = getTransformer(annotations);
        return transformer != null;
    }

    /**
     * Reads the given YsltSource and return it's content as byte[]. Returns
     * null, if it could not be read.
     * 
     * @param xsltSource
     */
    private Source loadSource(XsltSource xsltSource) {
        String sourceName = xsltSource.value();
        try {
            URL url = new URL(sourceName);
            InputStream inputStream = url.openStream();
            return new StreamSource(inputStream);
        } catch (MalformedURLException e) {
            // ignore; not treat as URL
        } catch (IOException e) {
            logger.log(Level.WARNING, "The URL " + sourceName
                    + " could not be read", e);
            return null;
        }
        try {
            File file = new File(sourceName);
            if (!file.exists()) {
                logger.warning("The file " + sourceName + " does not exist");
            } else if (!file.canRead()) {
                logger.warning("The file " + sourceName + " is not readeable");
            } else {
                FileInputStream inputStream = new FileInputStream(file);
                return new StreamSource(inputStream);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "The file " + sourceName
                    + " could not be read", e);
            return null;
        }
        String warning = "The @XsltSource "
                + sourceName
                + " could not be read, because it is neither a file name nor an URL";
        logger.log(Level.WARNING, warning);
        return null;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.provider.AbstractProvider#readFrom(java.lang.Class,
     *      Type, javax.ws.rs.core.MediaType, Annotation[],
     *      javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
     */
    public Source readFrom(Class<Source> type, Type genericType,
            MediaType mediaType, Annotation[] annotations,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return new StreamSource(entityStream);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object,
     *      java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType,
     *      javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    public void writeTo(Source source, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        StreamResult streamResult = new StreamResult(entityStream);
        Transformer transformer = getTransformer(annotations);
        try {
            transformer.transform(source, streamResult);
        } catch (Exception e) {
            IOException ioException = new IOException(
                    "Could not transform the javax.xml.transform.Source");
            ioException.setStackTrace(e.getStackTrace());
            throw ioException;
        } catch (TransformerFactoryConfigurationError e) {
            IOException ioException = new IOException(
                    "Could not transform the javax.xml.transform.Source");
            ioException.setStackTrace(e.getStackTrace());
            throw ioException;
        }
    }
}