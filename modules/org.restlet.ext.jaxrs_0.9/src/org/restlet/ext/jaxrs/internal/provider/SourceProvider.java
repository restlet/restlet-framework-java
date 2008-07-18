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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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

/**
 * JAX-RS Provider to convert a {@link Source} to an object and vice versa.
 * 
 * @author Stephan Koops
 */
@Provider
public class SourceProvider extends AbstractProvider<Source> {

    private final Logger logger = Logger.getLogger(SourceProvider.class
            .getName());

    private final TransformerFactory transformerFactory = TransformerFactory
            .newInstance();

    @Override
    public long getSize(Source object) {
        return -1;
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, MediaType, Annotation[],
     *      MultivaluedMap, InputStream)
     */
    @Override
    public Source readFrom(Class<Source> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return new StreamSource(entityStream);
    }

    @Override
    protected Class<?> supportedClass() {
        return Source.class;
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(Source source, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        final StreamResult streamResult = new StreamResult(entityStream);
        Transformer transformer;
        try {
            transformer = this.transformerFactory.newTransformer();
        } catch (final TransformerConfigurationException e) {
            this.logger.log(Level.WARNING, "Could not create Transformer", e);
            final IOException ioException = new IOException(
                    "Could not create javax.xml.transform.Transformer");
            ioException.setStackTrace(e.getStackTrace());
            throw ioException;
        }
        try {
            transformer.transform(source, streamResult);
        } catch (final Exception e) {
            final IOException ioException = new IOException(
                    "Could not transform the javax.xml.transform.Source");
            ioException.setStackTrace(e.getStackTrace());
            throw ioException;
        } catch (final TransformerFactoryConfigurationError e) {
            final IOException ioException = new IOException(
                    "Could not transform the javax.xml.transform.Source");
            ioException.setStackTrace(e.getStackTrace());
            throw ioException;
        }
    }
}