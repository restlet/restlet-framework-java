package org.restlet.ext.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.Source;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * 
 * @author Stephan Koops
 */
public class XmlTransformSourceProvider extends AbstractProvider<Source> {

    // TODO wie geht man mit javax.xml.transform.Source um?
    
    @Override
    public long getSize(Source object) {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    protected boolean isReadableAndWriteable(Class<?> type) {
        // TODO XmlTransformSourceProvider.isReadableAndWriteable(Class)
        return false;
    }

    @Override
    public Source readFrom(Class<Source> type, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        throw new NotYetImplementedException();
    }

    @Override
    public void writeTo(Source source, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        throw new NotYetImplementedException();
    }
}