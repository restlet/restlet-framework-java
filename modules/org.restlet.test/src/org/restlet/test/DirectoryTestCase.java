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
import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * Unit tests for the DirectoryHandler class.
 * 
 * @author Thierry Boileau
 */
public class DirectoryTestCase extends TestCase {
    String webSiteURL = "http://myapplication/";

    String baseFileUrl = webSiteURL.concat("fichier.txt");

    String baseFileUrlEn = webSiteURL.concat("fichier.txt.en");

    String baseFileUrlFr = webSiteURL.concat("fichier.txt.fr");

    String baseFileUrlFrBis = webSiteURL.concat("fichier.fr.txt");

    String percentEncodedFileUrl = webSiteURL.concat(Reference
            .encode("a new file.txt"));

    String percentEncodedFileUrlBis = webSiteURL.concat("a+new%20file.txt");

    File testDir;

    public void testDirectory() throws IOException {
        try {
            // Create a temporary directory for the tests
            testDir = new File(System.getProperty("java.io.tmpdir"),
                    "DirectoryTestCase");
            deleteDir(testDir);
            testDir.mkdir();

            // Create a new Restlet component
            Component clientComponent = new Component();
            clientComponent.getClients().add(Protocol.FILE);

            // Create an application
            MyApplication application = new MyApplication(clientComponent
                    .getContext(), testDir);
            // Attach the application to the component and start it
            clientComponent.getDefaultHost().attach("", application);

            // Now, let's start the component!
            clientComponent.start();

            // Test the directory Restlet with an index name
            testDirectory(application, application.getDirectory(), "index");
            // Test the directory Restlet with no index name
            testDirectory(application, application.getDirectory(), "");

            // Now, let's stop the component!
            clientComponent.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper
     * 
     * @param application
     * @param directory
     * @param indexName
     * @throws IOException
     */
    private void testDirectory(MyApplication application, Directory directory,
            String indexName) throws IOException {
        // Create a temporary file for the tests (the tests directory is not
        // empty)
        File testFile = File.createTempFile("test", ".txt", testDir);
        // Create a temporary directory
        File testDirectory = new File(testDir, "try");
        testDirectory.mkdir();

        String testFileUrl = webSiteURL.concat(testFile.getName());
        String testDirectoryUrl = webSiteURL.concat(testDirectory.getName());

        directory.setIndexName(indexName);
        // Test 1a : directory does not allow to GET its content
        directory.setListingAllowed(false);
        Response response = handle(application, webSiteURL, webSiteURL,
                Method.GET, null, "1a");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 1b : directory allows to GET its content
        directory.setListingAllowed(true);
        response = handle(application, webSiteURL, webSiteURL, Method.GET,
                null, "1b");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            // should list all files in the directory (at least the temporary
            // file generated before)
            response.getEntity().write(System.out);
        }

        // Test 2a : tests the HEAD method
        response = handle(application, webSiteURL, testFileUrl, Method.HEAD,
                null, "2a");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // Test 2b : try to GET a file that does not exist
        response = handle(application, webSiteURL, webSiteURL + "123456.txt",
                Method.GET, null, "2b");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 3a : try to put a new representation, but the directory is read
        // only
        directory.setModifiable(false);
        response = handle(application, webSiteURL, baseFileUrl, Method.PUT,
                new StringRepresentation("this is test 3a"), "3a");
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response
                .getStatus());

        // Test 3b : try to put a new representation, the directory is no more
        // read only
        directory.setModifiable(true);
        response = handle(application, webSiteURL, baseFileUrl, Method.PUT,
                new StringRepresentation("this is test 3b"), "3b");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // Test 4 : Try to get the representation of the new file
        response = handle(application, webSiteURL, baseFileUrl, Method.GET,
                null, "4");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            response.getEntity().write(System.out);
            System.out.println("");
        }

