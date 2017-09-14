/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.atom;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.ext.atom.Feed;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.service.ConverterService;
import org.restlet.test.RestletTestCase;

public class ConverterTestCase extends RestletTestCase {

    public void testObjectToRepresentation() throws IOException {
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
