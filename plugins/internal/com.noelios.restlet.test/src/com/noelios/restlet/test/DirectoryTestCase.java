/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.Container;
import org.restlet.data.Method;
import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.noelios.restlet.DirectoryFinder;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Unit tests for the directoryFinder.
 * @author Thierry Boileau
 */
public class DirectoryTestCase extends TestCase
{
   String webSiteURL = "http://myapplication/";
   String baseFileUrl = webSiteURL.concat("fichier.txt");
   String baseFileUrlEn = webSiteURL.concat("fichier.txt.en");
   String baseFileUrlFr = webSiteURL.concat("fichier.txt.fr");
   String baseFileUrlFrBis = webSiteURL.concat("fichier.fr.txt");
   String testFileUrl;

   public void testDirectory() throws IOException
   {
      try
      {
         // Create a temporary directory for the tests
         File testDir = new File(System.getProperty("java.io.tmpdir"),
               "DirectoryTestCase");
         testDir.mkdir();
         // Create a temporary file for the tests (the tests directory is not empty)
         File testFile = File.createTempFile("test", ".txt", testDir);

         testFileUrl = webSiteURL.concat(testFile.getName());

         // Create a new Restlet container
         Container clientContainer = new Container();

         // Now, let's start the container!
         clientContainer.start();

         // Create a directoryFinder that manages a local Directory with no index
         DirectoryFinder directory = new DirectoryFinder(clientContainer.getContext(),
               testDir.toURI().toString(), "");
         testDirectory(directory);

         // Now, let's stop the container!
         clientContainer.stop();

         testFile.delete();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Helper
    * @param directory
    * @param webSiteURL
    * @param baseFileUrl
    * @param baseFileUrlEn
    * @param baseFileUrlFr
    * @param testFileUrl
    * @throws IOException
    */
   private void testDirectory(DirectoryFinder directory) throws IOException
   {
      //Test n°1a : directory does not allow to GET its content
      directory.setListingAllowed(false);
      Response response = handle(directory, webSiteURL, webSiteURL, Method.GET, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));

      //Test n°1a : directory allows to GET its content
      directory.setListingAllowed(true);
      response = handle(directory, webSiteURL, webSiteURL, Method.GET, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      if (response.getStatus().equals(Status.SUCCESS_OK))
      {
         // should list all files in the directory (at least the temporary file generated before)
         response.getEntity().write(System.out);
      }

      //Test n°2a : tests the HEAD method
      response = handle(directory, webSiteURL, testFileUrl, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

      //Test n°2b : try to GET a file that does not exist
      response = handle(directory, webSiteURL, webSiteURL + "123456.txt", Method.GET, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));

      //Test n°3a : try to put a new representation, but the directory is by default read only
      response = handle(directory, webSiteURL, baseFileUrl, Method.PUT,
            new StringRepresentation("this is a test"));
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED));

      //Test n°3b : try to put a new representation, the directory is no more read only
      directory.setModifiable(true);
      response = handle(directory, webSiteURL, baseFileUrl, Method.PUT,
            new StringRepresentation("this is a test"));
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

      //Test n°4 : Try to get the representation of the new file
      response = handle(directory, webSiteURL, baseFileUrl, Method.GET, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      if (response.getStatus().equals(Status.SUCCESS_OK))
      {
         response.getEntity().write(System.out);
         System.out.println("");
      }

      //Test n°5 : add a new representation of the same base file
      response = handle(directory, webSiteURL, baseFileUrlEn, Method.PUT,
            new StringRepresentation("this is a test - En"));
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      response = handle(directory, webSiteURL, baseFileUrl, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      response = handle(directory, webSiteURL, baseFileUrlEn, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

      //Test n°6a : delete a file
      response = handle(directory, webSiteURL, testFileUrl, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));

      response = handle(directory, webSiteURL, testFileUrl, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));

      //Test n°6b : delete a file that does not exist
      response = handle(directory, webSiteURL, testFileUrl, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));

      //Test n°6c : delete a directory
      response = handle(directory, webSiteURL, webSiteURL, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN));

      //Test n°7a : put one representation of the base file (in french language)
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.PUT,
            new StringRepresentation("message de test"));
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

      //Test n°7b : put another representation of the base file (in french language) but the extensions are mixed
      // and there is no content negotiation
      directory.setNegotiationEnabled(false);
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.PUT,
            new StringRepresentation("message de test"));
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      // the 2 resources in french must be present
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

      //Test n°7c : delete the file representation of the resources with no content negotiation
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));

      //Test n°7d : put another representation of the base file (in french language) but the extensions are mixed
      // and there is content negotiation
      directory.setNegotiationEnabled(true);
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.PUT,
            new StringRepresentation("message de test"));
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.PUT,
            new StringRepresentation("message de test Bis"));
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      // only one resource in french must be present
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

      //Check if only one resource has been created
      directory.setNegotiationEnabled(false);
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));

      //Test n°7e : delete the file representation of the resources with content negotiation
      directory.setNegotiationEnabled(true);
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));
      response = handle(directory, webSiteURL, baseFileUrlFr, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));
      response = handle(directory, webSiteURL, baseFileUrlFrBis, Method.HEAD, null);
      assertTrue(response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND));

      //Test n°8 : should not delete the english representation
      response = handle(directory, webSiteURL, baseFileUrl, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));
      response = handle(directory, webSiteURL, baseFileUrlEn, Method.DELETE, null);
      assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));
   }

   /**
    * Helper for the test
    * @param directory
    * @param baseRef
    * @param resourceRef
    * @param method
    * @param entity
    * @return
    */
   private Response handle(DirectoryFinder directory, String baseRef, String resourceRef,
         Method method, Representation entity)
   {
      Request request = new Request();
      Response response = new Response(request);
      request.setResourceRef(resourceRef);
      request.setBaseRef(baseRef);
      request.setMethod(method);
      if (Method.PUT.equals(method))
      {
         request.setEntity(entity);
      }

      directory.handle(request, response);
      return response;
   }

   public static void main(String[] args)
   {
      try
      {
         new DirectoryTestCase().testDirectory();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
