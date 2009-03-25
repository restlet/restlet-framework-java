package org.restlet.test.ext.atom;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.atom.Feed;
import org.restlet.representation.Representation;
import org.restlet.service.ConverterService;

public class ConverterTestCase extends TestCase {

    public void testAtom() {

        ConverterService cs = new ConverterService();
        Feed feed = new Feed();
        Representation representation = cs.toRepresentation(feed);

        assertEquals(MediaType.APPLICATION_ATOM, representation.getMediaType());
    }

}
