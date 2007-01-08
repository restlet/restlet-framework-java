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
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Response;

/**
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TestClient {

    public static void main(String... args) throws Exception {
        createNewUser("jlouvel", "myPassword", "Jerome Louvel",
                "contact@noelios.com");
        Thread.sleep(1000);
        deleteUser("jlouvel");
    }

    public static void createNewUser(String name, String password,
            String fullName, String email) {
        Form form = new Form();
        form.add("user[name]", name);
        form.add("user[password]", password);
        form.add("user[full_name]", fullName);
        form.add("user[email]", email);

        Response resp = new Client(Protocol.HTTP).post(
                "http://localhost:3000/v1/users", form.getWebRepresentation());
        System.out.println(resp.getStatus() + " : " + resp.getRedirectRef());
    }

    public static void deleteUser(String name) {
        Response resp = new Client(Protocol.HTTP)
                .delete("http://localhost:3000/v1/users/" + name);
        System.out.println(resp.getStatus() + " : " + resp.getRedirectRef());
    }

}
