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
            // Will be templated
            File testFileFm1 = new File(testDir, "testFm1.txt.fmt");
            FileWriter fw = new FileWriter(testFileFm1);
            fw.write("Method=${m}/Authority=${ra}");
            fw.close();

            // Will not be templated
            File testFileFm2 = new File(testDir, "testFm2.txt");
            fw = new FileWriter(testFileFm2);
            fw.write("Method=${m}/Authority=${ra}");
            fw.close();

            // Will be templated
            File testFileVl1 = new File(testDir, "testVl1.txt.vm");
            fw = new FileWriter(testFileVl1);
            fw.write("Method=${m}/Path=${rp}");
            fw.close();

            // Will not be templated
            File testFileVl2 = new File(testDir, "testVl2.txt");
            fw = new FileWriter(testFileVl2);
            fw.write("Method=${m}/Path=${rp}");
            fw.close();

            // Create a new component
            Component component = new Component();
            component.getServers().add(Protocol.HTTP, 8182);
            component.getClients().add(Protocol.FILE);

            // Create an application filtered with Freemarker
            MyFreemakerApplication freemarkerApplication = new MyFreemakerApplication(
                    component.getContext(), testDir);
            // Create an application filtered with Velocity
            MyVelocityApplication velocityApplication = new MyVelocityApplication(
                    component.getContext(), testDir);

            // Attach the applications to the component and start it
            component.getDefaultHost().attach("/freemarker",
                    freemarkerApplication);
            component.getDefaultHost().attach("/velocity", velocityApplication);

            // Now, let's start the component!
            component.start();

            // Allow extensions tunneling
            freemarkerApplication.getTunnelService().setExtensionsTunnel(true);
            velocityApplication.getTunnelService().setExtensionsTunnel(true);
            Client client = new Client(Protocol.HTTP);
            Response response = client.get("http://localhost:8182/freemarker/"
                    + testFileFm1.getName());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(),
                        "Method=GET/Authority=localhost:8182");
            }
            response = client.get("http://localhost:8182/freemarker/"
                    + testFileFm2.getName());
            assertTrue(response.getStatus().isSuccess());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(),
                        "Method=${m}/Authority=${ra}");
            }

            response = client.get("http://localhost:8182/velocity/"
                    + testFileVl1.getName());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(),
                        "Method=GET/Path=/velocity/testVl1");
            }
            response = client.get("http://localhost:8182/velocity/"
                    + testFileVl2.getName());
            assertTrue(response.getStatus().isSuccess());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(),
                        "Method=${m}/Path=${rp}");
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
    private static class MyFreemakerApplication extends Application {
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
         * 
         * @param context
         *                The parent context.
         */
        public MyFreemakerApplication(Context context, File testDirectory) {
            super(context);

            this.setTestDirectory(testDirectory);
            // Create a Directory that manages a local directory
            this.directory = new Directory(getContext(), LocalReference
                    .createFileReference(getTestDirectory()));
            this.directory.setNegotiateContent(true);
        }

        @Override
        public Restlet createRoot() {
            return new org.restlet.ext.freemarker.TemplateFilter(getContext(),
                    directory);
        }

        public Directory getDirectory() {
            return directory;
        }

        public void setDirectory(Directory directory) {
            this.directory = directory;
        }
    }

    /**
     * Internal class used for test purpose
     * 
     * @author Thierry Boileau
     */
    private static class MyVelocityApplication extends Application {
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
         * 
         * @param context
         *                The parent context.
         */
        public MyVelocityApplication(Context context, File testDirectory) {
            super(context);

            this.setTestDirectory(testDirectory);
            // Create a Directory that manages a local directory
            this.directory = new Directory(getContext(), LocalReference
                    .createFileReference(getTestDirectory()));
            this.directory.setNegotiateContent(true);
        }

        @Override
        public Restlet createRoot() {
            return new org.restlet.ext.velocity.TemplateFilter(getContext(),
                    directory);
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
