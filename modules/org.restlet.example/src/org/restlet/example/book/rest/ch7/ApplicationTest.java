/*
 * Copyright 2005-2007 Noelios Technologies.
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

package org.restlet.example.book.rest.ch7;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Client code that can be used to test the application developped in the
 * chapter 7 of the book and converted to Restlets in the chapter 13.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ApplicationTest {
    /** Base application URI. */
    public static final String APPLICATION_URI = "http://localhost:3000/v1";

    public static void deleteBookmark(String userName, String password,
            String uri) {
        Request request = getAuthenticatedRequest(Method.DELETE,
                getBookmarkUri(userName, uri), userName, password);
        Response resp = new Client(Protocol.HTTP).handle(request);
        System.out.println(resp.getStatus() + " : " + resp.getRedirectRef());
    }

    public static void deleteUser(String userName, String password) {
        Request request = getAuthenticatedRequest(Method.DELETE,
                getUserUri(userName), userName, password);
        Response resp = new Client(Protocol.HTTP).handle(request);
        System.out.println(resp.getStatus() + " : " + resp.getRedirectRef());
    }

    /**
     * Creates an authenticated request.
     * 
     * @param method
     *            The request method.
     * @param uri
     *            The target resource URI.
     * @param login
     *            The login name.
     * @param password
     *            The password.
     * @return The authenticated request to use.
     */
    public static Request getAuthenticatedRequest(Method method, String uri,
            String login, String password) {
        Request request = new Request(method, uri);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, login, password));
        return request;
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

        // Create an authenticated request as a bookmark is in
        // the user's private area
        Request request = getAuthenticatedRequest(Method.PUT, getBookmarkUri(
                userName, uri), userName, password);
        request.setEntity(form.getWebRepresentation());

        // Invoke the client HTTP connector
        Response resp = new Client(Protocol.HTTP).handle(request);
        System.out.println(resp.getStatus());
    }

    public static void putUser(String userName, String password,
            String fullName, String email) {
        Form form = new Form();
        form.add("user[password]", password);
        form.add("user[full_name]", fullName);
        form.add("user[email]", email);

        Response resp = new Client(Protocol.HTTP).put(getUserUri(userName),
                form.getWebRepresentation());
        System.out.println(resp.getStatus());
    }

}
