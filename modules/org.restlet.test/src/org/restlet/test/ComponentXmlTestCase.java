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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Response;

/**
 * Unit test case for the configuration of a component with an XML file.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class ComponentXmlTestCase extends TestCase {

    private final int port = 8182;

    private final int port2 = 8183;

    /**
     * Recursively delete a directory.
     * 
     * @param dir
     *            The directory to delete.
     */
    private void deleteDir(File dir) {
        if (dir.exists()) {
            final File[] entries = dir.listFiles();

            for (final File entrie : entries) {
                if (entrie.isDirectory()) {
                    deleteDir(entrie);
                }

                entrie.delete();
            }
        }

        dir.delete();
    }

    public void testComponentXMLConfig() throws Exception {

        final File testDir = new File(System.getProperty("java.io.tmpdir"),
                this.getClass().getName());
        deleteDir(testDir);
        testDir.mkdir();
        final File file = new File(testDir, "component.xml");
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.append("<?xml version=\"1.0\"?>");
        writer.append("<component>");
        writer
                .append("<server protocol=\"HTTP\" port=\"" + this.port
                        + "\" />");
        writer.append("<server protocol=\"HTTP\" port=\"" + this.port2
                + "\" />");
        writer.append("<defaultHost hostPort=\"" + this.port2 + "\">");
        writer
                .append("<attach uriPattern=\"/abcd\" targetClass=\"org.restlet.test.HelloWorldApplication\" /> ");
        writer.append("</defaultHost>");
        writer.append("<host hostPort=\"" + this.port + "\">");
        writer
                .append("<attach uriPattern=\"/efgh\" targetClass=\"org.restlet.test.HelloWorldApplication\" /> ");
        writer.append("</host>");

        writer.append("</component>");

        writer.flush();
        writer.close();

        final Component component = new Component(LocalReference
                .createFileReference(file.getCanonicalPath()));
        component.start();

        final Client client = new Client(Protocol.HTTP);

        Response response = client.get("http://localhost:" + this.port
                + "/efgh");
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.get("http://localhost:" + this.port + "/abcd");
        assertTrue(response.getStatus().isClientError());

        response = client.get("http://localhost:" + this.port2 + "/abcd");
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.get("http://localhost:" + this.port2 + "/efgh");
        assertTrue(response.getStatus().isClientError());

        component.stop();
        deleteDir(testDir);
    }

}
