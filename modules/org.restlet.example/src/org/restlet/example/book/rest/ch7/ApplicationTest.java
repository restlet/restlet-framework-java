/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.example.book.rest.ch7;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

/**
 * Client code that can be used to test the application developed in the chapter
 * 7 of the book and converted to Restlets in the chapter 13.
 * 
 * @author Jerome Louvel
 */
public class ApplicationTest {
    /** Base application URI. */
    public static final String APPLICATION_URI = "http://localhost:3000/v1";

    public static void deleteBookmark(String userName, String password,
            String uri) {
        ClientResource resource = getAuthenticatedResource(getBookmarkUri(
                userName, uri), userName, password);
        resource.delete();
        System.out.println(resource.getStatus() + " : "
                + resource.getLocationRef());
    }

    public static void deleteUser(String userName, String password) {
        ClientResource resource = getAuthenticatedResource(
                getUserUri(userName), userName, password);
        resource.delete();
        System.out.println(resource.getStatus() + " : "
                + resource.getLocationRef());
    }

    /**
     * Creates an authenticated resour ce.
     * 
     * @param uri
     *            The target resource URI.
     * @param login
     *            The login name.
     * @param password
     *            The password.
     * @return The authenticated resource to use.
     */
    public static ClientResource getAuthenticatedResource(String uri,
            String login, String password) {
        ClientResource result = new ClientResource(uri);
        result.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, login, password));
        return result;
    }

    public static String getBookmarkUri(String userName, String uri) {
        return APPLICATION_URI + "/users/" + userName + "/bookmarks/" + uri;
    }

    public static String getUserUri(String name) {
        return APPLICATION_URI + "/users/" + name;
    }

    /**
     * Main method to use for testing.
     * 
     * @param args
     *            The arguments or nothing for a usage description.
     */
    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage depends on the number of arguments:");
            System.out.println(" - Deletes a user     : userName, password");
            System.out
                    .println(" - Deletes a bookmark : userName, password, URI");
            System.out
                    .println(" - Adds a new user    : userName, password, \"full name\", email");
            System.out
                    .println(" - Adds a new bookmark: userName, password, URI, shortDescription, longDescription, restrict");
        } else if (args.length == 2) {
            deleteUser(args[0], args[1]);
        } else if (args.length == 3) {
            deleteBookmark(args[0], args[1], args[2]);
        } else if (args.length == 4) {
            putUser(args[0], args[1], args[2], args[3]);
        } else if (args.length == 6) {
            putBookmark(args[0], args[1], args[2], args[3], args[4], Boolean
                    .valueOf(args[5]));
        }
    }

    public static void putBookmark(String userName, String password,
            String uri, String shortDescription, String longDescription,
            boolean restrict) {
        Form form = new Form();
        form.add("bookmark[short_description]", shortDescription);
        form.add("bookmark[long_description]", longDescription);
        form.add("bookmark[restrict]", Boolean.toString(restrict));

        // Create an authenticated resource as a bookmark is in
        // the user's private area
        ClientResource resource = getAuthenticatedResource(getBookmarkUri(
                userName, uri), userName, password);
        resource.put(form.getWebRepresentation());

        System.out.println(resource.getStatus());
    }

    public static void putUser(String userName, String password,
            String fullName, String email) {
        Form form = new Form();
        form.add("user[password]", password);
        form.add("user[full_name]", fullName);
        form.add("user[email]", email);

        ClientResource resource = new ClientResource(getUserUri(userName));
        resource.put(form.getWebRepresentation());
        System.out.println(resource.getStatus());
    }

}
