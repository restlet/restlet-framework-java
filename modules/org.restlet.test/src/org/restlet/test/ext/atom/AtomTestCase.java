/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.atom;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.engine.io.BioUtils;
import org.restlet.ext.atom.Categories;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Service;
import org.restlet.representation.FileRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the Atom extension.
 * 
 * @author Jerome Louvel
 */
public class AtomTestCase extends RestletTestCase {

    public void testCategories() throws Exception {
        Categories atomCategories = new Categories(
                "clap://class/org/restlet/test/ext/atom/categories.xml");
        assertEquals(new Reference("http://example.com/cats/big3"),
                atomCategories.getScheme());
        assertEquals("animal", atomCategories.getEntries().get(0).getTerm());
    }

    public void testAtom() throws Exception {
        // Create a temporary directory for the tests
        final File testDir = new File(System.getProperty("java.io.tmpdir"),
                "AtomTestCase");
        BioUtils.delete(testDir, true);
        testDir.mkdir();

        final Service atomService = new Service(
                "clap://class/org/restlet/test/ext/atom/service.xml");
        assertEquals("AtomPub Test Site", atomService.getWorkspaces().get(0)
                .getTitle());
        assertEquals("entry", atomService.getWorkspaces().get(0)
                .getCollections().get(0).getTitle());

        final Feed atomFeed = atomService.getWorkspaces().get(0)
                .getCollections().get(0).getFeed();

        // Write the feed into a file.
        final File feedFile = new File(testDir, "feed.xml");
        atomFeed.write(new BufferedOutputStream(new FileOutputStream(feedFile)));

        // Get the service from the file
        final FileRepresentation fileRepresentation = new FileRepresentation(
                feedFile, MediaType.TEXT_XML);
        final Feed atomFeed2 = new Feed(fileRepresentation);

        assertEquals(atomFeed2.getAuthors().get(0).getName(), atomFeed
                .getAuthors().get(0).getName());
        assertEquals(atomFeed2.getEntries().get(0).getContent()
                .getInlineContent().getText(), atomFeed2.getEntries().get(0)
                .getContent().getInlineContent().getText());
        assertEquals(atomFeed2.getEntries().get(0).getTitle().getContent(),
                atomFeed2.getEntries().get(0).getTitle().getContent());

        BioUtils.delete(testDir, true);
    }

}
