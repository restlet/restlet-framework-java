package org.restlet.test.ext.atom;

import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.atom.Feed;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.service.ConverterService;

public class ConverterTestCase extends TestCase {

    public void testObjectToRepresentation() {
        ConverterService cs = new ConverterService();
        Feed feed = new Feed();
        Representation representation = cs.toRepresentation(feed);
        assertEquals(MediaType.APPLICATION_ATOM, representation.getMediaType());
    }

    public void testRepresentationToObject() throws IOException {
        ConverterService cs = new ConverterService();
        ClientResource cr = new ClientResource(
                "clap://class/org/restlet/test/ext/atom/entry.xml");
        Representation representation = cr.get();
        Object object = cs.toObject(representation);
        assertTrue(object instanceof Feed);
    }
}
