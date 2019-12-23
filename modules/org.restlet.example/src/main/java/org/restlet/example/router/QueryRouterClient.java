/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.example.router;

import org.restlet.Application;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;

public class QueryRouterClient extends Application {
    public static void main(String[] args) throws Exception {
        ClientResource cr = new ClientResource("http://localhost:8182/path");
        cr.addQueryParameter("q", "hello");
        cr.get().write(System.out);

        cr = new ClientResource("http://localhost:8182/path");
        cr.addQueryParameter("q", "bye");
        cr.get().write(System.out);

        cr = new ClientResource("http://localhost:8182/path");
        cr.addQueryParameter("q", "test");
        try {
            cr.get();
        } catch (Exception e) {
            if (Status.CLIENT_ERROR_NOT_FOUND.equals(cr.getStatus())) {
                System.out.println("fine.");
            } else {
                System.out.println("Should be 404 not found response.");
            }
        }
    }
}
