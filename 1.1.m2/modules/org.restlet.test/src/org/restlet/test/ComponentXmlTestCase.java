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

    private int port = 8182;

    private int port2 = 8183;

    public void testComponentXMLConfig() throws Exception {

        File testDir = new File(System.getProperty("java.io.tmpdir"), this
                .getClass().getName());
        deleteDir(testDir);
        testDir.mkdir();
        File file = new File(testDir, "component.xml");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.append("<?xml version=\"1.0\"?>");
        writer.append("<component>");
        writer.append("<server protocol=\"HTTP\" port=\"" + port + "\" />");
        writer.append("<server protocol=\"HTTP\" port=\"" + port2 + "\" />");
        writer.append("<defaultHost hostPort=\"" + port2 + "\">");
        writer
                .append("<attach uriPattern=\"/abcd\" targetClass=\"org.restlet.test.HelloWorldApplication\" /> ");
        writer.append("</defaultHost>");
        writer.append("<host hostPort=\"" + port + "\">");
        writer
                .append("<attach uriPattern=\"/efgh\" targetClass=\"org.restlet.test.HelloWorldApplication\" /> ");
        writer.append("</host>");

        writer.append("</component>");

        writer.flush();
        writer.close();

        Component component = new Component(LocalReference
                .createFileReference(file.getCanonicalPath()));
        component.start();

        Client client = new Client(Protocol.HTTP);
        
        Response response = client.get("http://localhost:" + port + "/efgh");
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.get("http://localhost:" + port + "/abcd");
        assertTrue(response.getStatus().isClientError());

        response = client.get("http://localhost:" + port2 + "/abcd");
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.isEntityAvailable());
        response = client.get("http://localhost:" + port2 + "/efgh");
        assertTrue(response.getStatus().isClientError());

        component.stop();
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
