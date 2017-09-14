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

package org.restlet.test.ext.rome;

import java.io.IOException;

import org.restlet.ext.rome.SyndFeedRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.test.RestletTestCase;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Unit test for the {@link SyndFeedRepresentation} class.
 * 
 * @author Jerome Louvel
 */
@Deprecated
public class SyndFeedRepresentationTestCase extends RestletTestCase {

    public void testParsing() throws IOException {
        ClientResource cr = new ClientResource(
                "clap://class/org/restlet/test/ext/rome/testRome.xml");
        Representation xmlRep = cr.get();
        SyndFeed feed = new SyndFeedRepresentation(xmlRep).getFeed();
        assertNotNull(feed);
    }

    public void testBomb() throws IOException {
        ClientResource cr = new ClientResource(
                "clap://class/org/restlet/test/ext/rome/testRomeBomb.xml");
        Representation xmlRep = cr.get();
        boolean error = false;
        try {
            new SyndFeedRepresentation(xmlRep).getFeed();
        } catch (Exception e) {
            error = true;
        }
        assertTrue(error);
    }
}
