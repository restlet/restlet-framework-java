/*
 * Copyright 2005-2008 Noelios Consulting.
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

package com.noelios.restlet.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit test for the JavaMail connector.
 * 
 * @author Jerome Louvel
 */
public class JavaMailTestCase extends TestCase {

    private static final String _TRUSTSTORE = "d:/temp/certificats/myClientKeystore";

    private static final String DEBUG = "false";

    private static final String GMAIL_ID = "XXX";

    private static final String GMAIL_LOGIN = GMAIL_ID + "@gmail.com";

    private static final String GMAIL_PASSWORD = "XXX";

    private static final String GMAIL_SMTPS = "smtps://smtp.gmail.com";

    private static final String MAIL = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>"
            + "<email>"
            + "<head>"
            + "<subject>Test message</subject>"
            + "<from>restlet.testfr@yahoo.fr</from>"
            + "<to>restlet.testfr@yahoo.fr</to>"
            + "<cc>restlet.testfr@yahoo.fr</cc>"
            + "</head>"
            + "<body><![CDATA[Hi, this is a test.]]></body>" + "</email>";

    private static final String NOELIOS_LOGIN = "XXX";

    private static final String NOELIOS_PASSWORD = "XXX";

    private static final String NOELIOS_POPS = "pops://alaska.noelios.com";

    private static final String NOELIOS_SMTP = "smtp://alaska.noelios.com";

    private static final String YAHOO_ID = "XXX";

    private static final String YAHOO_PASSWORD = "XXX";

    private static final String YAHOO_POP = "pop://pop.mail.yahoo.fr";

    private static final String YAHOO_SMTP = "smtp://smtp.mail.yahoo.com";

    private void printMail(Client client, String baseUri, String href)
            throws IOException {
        Request request = new Request(Method.GET, baseUri + href);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.POP_BASIC, NOELIOS_LOGIN, NOELIOS_PASSWORD));

        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response.getEntity().write(System.out);
        System.out.println();
    }

    private void sendMail(Protocol protocol, Request request, boolean startTls) {
        Client client = new Client(protocol);
        client.getContext().getParameters().add("debug", DEBUG);
        client.getContext().getParameters().add("startTls",
                Boolean.toString(startTls).toLowerCase());

        request.setEntity(MAIL, MediaType.APPLICATION_XML);
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Override
    protected void setUp() throws Exception {
        File keyStoreFile = new File(_TRUSTSTORE);

        if (keyStoreFile.exists()) {
            System.setProperty("javax.net.ssl.trustStore", keyStoreFile
                    .getCanonicalPath());
        }
    }

    public void testPop() {
        Client client = new Client(Protocol.POP);
        client.getContext().getParameters().add("debug", DEBUG);

        Request request = new Request(Method.GET, YAHOO_POP);
        ChallengeResponse challengeResponse = new ChallengeResponse(
                ChallengeScheme.POP_BASIC, YAHOO_ID, YAHOO_PASSWORD);

        request.setChallengeResponse(challengeResponse);

        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // Send a new mail.
        testSmtp();

        // Try to get then delete the first message, if it exists.
        if (response.isEntityAvailable()) {
            DomRepresentation representation = response.getEntityAsDom();
            NodeList nodes = representation.getNodes("/emails/email");
            if (nodes.getLength() > 0) {
                Node node = representation.getNode("/emails/email[1]/@href");
                String mailUrl = YAHOO_POP + node.getNodeValue();
                request = new Request(Method.GET, mailUrl);
                request.setChallengeResponse(challengeResponse);
                response = client.handle(request);
                assertEquals(Status.SUCCESS_OK, response.getStatus());

                request = new Request(Method.DELETE, mailUrl);
                request.setChallengeResponse(challengeResponse);
                response = client.handle(request);
                assertEquals(Status.SUCCESS_OK, response.getStatus());
            }
        }
    }

    public void testPops() throws IOException {
        Client client = new Client(Protocol.POPS);
        client.getContext().getParameters().add("debug", DEBUG);

        String baseUri = NOELIOS_POPS;
        Request request = new Request(Method.GET, baseUri);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.POP_BASIC, NOELIOS_LOGIN, NOELIOS_PASSWORD));

        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        response.getEntity().write(System.out);
        System.out.println();

        DomRepresentation dom = response.getEntityAsDom();
        for (Node node : dom.getNodes("/emails/email")) {
            NamedNodeMap attrs = node.getAttributes();
            String href = attrs.getNamedItem("href").getNodeValue();
            printMail(client, baseUri, href);
        }
    }

    public void testSmtp() {
        Request request = new Request(Method.POST, YAHOO_SMTP);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.SMTP_PLAIN, YAHOO_ID, YAHOO_PASSWORD));
        sendMail(Protocol.SMTP, request, false);
    }

    public void testSmtps() {
        Request request = new Request(Method.POST, GMAIL_SMTPS);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.SMTP_PLAIN, GMAIL_LOGIN, GMAIL_PASSWORD));
        sendMail(Protocol.SMTPS, request, false);
    }

    public void testSmtpStartTls() {
        Request request = new Request(Method.POST, NOELIOS_SMTP);
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.SMTP_PLAIN, NOELIOS_LOGIN, NOELIOS_PASSWORD));
        sendMail(Protocol.SMTP, request, true);
    }

}
