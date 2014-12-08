/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jackson;

import java.io.IOException;
import java.util.Date;

import org.restlet.data.MediaType;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.test.RestletTestCase;

/**
 * Unit test for the Jackson extension.
 * 
 * @author Jerome Louvel
 */
public class JacksonTestCase extends RestletTestCase {

    protected Customer createCustomer() {
        Date date = new Date(1356533333882L);

        Customer result = new Customer();
        result.setFirstName("Foo");
        result.setLastName("Bar");

        Invoice invoice = new Invoice();
        invoice.setAmount(12456);
        invoice.setDate(date);
        invoice.setPaid(false);
        result.getInvoices().add(invoice);

        invoice = new Invoice();
        invoice.setAmount(7890);
        invoice.setDate(date);
        invoice.setPaid(true);
        result.getInvoices().add(invoice);

        return result;
    }

    protected Invoice createInvoice() {
        Date date = new Date(1356533333882L);
        Invoice invoice = new Invoice();
        invoice.setAmount(12456);
        invoice.setDate(date);
        invoice.setPaid(false);
        return invoice;
    }

    public void testCsv() throws Exception {
        Invoice invoice = createInvoice();
        JacksonRepresentation<Invoice> rep = new JacksonRepresentation<Invoice>(
                MediaType.TEXT_CSV, invoice);
        String text = rep.getText();
        assertEquals("12456,1356533333882,false\n", text);
        rep = new JacksonRepresentation<Invoice>(new StringRepresentation(text,
                rep.getMediaType()), Invoice.class);
        verify(invoice, rep.getObject());
    }

    public void testException() throws Exception {
        Customer customer = createCustomer();

        MyException me = new MyException(customer, "CUST-1234");

        // Unless we are in debug mode, hide those properties
        me.setStackTrace(new StackTraceElement[0]);

        JacksonRepresentation<MyException> rep = new JacksonRepresentation<MyException>(
                MediaType.APPLICATION_JSON, me);
        String text = rep.getText();
        System.out.println(text);

        rep = new JacksonRepresentation<MyException>(new StringRepresentation(
                text, rep.getMediaType()), MyException.class);
        verify(me, rep.getObject());
    }

    public void testJson() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep = new JacksonRepresentation<Customer>(
                MediaType.APPLICATION_JSON, customer);
        String text = rep.getText();
        assertEquals(
                "{\"firstName\":\"Foo\",\"lastName\":\"Bar\",\"invoices\":[{\"date\":1356533333882,\"amount\":12456,\"paid\":false},{\"date\":1356533333882,\"amount\":7890,\"paid\":true}]}",
                text);

        rep = new JacksonRepresentation<Customer>(new StringRepresentation(
                text, rep.getMediaType()), Customer.class);
        verify(customer, rep.getObject());
    }

    public void testSmile() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep = new JacksonRepresentation<Customer>(
                MediaType.APPLICATION_JSON_SMILE, customer);
        rep = new JacksonRepresentation<Customer>(rep, Customer.class);
        verify(customer, rep.getObject());
    }

    public void testXml() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep = new JacksonRepresentation<Customer>(
                MediaType.APPLICATION_XML, customer);
        String text = rep.getText();
        assertEquals(
                "<Customer><firstName>Foo</firstName><lastName>Bar</lastName><invoices><invoices><date>1356533333882</date><amount>12456</amount><paid>false</paid></invoices><invoices><date>1356533333882</date><amount>7890</amount><paid>true</paid></invoices></invoices></Customer>",
                text);
        rep = new JacksonRepresentation<Customer>(new StringRepresentation(
                text, rep.getMediaType()), Customer.class);
        verify(customer, rep.getObject());
    }

    public void testXmlBomb() throws IOException {
        ClientResource cr = new ClientResource(
                "clap://class/org/restlet/test/ext/jackson/jacksonBomb.xml");
        Representation xmlRep = cr.get();
        xmlRep.setMediaType(MediaType.APPLICATION_XML);
        boolean error = false;
        try {
            new JacksonRepresentation<Customer>(xmlRep, Customer.class)
                    .getObject();
        } catch (Exception e) {
            error = true;
        }
        assertTrue(error);
    }

    public void testYaml() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep = new JacksonRepresentation<Customer>(
                MediaType.APPLICATION_YAML, customer);
        String text = rep.getText();
        assertEquals("---\n" + "firstName: \"Foo\"\n" + "lastName: \"Bar\"\n"
                + "invoices:\n" + "- date: 1356533333882\n"
                + "  amount: 12456\n" + "  paid: false\n"
                + "- date: 1356533333882\n" + "  amount: 7890\n"
                + "  paid: true\n", text);
        rep = new JacksonRepresentation<Customer>(new StringRepresentation(
                text, rep.getMediaType()), Customer.class);
        verify(customer, rep.getObject());
    }

    protected void verify(Customer customer1, Customer customer2) {
        assertEquals(customer1.getFirstName(), customer2.getFirstName());
        assertEquals(customer1.getLastName(), customer2.getLastName());
        assertEquals(customer1.getInvoices().size(), customer2.getInvoices()
                .size());
        assertEquals(customer1.getInvoices().get(0).getAmount(), customer2
                .getInvoices().get(0).getAmount());
        assertEquals(customer1.getInvoices().get(1).getAmount(), customer2
                .getInvoices().get(1).getAmount());
        assertEquals(customer1.getInvoices().get(0).getDate(), customer2
                .getInvoices().get(0).getDate());
        assertEquals(customer1.getInvoices().get(1).getDate(), customer2
                .getInvoices().get(1).getDate());
    }

    protected void verify(Invoice invoice1, Invoice invoice2) {
        assertEquals(invoice1.getAmount(), invoice2.getAmount());
        assertEquals(invoice1.getDate(), invoice2.getDate());
    }

    protected void verify(MyException me1, MyException me2) {
        assertEquals(me1.getErrorCode(), me2.getErrorCode());
        verify(me1.getCustomer(), me2.getCustomer());
    }
}
