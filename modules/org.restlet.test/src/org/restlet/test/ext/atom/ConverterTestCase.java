package org.restlet.test.ext.atom;

import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.atom.Feed;
import org.restlet.representation.Representation;
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
        Client cc = new Client(Protocol.CLAP);
        Representation representation = cc.get(
                "clap://class/org/restlet/test/ext/atom/entry.xml").getEntity();

        Object object = cs.toObject(representation);
        assertTrue(object instanceof Feed);
    }
}
