package org.restlet.example.book.restlet.ch7.jaxb;

import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxb.JaxbRepresentation;

public class XmlTest {
    public static void main(String[] args) throws Exception {
        final Restlet jaxbRestlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                final Customer customer = new Customer();
                customer.setCity("city_value");
                customer.setPhone("phone_value");
                customer.setState("state_value");
                customer.setStreet("street_value");
                customer.setZip(10000);

                response.setEntity(new JaxbRepresentation<Customer>(
                        MediaType.TEXT_XML, customer));
            }
        };

        final Server server = new Server(Protocol.HTTP, 8182, jaxbRestlet);

        try {
            server.start();

            final Client client = new Client(Protocol.HTTP);
            final Response response = client.get("http://localhost:8182/");
            if (response.isEntityAvailable()) {
                final JaxbRepresentation<Customer> representation = new JaxbRepresentation<Customer>(
                        response.getEntity(), Customer.class);
                final Customer customer = representation.getObject();
                System.out.println(customer.getCity());
                System.out.println(customer.getPhone());
                System.out.println(customer.getState());
                System.out.println(customer.getStreet());
                System.out.println(customer.getZip());
            } else {
                System.err.println("No entity");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
