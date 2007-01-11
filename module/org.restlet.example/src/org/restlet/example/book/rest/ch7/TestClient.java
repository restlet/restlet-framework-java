/*
 * Copyright 2005-2007 Noelios Consulting.
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
public class TestClient {

    public static final String APPLICATION_URI = "http://localhost:3000/v1";

    public static void main(String... args) throws Exception {
        putUser("jlouvel", "myPassword", "Jerome Louvel", "contact@noelios.com");
        putBookmark("jlouvel", "myPassword", "http://www.restlet.org",
                "Restlet", "Lightweight framework for Java", false);
        // deleteUser("jlouvel");
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
        Request request = new Request(Method.PUT,
                getBookmarkUri(userName, uri), form.getWebRepresentation());
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, userName, password));

        // Invoke the client HTTP connector
        Response resp = new Client(Protocol.HTTP).handle(request);
        System.out.println(resp.getStatus());
    }

    public static void putUser(String name, String password, String fullName,
            String email) {
        Form form = new Form();
        form.add("user[password]", password);
        form.add("user[full_name]", fullName);
        form.add("user[email]", email);

        Response resp = new Client(Protocol.HTTP).put(getUserUri(name), form
                .getWebRepresentation());
        System.out.println(resp.getStatus());
    }

    public static void deleteUser(String name, String password) {
        Response resp = new Client(Protocol.HTTP).delete(getUserUri(name));
        System.out.println(resp.getStatus() + " : " + resp.getRedirectRef());
    }

    public static String getUserUri(String name) {
        return APPLICATION_URI + "/users/" + name;
    }

    public static String getBookmarkUri(String userName, String uri) {
        return APPLICATION_URI + "/users/" + userName + "/bookmarks/" + uri;
    }

}
