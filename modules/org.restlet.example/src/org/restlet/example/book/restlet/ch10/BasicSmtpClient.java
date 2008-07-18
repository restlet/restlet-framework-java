package org.restlet.example.book.restlet.ch10;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

public class BasicSmtpClient {

    private static final String MAIL = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>"
            + "<email>"
            + " <head>"
            + "  <subject>Test</subject>"
            + "  <from>restlet.testfr@yahoo.fr</from>"
            + "  <to>thboileau@gmail.com</to>"
            + "  <cc>thboileau@hotmail.com</cc>"
            + " </head>"
            + " <body><![CDATA[This is a simple message for you.]]></body>"
            + "</email>";

    public static void main(String[] args) {

        final Representation mail = new StringRepresentation(MAIL,
                MediaType.TEXT_XML);

        // Sends the request and gets the response
        final Request request = new Request(new Method("POST"),
                "smtp://smtp.mail.yahoo.com", mail);
        final ChallengeResponse challengeResponse = new ChallengeResponse(
                ChallengeScheme.SMTP_PLAIN, "restlet.testfr", "saya08");
        request.setChallengeResponse(challengeResponse);

        // Instantiates a client according to a protocol
        final Client client = new Client(Protocol.SMTP);
        // Sends the request
        final Response response = client.handle(request);

        // Prints the status of the response
        System.out.println(response.getStatus());
    }
}
