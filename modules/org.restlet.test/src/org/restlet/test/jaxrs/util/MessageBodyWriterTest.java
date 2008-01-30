package org.restlet.test.jaxrs.util;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.wrappers.MessageBodyWriter;

import junit.framework.TestCase;

public class MessageBodyWriterTest extends TestCase {

    public void testSupports() {
        assertSupport(MediaType.TEXT_ALL, MediaType.TEXT_PLAIN);
        assertSupport(MediaType.TEXT_PLAIN, MediaType.TEXT_ALL);
    }
    
    public void assertSupport(MediaType mbwMediaType, MediaType requMediaType)
    {
        assertTrue(MessageBodyWriter.supports(mbwMediaType, requMediaType));
    }

}
