/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.bench;

import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Response;

public class TestGetClient {

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();

        Client client = new Client(Protocol.HTTP);
        Response response = client.get("http://localhost:8554/");
        System.out.println("Status: " + response.getStatus());

        if (response.getStatus().isSuccess()) {
            long expectedSize = response.getEntity().getSize();
            long receivedSize = response.getEntity().exhaust();

            System.out.println("Size expected: " + expectedSize);
            System.out.println("Size consumed: " + receivedSize);

            if ((expectedSize != -1) && (expectedSize != receivedSize)) {
                System.out.println("ERROR: SOME BYTES WERE LOST!");
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Duration: " + (endTime - startTime) + " ms");
    }

}
