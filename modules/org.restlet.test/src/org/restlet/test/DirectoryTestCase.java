/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Component;
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
 * Unit tests for the Directory class.
 * 
 * @author Thierry Boileau
 */
public class DirectoryTestCase extends TestCase {
    /**
     * Internal class used for test purpose
     * 
     * @author Thierry Boileau
     */
    private static class MyApplication extends Application {
        File testDirectory;

        Directory directory;

        /**
         * Constructor.
         * 
         * @param testDirectory
         *            The test directory.
         */
        public MyApplication(File testDirectory) {
            setTestDirectory(testDirectory);
        }

        @Override
        public Restlet createRoot() {
            // Create a DirectoryHandler that manages a local Directory
            this.directory = new Directory(getContext(), LocalReference
                    .createFileReference(getTestDirectory()));
            this.directory.setNegotiateContent(true);
            return this.directory;
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

    public static void main(String[] args) {
        new DirectoryTestCase().testDirectory();
    }

    String webSiteURL = "http://myapplication/";

    String baseFileUrl = this.webSiteURL.concat("fichier.txt");

    String baseFileUrlEn = this.webSiteURL.concat("fichier.txt.en");

    String baseFileUrlFr = this.webSiteURL.concat("fichier.txt.fr");

    String baseFileUrlFrBis = this.webSiteURL.concat("fichier.fr.txt");

    String percentEncodedFileUrl = this.webSiteURL.concat(Reference
            .encode("a new file.txt"));

    String percentEncodedFileUrlBis = this.webSiteURL
            .concat("a+new%20file.txt");

    /** Tests the creation of directory with unknown parent directories. */
    String testCreationDirectory = webSiteURL.concat("dir/does/not/exist");

    /** Tests the creation of file with unknown parent directories. */
    String testCreationFile = webSiteURL.concat("file/does/not/exist");

    /** Tests the creation of text file with unknown parent directories. */
    String testCreationTextFile = webSiteURL
            .concat("text/file/does/not/exist.txt");

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
            for (final File entry : entries) {
                if (entry.isDirectory()) {
                    deleteDir(entry);
                }
                entry.delete();
            }
        }
        dir.delete();
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
        final Request request = new Request();
        final Response response = new Response(request);
        request.setResourceRef(resourceRef);
        request.setOriginalRef(request.getResourceRef().getTargetRef());
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

    public void testDirectory() {
        try {
            // Create a temporary directory for the tests
            this.testDir = new File(System.getProperty("java.io.tmpdir"),
                    "DirectoryTestCase");

            // Create a new Restlet component
            final Component clientComponent = new Component();
            clientComponent.getClients().add(Protocol.FILE);

            // Create an application
            final MyApplication application = new MyApplication(this.testDir);
            // Attach the application to the component and start it
            clientComponent.getDefaultHost().attach("", application);

            // Now, let's start the component!
            clientComponent.start();

            // Allow extensions tunneling
            application.getTunnelService().setExtensionsTunnel(true);
            deleteDir(this.testDir);
            this.testDir.mkdir();

            // Test the directory Restlet with an index name
            testDirectory(application, application.getDirectory(), "index");
            deleteDir(this.testDir);
            this.testDir.mkdir();

            // Test the directory Restlet with no index name
            testDirectory(application, application.getDirectory(), "");

            // Avoid extensions tunneling
            application.getTunnelService().setExtensionsTunnel(false);
            deleteDir(this.testDir);
            this.testDir.mkdir();

            // Test the directory Restlet with an index name
            testDirectory(application, application.getDirectory(), "index");
            deleteDir(this.testDir);
            this.testDir.mkdir();

            // Test the directory Restlet with no index name
            testDirectory(application, application.getDirectory(), "");

            // Now, let's stop the component!
            clientComponent.stop();
        } catch (final Exception e) {
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
        final File testFile = File.createTempFile("test", ".txt", this.testDir);
        // Create a temporary directory
        final File testDirectory = new File(this.testDir, "try");
        testDirectory.mkdir();

        final String testFileUrl = this.webSiteURL.concat(testFile.getName());
        final String testDirectoryUrl = this.webSiteURL.concat(testDirectory
                .getName());

        directory.setIndexName(indexName);
        // Test 1a : directory does not allow to GET its content
        directory.setListingAllowed(false);
        Response response = handle(application, this.webSiteURL,
                this.webSiteURL, Method.GET, null, "1a");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 1b : directory allows to GET its content
        directory.setListingAllowed(true);
        response = handle(application, this.webSiteURL, this.webSiteURL,
                Method.GET, null, "1b");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            // should list all files in the directory (at least the temporary
            // file generated before)
            response.getEntity().write(System.out);
        }

        // Test 2a : tests the HEAD method
        response = handle(application, this.webSiteURL, testFileUrl,
                Method.HEAD, null, "2a");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // Test 2b : try to GET a file that does not exist
        response = handle(application, this.webSiteURL, this.webSiteURL
                + "123456.txt", Method.GET, null, "2b");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 3a : try to put a new representation, but the directory is read
        // only
        directory.setModifiable(false);
        response = handle(application, this.webSiteURL, this.baseFileUrl,
                Method.PUT, new StringRepresentation("this is test 3a"), "3a");
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response
                .getStatus());

