/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Service;
import org.restlet.resource.FileRepresentation;

/**
 * Unit test case for the Atom extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AtomTest extends TestCase {

    public void testAtom() {
        // Create a temporary directory for the tests
        File testDir = new File(System.getProperty("java.io.tmpdir"),
                "AtomTestCase");
        deleteDir(testDir);
        testDir.mkdir();

        try {
            Service atomService = new Service(
                    "http://bitworking.org/projects/apptestsite/app.cgi/service/;service_document");
            Feed atomFeed = atomService.getWorkspaces().get(0).getCollections()
                    .get(0).getFeed();

            // Write the feed into a file.
            File feedFile = new File(testDir, "feed.xml");
            atomFeed.write(new BufferedOutputStream(new FileOutputStream(
                    feedFile)));

            // Get the service from the file
            FileRepresentation fileRepresentation = new FileRepresentation(
                    feedFile, MediaType.TEXT_XML);
            Feed atomFeed2 = new Feed(fileRepresentation);

            assertEquals(atomFeed2.getAuthors().get(0).getName(), atomFeed
                    .getAuthors().get(0).getName());
            assertEquals(atomFeed2.getEntries().get(0).getContent()
                    .getInlineContent().getText(), atomFeed2.getEntries()
                    .get(0).getContent().getInlineContent().getText());
            assertEquals(atomFeed2.getEntries().get(0).getTitle().getContent(),
                    atomFeed2.getEntries().get(0).getTitle().getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        deleteDir(testDir);
    }

    /**
     * Recursively delete a directory.
     * 
     * @param dir
     *                The directory to delete.
     */
    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] entries = dir.listFiles();

            for (int i = 0; i < entries.length; i++) {
                if (entries[i].isDirectory()) {
                    deleteDir(entries[i]);
                }

                entries[i].delete();
            }
        }

        dir.delete();
    }

}
