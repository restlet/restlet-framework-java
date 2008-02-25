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
package org.restlet.ext.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * @author Stephan Koops
 * 
 */
@Provider
public class DataSourceProvider extends AbstractProvider<DataSource> {

    @Override
    public long getSize(DataSource object) {
        return -1;
    }

    @Override
    protected boolean isReadableAndWriteable(Class<?> type, Type genericType, Annotation[] annotations) {
        return DataSource.class.isAssignableFrom(type);
    }

    @Override
    public DataSource readFrom(Class<DataSource> type, Type genericType,
            MediaType mediaType, Annotation[] annotations, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return new ByteArrayDataSource(entityStream, mediaType.toString());
    }

    @Override
    public void writeTo(DataSource dataSource, Type genericType,
            Annotation[] annotations,
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        InputStream inputStream = dataSource.getInputStream();
        super.copyAndCloseStream(inputStream, entityStream);
    }
}