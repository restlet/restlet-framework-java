package org.restlet.ext.jaxrs.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * Provider based on the Simple XML framework in order to convert Java beans to XML
 * and vice versa.
 * 
 * @author Kiran Rao
 * @see <a href="http://simple.sourceforge.net/">Simple XML</a>
 */

@Provider
@Produces({ "application/xml", MediaType.TEXT_XML, "application/*+xml" })
@Consumes({ "application/xml", MediaType.TEXT_XML, "application/*+xml" })
public class SimpleXmlProvider extends AbstractProvider<Object> {

    private final Serializer serializer = new Persister();

    @Override
    public long getSize(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(Root.class);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(Root.class);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpResponseHeaders,
            InputStream entityStream) throws IOException {
        try {
            return serializer.read(type, entityStream);
        } catch (Exception e) {
            throw new IOException("Could not parse as " + type.getName(), e);
        }
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {

        try {
            serializer.write(object, entityStream);
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

}