        // Test 5 : add a new representation of the same base file
        response = handle(application, webSiteURL, baseFileUrlEn, Method.PUT,
                new StringRepresentation("this is a test - En"), "5a");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrl, Method.HEAD,
                null, "5b");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlEn, Method.HEAD,
                null, "5c");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // Test 6a : delete a file
        response = handle(application, webSiteURL, testFileUrl, Method.DELETE,
                null, "6a-1");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        response = handle(application, webSiteURL, testFileUrl, Method.HEAD,
                null, "6a-2");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 6b : delete a file that does not exist
        response = handle(application, webSiteURL, testFileUrl, Method.DELETE,
                null, "6b");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 6c : delete a directory (without and with trailing slash)
        // Distinct behaviours if an index has been defined or not
        if (indexName.length() == 0) {
            response = handle(application, webSiteURL, testDirectoryUrl,
                    Method.DELETE, null, "6c-1");
            assertEquals(Status.REDIRECTION_SEE_OTHER, response.getStatus());
            System.out.println(response.getLocationRef());
            response = handle(application, response.getLocationRef().getPath(),
                    response.getLocationRef().getPath(), Method.DELETE, null,
                    "6c-2");
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
            response = handle(application, webSiteURL, webSiteURL,
                    Method.DELETE, null, "6c-3");
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
        } else {
            // As there is no index file in the directory, the response must
            // return the status Status.CLIENT_ERROR_NOT_FOUND
            response = handle(application, webSiteURL, testDirectoryUrl + "/",
                    Method.DELETE, null, "6c-2");
            assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
            response = handle(application, webSiteURL, webSiteURL,
                    Method.DELETE, null, "6c-3");
            assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        }

        // Test 7a : put one representation of the base file (in french
        // language)
        response = handle(application, webSiteURL, baseFileUrlFr, Method.PUT,
                new StringRepresentation("message de test"), "7a");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // Test 7b : put another representation of the base file (in french
        // language) but the extensions are mixed
        // and there is no content negotiation
        directory.setNegotiateContent(false);
        response = handle(application, webSiteURL, baseFileUrlFrBis,
                Method.PUT, new StringRepresentation("message de test"), "7b-1");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        // The 2 resources in french must be present (the same actually)
        response = handle(application, webSiteURL, baseFileUrlFr, Method.HEAD,
                null, "7b-2");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFrBis,
                Method.HEAD, null, "7b-3");
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

        // Test 7c : delete the file representation of the resources with no
        // content negotiation
        // The 2 french resources are deleted (there were only one)
        response = handle(application, webSiteURL, baseFileUrlFr,
                Method.DELETE, null, "7c-1");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFr, Method.HEAD,
                null, "7c-2");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFrBis,
                Method.HEAD, null, "7c-3");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFrBis,
                Method.DELETE, null, "7c-4");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 7d : put another representation of the base file (in french
        // language) but the extensions are mixed
        // and there is content negotiation
        directory.setNegotiateContent(true);
        response = handle(application, webSiteURL, baseFileUrlFr, Method.PUT,
                new StringRepresentation("message de test"), "7d-1");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFrBis,
                Method.PUT, new StringRepresentation("message de test Bis"),
                "7d-2");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        // only one resource in french must be present
        response = handle(application, webSiteURL, baseFileUrlFr, Method.HEAD,
                null, "7d-3");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFrBis,
                Method.HEAD, null, "7d-4");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // TBOI : not sure this test is correct
        // Check if only one resource has been created
        directory.setNegotiateContent(false);
        response = handle(application, webSiteURL, baseFileUrlFr, Method.HEAD,
                null, "7d-5");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // Test 7e : delete the file representation of the resources with
        // content negotiation
        directory.setNegotiateContent(true);
        response = handle(application, webSiteURL, baseFileUrlFr,
                Method.DELETE, null, "7e-1");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFr, Method.HEAD,
                null, "7e-2");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlFrBis,
                Method.HEAD, null, "7e-8");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 8 : must delete the english representation
        response = handle(application, webSiteURL, baseFileUrl, Method.DELETE,
                null, "8a");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        response = handle(application, webSiteURL, baseFileUrlEn,
                Method.DELETE, null, "8b");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        // Test 9a : put a new representation, the resource's URI contains
        // percent-encoded characters
        directory.setModifiable(true);
        response = handle(application, webSiteURL, percentEncodedFileUrl,
                Method.PUT, new StringRepresentation("this is test 9a"), "9a");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // Test 9b : Try to get the representation of the new file
        response = handle(application, webSiteURL, percentEncodedFileUrl,
                Method.GET, null, "9b");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            response.getEntity().write(System.out);
            System.out.println("");
        }
        // Test 9c : Try to get the representation of the new file with an
        // equivalent URI
        response = handle(application, webSiteURL, percentEncodedFileUrlBis,
                Method.GET, null, "9c");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            response.getEntity().write(System.out);
            System.out.println("");
        }
        // Test 9d : Try to delete the file
        response = handle(application, webSiteURL, percentEncodedFileUrl,
                Method.DELETE, null, "9d");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        testDirectory.delete();
        System.out.println("End of tests*********************");
    }

    /**
     * Helper for the test
     * 
     * @param directory
     * @param baseRef
     * @param resourceRef
     * @param method
     * @param entity
     * @return
     */
    private Response handle(Application application, String baseRef,
            String resourceRef, Method method, Representation entity,
            String testCode) {
        Request request = new Request();
        Response response = new Response(request);
        request.setResourceRef(resourceRef);
        request.getResourceRef().setBaseRef(baseRef);
        request.setMethod(method);
        if (Method.PUT.equals(method)) {
            request.setEntity(entity);
        }
        application.handle(request, response);
        System.out.println("[test, status]=[" + testCode + ", "
                + response.getStatus() + "]");
        return response;
    }

    public static void main(String[] args) {
        try {
            new DirectoryTestCase().testDirectory();
        } catch (IOException e) {
            System.out.println("Exception = " + e.getMessage());
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
        public MyApplication(Context context, File testDirectory)
                throws IOException {
            super(context);
            this.setTestDirectory(testDirectory);
            // Create a DirectoryHandler that manages a local Directory
            this.directory = new Directory(getContext(), LocalReference
                    .createFileReference(getTestDirectory()));
            this.directory.setNegotiateContent(true);
        }

        @Override
        public Restlet createRoot() {
            return directory;
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