        // Test 3b : try to put a new representation, the directory is no more
        // read only
        directory.setModifiable(true);
        response = handle(application, this.webSiteURL, this.baseFileUrl,
                Method.PUT, new StringRepresentation("this is test 3b"), "3b");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // Test 4 : Try to get the representation of the new file
        response = handle(application, this.webSiteURL, this.baseFileUrl,
                Method.GET, null, "4");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            response.getEntity().write(System.out);
            System.out.println("");
        }

        // Test 5 : add a new representation of the same base file
        response = handle(application, this.webSiteURL, this.baseFileUrlEn,
                Method.PUT, new StringRepresentation("this is a test - En"),
                "5a");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());
        response = handle(application, this.webSiteURL, this.baseFileUrl,
                Method.HEAD, null, "5b");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response = handle(application, this.webSiteURL, this.baseFileUrlEn,
                Method.HEAD, null, "5c");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // Test 6a : delete a file
        response = handle(application, this.webSiteURL, testFileUrl,
                Method.DELETE, null, "6a-1");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        response = handle(application, this.webSiteURL, testFileUrl,
                Method.HEAD, null, "6a-2");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 6b : delete a file that does not exist
        response = handle(application, this.webSiteURL, testFileUrl,
                Method.DELETE, null, "6b");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 6c : delete a directory (without and with trailing slash)
        // Distinct behaviours if an index has been defined or not
        if (indexName.length() == 0) {
            response = handle(application, this.webSiteURL, testDirectoryUrl,
                    Method.DELETE, null, "6c-1");
            assertEquals(Status.REDIRECTION_SEE_OTHER, response.getStatus());

            response = handle(application, response.getLocationRef()
                    .getIdentifier(),
                    response.getLocationRef().getIdentifier(), Method.DELETE,
                    null, "6c-2");
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
            response = handle(application, this.webSiteURL, this.webSiteURL,
                    Method.DELETE, null, "6c-3");
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
        } else {
            // As there is no index file in the directory, the response must
            // return the status Status.CLIENT_ERROR_NOT_FOUND
            response = handle(application, this.webSiteURL, testDirectoryUrl
                    + "/", Method.DELETE, null, "6c-2");
            assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
            response = handle(application, this.webSiteURL, this.webSiteURL,
                    Method.DELETE, null, "6c-3");
            assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        }

        // Test 7a : put one representation of the base file (in french
        // language)
        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.PUT, new StringRepresentation("message de test"), "7a");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // Test 7b : put another representation of the base file (in french
        // language) but the extensions are mixed
        // and there is no content negotiation
        directory.setNegotiateContent(false);
        response = handle(application, this.webSiteURL, this.baseFileUrlFrBis,
                Method.PUT, new StringRepresentation("message de test"), "7b-1");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        // The 2 resources in french must be present (the same actually)
        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.HEAD, null, "7b-2");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response = handle(application, this.webSiteURL, this.baseFileUrlFrBis,
                Method.HEAD, null, "7b-3");
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

        // Test 7c : delete the file representation of the resources with no
        // content negotiation
        // The 2 french resources are deleted (there were only one)
        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.DELETE, null, "7c-1");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.HEAD, null, "7c-2");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        response = handle(application, this.webSiteURL, this.baseFileUrlFrBis,
                Method.HEAD, null, "7c-3");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        response = handle(application, this.webSiteURL, this.baseFileUrlFrBis,
                Method.DELETE, null, "7c-4");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Test 7d : put another representation of the base file (in french
        // language) but the extensions are mixed
        // and there is content negotiation
        directory.setNegotiateContent(true);
        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.PUT, new StringRepresentation("message de test"), "7d-1");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());
        response = handle(application, this.webSiteURL, this.baseFileUrlFrBis,
                Method.PUT, new StringRepresentation("message de test Bis"),
                "7d-2");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        // only one resource in french must be present
        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.HEAD, null, "7d-3");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response = handle(application, this.webSiteURL, this.baseFileUrlFrBis,
                Method.HEAD, null, "7d-4");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // TBOI : not sure this test is correct
        // Check if only one resource has been created
        directory.setNegotiateContent(false);
        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.HEAD, null, "7d-5");
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // Test 7e : delete the file representation of the resources with
        // content negotiation
        directory.setNegotiateContent(true);
        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.DELETE, null, "7e-1");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        response = handle(application, this.webSiteURL, this.baseFileUrlFr,
                Method.HEAD, null, "7e-2");
        if (application.getTunnelService().isExtensionsTunnel()) {
            assertEquals(Status.SUCCESS_OK, response.getStatus());
        } else {
            assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        }

        response = handle(application, this.webSiteURL, this.baseFileUrlFrBis,
                Method.HEAD, null, "7e-8");
        if (application.getTunnelService().isExtensionsTunnel()) {
            assertEquals(Status.SUCCESS_OK, response.getStatus());
        } else {
            assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        }

        // Test 8 : must delete the english representation
        response = handle(application, this.webSiteURL, this.baseFileUrl,
                Method.DELETE, null, "8a");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        response = handle(application, this.webSiteURL, this.baseFileUrlEn,
                Method.DELETE, null, "8b");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        // Test 9a : put a new representation, the resource's URI contains
        // percent-encoded characters
        directory.setModifiable(true);
        response = handle(application, this.webSiteURL,
                this.percentEncodedFileUrl, Method.PUT,
                new StringRepresentation("this is test 9a"), "9a");
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // Test 9b : Try to get the representation of the new file
        response = handle(application, this.webSiteURL,
                this.percentEncodedFileUrl, Method.GET, null, "9b");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            response.getEntity().write(System.out);
            System.out.println("");
        }
        // Test 9c : Try to get the representation of the new file with an
        // equivalent URI
        response = handle(application, this.webSiteURL,
                this.percentEncodedFileUrlBis, Method.GET, null, "9c");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            response.getEntity().write(System.out);
            System.out.println("");
        }
        // Test 9d : Try to delete the file
        response = handle(application, this.webSiteURL,
                this.percentEncodedFileUrl, Method.DELETE, null, "9d");
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

        // Test 10a : Try to create a directory with an unkown hierarchy of
        // parent directories.
        response = handle(application, webSiteURL, testCreationDirectory,
                Method.PUT, new StringRepresentation("useless entity"), "10a");
        assertTrue(response.getStatus().equals(Status.REDIRECTION_SEE_OTHER));

        // Test 10b : Try to create a directory (with the trailing "/") with an
        // unkown hierarchy of parent directories.
        response = handle(application, webSiteURL, testCreationDirectory + "/",
                Method.PUT, new StringRepresentation("useless entity"), "10b");
        assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));

        // Test 10c : Try to create a file with an unkown hierarchy of
        // parent directories. The name and the metadata of the provided entity
        // don't match
        response = handle(application, webSiteURL, testCreationFile,
                Method.PUT, new StringRepresentation("file entity"), "10c");
        assertTrue(response.getStatus().equals(Status.REDIRECTION_SEE_OTHER));

        // Test 10d : Try to create a file with an unkown hierarchy of
        // parent directories. The name and the metadata of the provided entity
        // match
        response = handle(application, webSiteURL, testCreationTextFile,
                Method.PUT, new StringRepresentation("file entity"), "10d");
        assertTrue(response.getStatus().equals(Status.SUCCESS_CREATED));

        testDirectory.delete();
        System.out.println("End of tests*********************");
    }
}
