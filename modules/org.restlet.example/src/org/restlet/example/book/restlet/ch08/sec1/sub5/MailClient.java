package org.restlet.example.book.restlet.ch08.sec1.sub5;

import org.restlet.data.Form;
import org.restlet.resource.ClientResource;

/**
 * Mail client updating a mail by submitting a form.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        ClientResource mailClient = new ClientResource(
                "http://localhost:8111/accounts/123/mails/abc");
        Form form = new Form();
        form.add("subject", "Message to Jérôme");
        form.add("content", "Doh!\n\nAllo?");
        mailClient.put(form).write(System.out);
    }

}
