package org.restlet.example.book.restlet.ch7.jibx;

import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jibx.JibxRepresentation;

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
