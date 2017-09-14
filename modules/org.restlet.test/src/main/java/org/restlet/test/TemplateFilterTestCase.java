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

package org.restlet.test;

import java.io.File;
import java.io.FileWriter;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.io.IoUtils;
import org.restlet.resource.Directory;

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
        Directory directory;

        File testDirectory;

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
        public Restlet createInboundRoot() {
            // Create a Directory that manages a local directory
            this.directory = new Directory(getContext(),
                    LocalReference.createFileReference(getTestDirectory()));
            this.directory.setNegotiatingContent(true);
            return new org.restlet.ext.freemarker.TemplateFilter(getContext(),
                    this.directory);
        }

        public File getTestDirectory() {
            return this.testDirectory;
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
        Directory directory;

        File testDirectory;

        /**
         * Constructor.
         */
        public MyVelocityApplication(File testDirectory) {
            setTestDirectory(testDirectory);
        }

        @Override
        public Restlet createInboundRoot() {
            // Create a Directory that manages a local directory
            this.directory = new Directory(getContext(),
                    LocalReference.createFileReference(getTestDirectory()));
            this.directory.setNegotiatingContent(true);
            return new org.restlet.ext.velocity.TemplateFilter(getContext(),
                    this.directory);
        }

        public File getTestDirectory() {
            return this.testDirectory;
        }

        public void setTestDirectory(File testDirectory) {
            this.testDirectory = testDirectory;
        }
    }

    File testDir;

    public void testTemplateFilter() {
        try {
            // Create a temporary directory for the tests
            this.testDir = new File(System.getProperty("java.io.tmpdir"),
                    "TemplateFilterTestCase");
            IoUtils.delete(this.testDir, true);
            this.testDir.mkdir();

            // Create temporary template files
            // Will be templated
            File testFileFm1 = new File(this.testDir, "testFm1.txt.fmt");
            FileWriter fw = new FileWriter(testFileFm1);
            fw.write("Method=${m}/Authority=${ra}");
            fw.close();

            // Will not be templated
            File testFileFm2 = new File(this.testDir, "testFm2.txt");
            fw = new FileWriter(testFileFm2);
            fw.write("Method=${m}/Authority=${ra}");
            fw.close();

            // Will be templated
            File testFileVl1 = new File(this.testDir, "testVl1.txt.vm");
            fw = new FileWriter(testFileVl1);
            fw.write("Method=${m}/Path=${rp}");
            fw.close();

            // Will not be templated
            File testFileVl2 = new File(this.testDir, "testVl2.txt");
            fw = new FileWriter(testFileVl2);
            fw.write("Method=${m}/Path=${rp}");
            fw.close();

            // Create a new component
            Component component = new Component();
            component.getServers().add(Protocol.HTTP, TEST_PORT);
            component.getClients().add(Protocol.FILE);

            // Create an application filtered with FreeMarker
            MyFreemakerApplication freemarkerApplication = new MyFreemakerApplication(
                    this.testDir);
            // Create an application filtered with Velocity
            MyVelocityApplication velocityApplication = new MyVelocityApplication(
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
            Client client = new Client(Protocol.HTTP);
            Response response = client.handle(new Request(Method.GET,
                    "http://localhost:" + TEST_PORT + "/freemarker/"
                            + testFileFm1.getName()));

            if (response.isEntityAvailable()) {
                assertEquals("Method=GET/Authority=localhost:" + TEST_PORT,
                        response.getEntity().getText());
            }

            response = client.handle(new Request(Method.GET,
                    "http://localhost:" + TEST_PORT + "/freemarker/"
                            + testFileFm2.getName()));
            assertTrue(response.getStatus().isSuccess());

            if (response.isEntityAvailable()) {
                assertEquals("Method=${m}/Authority=${ra}", response
                        .getEntity().getText());
            }

            response = client.handle(new Request(Method.GET,
                    "http://localhost:" + TEST_PORT + "/velocity/"
                            + testFileVl1.getName()));

            if (response.isEntityAvailable()) {
                assertEquals("Method=GET/Path=/velocity/testVl1", response
                        .getEntity().getText());
            }

            response = client.handle(new Request(Method.GET,
                    "http://localhost:" + TEST_PORT + "/velocity/"
                            + testFileVl2.getName()));
            assertTrue(response.getStatus().isSuccess());

            if (response.isEntityAvailable()) {
                assertEquals("Method=${m}/Path=${rp}", response.getEntity()
                        .getText());
            }

            // Now, let's stop the component!
            component.stop();
            client.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
