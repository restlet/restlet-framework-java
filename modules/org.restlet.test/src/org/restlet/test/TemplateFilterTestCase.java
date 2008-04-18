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

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.ext.freemarker.TemplateFilter;

import freemarker.template.Configuration;

/**
 * Test case for template filters.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class TemplateFilterTestCase extends TestCase {

    File testDir;

    public void testTemplateFilter() {
        try {
            // Create a temporary directory for the tests
            testDir = new File(System.getProperty("java.io.tmpdir"),
                    "TemplateFilterTestCase");
            deleteDir(testDir);
            testDir.mkdir();

            // Create temporary template files
            File testFileFm1 = new File(testDir, "testFm1.txt.fmt");
            FileWriter fw = new FileWriter(testFileFm1);
            fw.write("Value=${value}");
            fw.close();

            File testFileFm2 = new File(testDir, "testFm2.txt");
            fw = new FileWriter(testFileFm2);
            fw.write("Value=${value}");
            fw.close();

            // Create a new component
            Component component = new Component();
            component.getServers().add(Protocol.HTTP, 8182);
            component.getClients().add(Protocol.FILE);

            // Create an application
            MyApplication application = new MyApplication(component
                    .getContext(), testDir);
            // Attach the application to the component and start it
            component.getDefaultHost().attachDefault(application);

            // Now, let's start the component!
            component.start();

            // Allow extensions tunneling
            application.getTunnelService().setExtensionsTunnel(true);
            Client client = new Client(Protocol.HTTP);
            Response response = client.get("http://localhost:8182/"
                    + testFileFm1.getName());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(), "Value=myValue");
            }
            response = client.get("http://localhost:8182/"
                    + testFileFm2.getName());
            assertTrue(response.getStatus().isSuccess());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(), "Value=${value}");
            }

            // Now, let's stop the component!
            component.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Internal class used for test purpose
     * 
     * @author Thierry Boileau
     */
    private static class MyApplication extends Application {
        File testDirectory;

        Directory directory;

        public File getTestDirectory() {
            return testDirectory;
        }

        public void setTestDirectory(File testDirectory) {
            this.testDirectory = testDirectory;
        }

        /**
         * Constructor.
         */
        public MyApplication() {
            super();
        }

        /**
         * Constructor.
         * 
         * @param context
         *                The parent context.
         */
        public MyApplication(Context context, File testDirectory) {
            super(context);

            this.setTestDirectory(testDirectory);
            // Create a Directory that manages a local directory
            this.directory = new Directory(getContext(), LocalReference
                    .createFileReference(getTestDirectory()));
            this.directory.setNegotiateContent(true);
        }

        @Override
        public Restlet createRoot() {
            Configuration fmc = new Configuration();
            Map<String, Object> map = new TreeMap<String, Object>();
            map.put("value", "myValue");

            TemplateFilter filter = new TemplateFilter(getContext(), directory,
                    fmc, map);
            return filter;
        }

        public Directory getDirectory() {
            return directory;
        }

        public void setDirectory(Directory directory) {
            this.directory = directory;
        }
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
