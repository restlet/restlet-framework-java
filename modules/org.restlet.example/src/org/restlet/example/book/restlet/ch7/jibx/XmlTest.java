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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch7.jibx;

import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jibx.JibxRepresentation;

/**
 *
 */
public class XmlTest {
    public static void main(String[] args) {
        final Restlet jibxRestlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                final Customer customer = new Customer();
                customer.state = "state_value";
                customer.street = "street_value";
                customer.city = "city_value";
                customer.zip = 100000;
                customer.phone = "phone_value";
                response.setEntity(new JibxRepresentation<Customer>(
                        MediaType.TEXT_XML, customer));
            }
        };

        final Server server = new Server(Protocol.HTTP, 8182, jibxRestlet);

        try {
            server.start();

            final Client client = new Client(Protocol.HTTP);
            final Response response = client.get("http://localhost:8182/");
            if (response.isEntityAvailable()) {
                final JibxRepresentation<Customer> representation = new JibxRepresentation<Customer>(
                        response.getEntity(), Customer.class);
                final Customer customer = representation.getObject();
                System.out.println(customer.state);
                System.out.println(customer.street);
                System.out.println(customer.city);
                System.out.println(customer.zip);
                System.out.println(customer.phone);
            } else {
                System.err.println("No entity");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            try {
                server.stop();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
