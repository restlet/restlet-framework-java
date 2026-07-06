/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jackson;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonParseException;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

/**
 * Unit test for the Jackson extension.
 *
 * @author Jerome Louvel
 */
class JacksonTestCase {

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

    @Test
    void testCsv() throws Exception {
        Invoice invoice = createInvoice();
        JacksonRepresentation<Invoice> rep =
                new JacksonRepresentation<>(MediaType.TEXT_CSV, invoice);
        String text = rep.getText();
        Assertions.assertEquals("12456,1356533333882,false\n", text);
        rep =
                new JacksonRepresentation<>(
                        new StringRepresentation(text, rep.getMediaType()), Invoice.class);
        assertEquals(invoice, rep.getObject());
    }

    @Test
    void testException() throws Exception {
        Customer customer = createCustomer();

        MyException me = new MyException(customer, "CUST-1234");

        // Unless we are in debug mode, hide those properties
        me.setStackTrace(new StackTraceElement[0]);

        JacksonRepresentation<MyException> rep =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON, me);

        rep =
                new JacksonRepresentation<>(
                        new StringRepresentation(rep.getText(), rep.getMediaType()),
                        MyException.class);
        assertEquals(me, rep.getObject());
    }

    @Test
    void testJson() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON, customer);
        String text = rep.getText();

        Assertions.assertEquals(
                "{\"firstName\":\"Foo\",\"lastName\":\"Bar\",\"invoices\":[{\"date\":1356533333882,\"amount\":12456,\"paid\":false},{\"date\":1356533333882,\"amount\":7890,\"paid\":true}]}",
                text);

        rep =
                new JacksonRepresentation<>(
                        new StringRepresentation(text, rep.getMediaType()), Customer.class);
        assertEquals(customer, rep.getObject());
    }

    @Test
    void testSmile() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep =
                new JacksonRepresentation<>(MediaType.APPLICATION_JSON_SMILE, customer);
        rep = new JacksonRepresentation<>(rep, Customer.class);
        assertEquals(customer, rep.getObject());
    }

    @Test
    void testXml() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep =
                new JacksonRepresentation<>(MediaType.APPLICATION_XML, customer);
        String text = rep.getText();
        Assertions.assertEquals(
                "<Customer><firstName>Foo</firstName><lastName>Bar</lastName><invoices><invoices><date>1356533333882</date><amount>12456</amount><paid>false</paid></invoices><invoices><date>1356533333882</date><amount>7890</amount><paid>true</paid></invoices></invoices></Customer>",
                text);

        rep =
                new JacksonRepresentation<>(
                        new StringRepresentation(text, rep.getMediaType()), Customer.class);
        assertEquals(customer, rep.getObject());
    }

    @Test
    void testXmlBomb() {
        ClientResource cr =
                new ClientResource("clap://class/org/restlet/ext/jackson/jacksonBomb.xml");
        Representation xmlRep = cr.get();
        xmlRep.setMediaType(MediaType.APPLICATION_XML);

        Exception exception =
                assertThrows(
                        JsonParseException.class,
                        () -> new JacksonRepresentation<>(xmlRep, Customer.class).getObject());
        String expected =
                """
                Undeclared general entity "lol10"
                 at [row,col {unknown-source}]: [14,31]
                 at [Source: (BufferedInputStream); line: 14, column: 32]""";
        Assertions.assertEquals(
                normalizeLineEndings(expected), normalizeLineEndings(exception.getMessage()));
    }

    @Test
    void testYaml() throws Exception {
        Customer customer = createCustomer();
        JacksonRepresentation<Customer> rep =
                new JacksonRepresentation<>(MediaType.APPLICATION_YAML, customer);
        String text = rep.getText();

        Assertions.assertEquals(
                """
                ---
                firstName: "Foo"
                lastName: "Bar"
                invoices:
                - date: 1356533333882
                  amount: 12456
                  paid: false
                - date: 1356533333882
                  amount: 7890
                  paid: true
                """,
                text);

        rep =
                new JacksonRepresentation<>(
                        new StringRepresentation(text, rep.getMediaType()), Customer.class);
        assertEquals(customer, rep.getObject());
    }

    protected void assertEquals(Customer customer1, Customer customer2) {
        Assertions.assertEquals(customer1.getFirstName(), customer2.getFirstName());
        Assertions.assertEquals(customer1.getLastName(), customer2.getLastName());
        Assertions.assertEquals(customer1.getInvoices().size(), customer2.getInvoices().size());
        Assertions.assertEquals(
                customer1.getInvoices().getFirst().getAmount(),
                customer2.getInvoices().getFirst().getAmount());
        Assertions.assertEquals(
                customer1.getInvoices().get(1).getAmount(),
                customer2.getInvoices().get(1).getAmount());
        Assertions.assertEquals(
                customer1.getInvoices().getFirst().getDate(),
                customer2.getInvoices().getFirst().getDate());
        Assertions.assertEquals(
                customer1.getInvoices().get(1).getDate(), customer2.getInvoices().get(1).getDate());
    }

    protected void assertEquals(Invoice invoice1, Invoice invoice2) {
        Assertions.assertEquals(invoice1.getAmount(), invoice2.getAmount());
        Assertions.assertEquals(invoice1.getDate(), invoice2.getDate());
    }

    protected void assertEquals(MyException me1, MyException me2) {
        Assertions.assertEquals(me1.getErrorCode(), me2.getErrorCode());
        assertEquals(me1.getCustomer(), me2.getCustomer());
    }

    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }
}
