package org.restlet.ext.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

// TODO JSR311: where get the XSLT to convert the javax.xml.transform.Source to
// an OutputStream?

/**
 * 
 * @author Stephan Koops
 */
public class XmlTransformSourceProvider extends AbstractProvider<Source> {

    @Override
    public long getSize(Source object) {
        return -1;
    }

    @Override
    protected boolean isReadableAndWriteable(Class<?> type, Type genericType, Annotation[] annotations) {
        // return Source.class.isAssignableFrom(type);
        return false;
    }

    /**
     * @see org.restlet.ext.jaxrs.provider.AbstractProvider#readFrom(java.lang.Class,
     *      Type, javax.ws.rs.core.MediaType,
     *      Annotation[], javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
     */
    @Override
    public Source readFrom(Class<Source> type, Type genericType,
            MediaType mediaType, Annotation[] annotations, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return new StreamSource(entityStream);
    }

    @Override
    public void writeTo(Source source, Type genericType,
            Annotation[] annotations,
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        // StreamResult streamResult = new StreamResult(entityStream);
        // TransformerFactory transformerFactory = TransformerFactory
        //         .newInstance();
        // Source xsltSource = null;
        // Transformer trans = transformerFactory.newTransformer(xsltSource);
        // trans.transform(source, streamResult);
        throw new NotYetImplementedException();
    }
}