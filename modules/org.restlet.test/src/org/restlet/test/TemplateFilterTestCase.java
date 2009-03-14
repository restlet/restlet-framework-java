/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import java.io.File;
import java.io.FileWriter;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Response;

/**
 * Test case for template filters.
 * 
 * @author Thierry Boileau
 */
public class TemplateFilterTestCase extends RestletTestCase {

    /**
     * Internal class used for test purpose
     * 
     * @author Thierry Boileau
     */
    private static class MyFreemakerApplication extends Application {
        File testDirectory;

        Directory directory;

        /**
         * Constructor.
         * 
         * @param testDirectory
         *            The test directory.
         */
        public MyFreemakerApplication(File testDirectory) {
            setTestDirectory(testDirectory);
        }

        @Override
        public Restlet createRoot() {
            // Create a Directory that manages a local directory
            this.directory = new Directory(getContext(), LocalReference
                    .createFileReference(getTestDirectory()));
            this.directory.setNegotiateContent(true);
            return new org.restlet.ext.freemarker.TemplateFilter(getContext(),
                    this.directory);
        }

        public Directory getDirectory() {
            return this.directory;
        }

        public File getTestDirectory() {
            return this.testDirectory;
        }

        public void setDirectory(Directory directory) {
            this.directory = directory;
        }

        public void setTestDirectory(File testDirectory) {
            this.testDirectory = testDirectory;
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

        /**
         * Constructor.
         */
        public MyVelocityApplication(File testDirectory) {
            setTestDirectory(testDirectory);
        }

        @Override
        public Restlet createRoot() {
            // Create a Directory that manages a local directory
            this.directory = new Directory(getContext(), LocalReference
                    .createFileReference(getTestDirectory()));
            this.directory.setNegotiateContent(true);
            return new org.restlet.ext.velocity.TemplateFilter(getContext(),
                    this.directory);
        }

        public Directory getDirectory() {
            return this.directory;
        }

        public File getTestDirectory() {
            return this.testDirectory;
        }

        public void setDirectory(Directory directory) {
            this.directory = directory;
        }

        public void setTestDirectory(File testDirectory) {
            this.testDirectory = testDirectory;
        }
    }

    File testDir;

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

    public void testTemplateFilter() {
        try {
            // Create a temporary directory for the tests
            this.testDir = new File(System.getProperty("java.io.tmpdir"),
                    "TemplateFilterTestCase");
            deleteDir(this.testDir);
            this.testDir.mkdir();

            // Create temporary template files
            // Will be templated
            final File testFileFm1 = new File(this.testDir, "testFm1.txt.fmt");
            FileWriter fw = new FileWriter(testFileFm1);
            fw.write("Method=${m}/Authority=${ra}");
            fw.close();

            // Will not be templated
            final File testFileFm2 = new File(this.testDir, "testFm2.txt");
            fw = new FileWriter(testFileFm2);
            fw.write("Method=${m}/Authority=${ra}");
            fw.close();

            // Will be templated
            final File testFileVl1 = new File(this.testDir, "testVl1.txt.vm");
            fw = new FileWriter(testFileVl1);
            fw.write("Method=${m}/Path=${rp}");
            fw.close();

            // Will not be templated
            final File testFileVl2 = new File(this.testDir, "testVl2.txt");
            fw = new FileWriter(testFileVl2);
            fw.write("Method=${m}/Path=${rp}");
            fw.close();

            // Create a new component
            final Component component = new Component();
            component.getServers().add(Protocol.HTTP, TEST_PORT);
            component.getClients().add(Protocol.FILE);

            // Create an application filtered with Freemarker
            final MyFreemakerApplication freemarkerApplication = new MyFreemakerApplication(
                    this.testDir);
            // Create an application filtered with Velocity
            final MyVelocityApplication velocityApplication = new MyVelocityApplication(
                    this.testDir);

            // Attach the applications to the component and start it
            component.getDefaultHost().attach("/freemarker",
                    freemarkerApplication);
            component.getDefaultHost().attach("/velocity", velocityApplication);

            // Now, let's start the component!
            component.start();

            // Allow extensions tunneling
            freemarkerApplication.getTunnelService().setExtensionsTunnel(true);
            velocityApplication.getTunnelService().setExtensionsTunnel(true);
            final Client client = new Client(Protocol.HTTP);
            Response response = client.get("http://localhost:" + TEST_PORT
                    + "/freemarker/" + testFileFm1.getName());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(),
                        "Method=GET/Authority=localhost:" + TEST_PORT);
            }
            response = client.get("http://localhost:" + TEST_PORT
                    + "/freemarker/" + testFileFm2.getName());
            assertTrue(response.getStatus().isSuccess());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(),
                        "Method=${m}/Authority=${ra}");
            }

            response = client.get("http://localhost:" + TEST_PORT
                    + "/velocity/" + testFileVl1.getName());
            if (response.isEntityAvailable()) {
                assertEquals(response.getEntity().getText(),
                        "Method=GET/Path=/velocity/testVl1");
            }
            response = client.get("http://localhost:" + TEST_PORT
                    + "/velocity/" + testFileVl2.getName());
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
}
