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

package com.noelios.restlet.ext.javamail;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.DateUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.noelios.restlet.ClientHelper;
import com.sun.mail.pop3.POP3Folder;

/**
 * Client connector to a mail server. This connector supports the SMTP, SMTP
 * with STARTTLS and SMTPS protocols to send emails, POP v3 and POPS v3 to
 * retrieved emails from a mail box.<br>
 * <br>
 * To send an email, send a POST request with a resource reference on a SMTP or
 * SMTPS URI and use an XML email as the entity. A SMTP URI has the following
 * syntax: smtp://host[:port]<br>
 * <br>
 * Use the
 * {@link Request#setChallengeResponse(org.restlet.data.ChallengeResponse)}
 * method to set the identified/login and secret/password. You will also need to
 * specify the {@link ChallengeScheme#SMTP_PLAIN} challenge scheme.<br>
 * <br>
 * Sample XML email:<br>
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot; ?&gt;
 *  &lt;email&gt;
 *  &lt;head&gt;
 *  &lt;subject&gt;Account activation&lt;/subject&gt;
 *  &lt;from&gt;support@restlet.org&lt;/from&gt;
 *  &lt;to&gt;user@domain.com&lt;/to&gt;
 *  &lt;cc&gt;log@restlet.org&lt;/cc&gt;
 *  &lt;/head&gt;
 *  &lt;body&gt;&lt;![CDATA[Your account was sucessfully created!]]&gt;&lt;/body&gt;
 *  &lt;/email&gt;
 * </pre>
 * 
 * To receive the list of emails, send a GET request to a resource reference on
 * a POP or POPS URI, leaving the reference path empty. A POP URI has the
 * following syntax: pop://host[:port]<br>
 * <br>
 * Use the
 * {@link Request#setChallengeResponse(org.restlet.data.ChallengeResponse)}
 * method to set the identified/login and secret/password. You will also need to
 * specify the {@link ChallengeScheme#POP_BASIC} or the
 * {@link ChallengeScheme#POP_DIGEST} challenge scheme.<br>
 * <br>
 * Sample XML list of emails:<br>
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot; ?&gt;
 * &lt;emails&gt; 
 *    &lt;email href=’/1234’/&gt;
 *    &lt;email href=’/5678’/&gt;
 *    &lt;email href=’/9012’/&gt;
 *    &lt;email href=’/3456’/&gt;
 * &lt;/emails&gt;
 * </pre>
 * 
 * To retrieve an individual email, just add the href attribute at the end of
 * the POP URI, such as: pop://host/1234<br>
 * 
 * Here is the list of parameters that are supported: <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>startTls</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the SMTP connector will attempt to start a TLS tunnel, right
 * after the SMTP connection is established.</td>
 * </tr>
 * <tr>
 * <td>debug</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the connector will generate JavaMail debug messages.</td>
 * </tr>
 * </table>
 * 
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class JavaMailClientHelper extends ClientHelper {
    /**
     * Creates a high-level request.
     * 
     * @param smtpURI
     *                The SMTP server's URI (ex: smtp://localhost).
     * @param email
     *                The email to send (valid XML email).
     * @deprecated With no replacement as it creates an unecessary dependency on
     *             NRE classes.
     */
    @Deprecated
    public static Request create(String smtpURI, Representation email) {
        Request result = new Request();
        result.setMethod(Method.POST);
        result.setResourceRef(smtpURI);
        result.setEntity(email);
        return result;
    }

    /**
     * Creates a high-level request.
     * 
     * @param smtpURI
     *                The SMTP server's URI (ex: smtp://localhost).
     * @param email
     *                The email to send (valid XML email).
     * @param login
     *                Authenticate using this login name.
     * @param password
     *                Authenticate using this password.
     * @deprecated With no replacement as it creates an unecessary dependency on
     *             NRE classes.
     */
    @Deprecated
    public static Request create(String smtpURI, Representation email,
            String login, String password) {
        Request result = create(smtpURI, email);
        result.getChallengeResponse().setIdentifier(login);
        result.getChallengeResponse().setSecret(password);
        return result;
    }

    /**
     * Constructor.
     * 
     * @param client
     *                The client to help.
     */
    public JavaMailClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.SMTP);
        getProtocols().add(Protocol.SMTPS);
        getProtocols().add(Protocol.POP);
        getProtocols().add(Protocol.POPS);
    }

    /**
     * Returns the request login.
     * 
     * @param request
     *                The high-level request.
     * @return The login.
     */
    private String getLogin(Request request) {
        return request.getChallengeResponse().getIdentifier();
    }

    /**
     * Returns the request password.
     * 
     * @param request
     *                The high-level request.
     * @return The password.
     */
    private String getPassword(Request request) {
        return new String(request.getChallengeResponse().getSecret());
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            Protocol protocol = request.getProtocol();

            if (Protocol.SMTP.equals(protocol)
                    || Protocol.SMTPS.equals(protocol)) {
                handleSmtp(request, response);
            } else if (Protocol.POP.equals(protocol)
                    || Protocol.POPS.equals(protocol)) {
                handlePop(request, response);
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        } catch (NoSuchProviderException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
        } catch (AddressException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
        } catch (MessagingException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    /**
     * Handles a POP or POPS request.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @throws IOException
     * @throws MessagingException
     * @throws IOException
     */
    private void handlePop(Request request, Response response)
            throws MessagingException, IOException {
        // Parse the POP URI
        String popHost = request.getResourceRef().getHostDomain();
        int popPort = request.getResourceRef().getHostPort();
        String path = request.getResourceRef().getPath();

        if (popPort == -1) {
            // No port specified, the default one should be used
            popPort = request.getProtocol().getDefaultPort();
        }

        if ((popHost == null) || (popHost.equals(""))) {
            throw new IllegalArgumentException("Invalid POP host specified");
        }

        // Check if authentication required
        boolean authenticate = ((getLogin(request) != null) && (getPassword(request) != null));
        boolean apop = authenticate
                && (ChallengeScheme.POP_DIGEST.equals(request
                        .getChallengeResponse().getScheme()));

        String transport = null;

        if (Protocol.POP.equals(request.getProtocol())) {
            transport = "pop3";
        } else if (Protocol.POPS.equals(request.getProtocol())) {
            transport = "pop3s";
        }

        Properties props = System.getProperties();
        props.put("mail." + transport + ".host", popHost);
        props.put("mail." + transport + ".port", Integer.toString(popPort));
        props.put("mail." + transport + ".apop.enable", Boolean.toString(apop));

        Session session = Session.getDefaultInstance(props);
        session.setDebug(isDebug());
        Store store = session.getStore(transport);
        store.connect(getLogin(request), getPassword(request));
        POP3Folder inbox = (POP3Folder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        FetchProfile profile = new FetchProfile();
        profile.add(UIDFolder.FetchProfileItem.UID);
        Message[] messages = inbox.getMessages();
        inbox.fetch(messages, profile);

        if ((path == null) || path.equals("") || path.equals("/")) {
            DomRepresentation result = new DomRepresentation(
                    MediaType.APPLICATION_XML);
            Document dom = result.getDocument();
            Element emails = dom.createElement("emails");
            dom.appendChild(emails);

            // Retrieve the list of messages
            Element email;
            for (int i = 0; i < messages.length; i++) {
                String uid = inbox.getUID(messages[i]);

                email = dom.createElement("email");
                email.setAttribute("href", "/" + uid);
                emails.appendChild(email);
            }

            // Set the result document
            response.setEntity(result);
        } else if (path.startsWith("/")) {
            // Retrieve the specified message
            String mailUid = path.substring(1);
            Message message = null;

            for (int i = 0; (message == null) && (i < messages.length); i++) {
                String uid = inbox.getUID(messages[i]);

                if (mailUid.equals(uid)) {
                    message = messages[i];
                }
            }

            if (message == null) {
                // Message not found
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                        "No message matches the given UID: " + mailUid);
            } else {
                // Message found
                DomRepresentation result = new DomRepresentation(
                        MediaType.APPLICATION_XML);
                Document dom = result.getDocument();
                Element email = dom.createElement("email");
                dom.appendChild(email);

                // Add the email header
                Element head = dom.createElement("head");
                email.appendChild(head);

                if (message.getSubject() != null) {
                    Element subject = dom.createElement("subject");
                    subject.setTextContent(message.getSubject());
                    head.appendChild(subject);
                }

                Address[] froms = message.getFrom();
                if (froms != null) {
                    for (Address fromAddress : froms) {
                        Element from = dom.createElement("from");
                        from.setTextContent(fromAddress.toString());
                        head.appendChild(from);
                    }
                }

                Address[] tos = message.getRecipients(Message.RecipientType.TO);
                if (tos != null) {
                    for (Address toAddress : tos) {
                        Element to = dom.createElement("to");
                        to.setTextContent(toAddress.toString());
                        head.appendChild(to);
                    }
                }

                Address[] ccs = message.getRecipients(Message.RecipientType.CC);
                if (ccs != null) {
                    for (Address ccAddress : ccs) {
                        Element cc = dom.createElement("cc");
                        cc.setTextContent(ccAddress.toString());
                        head.appendChild(cc);
                    }
                }

                Address[] bccs = message
                        .getRecipients(Message.RecipientType.BCC);
                if (bccs != null) {
                    for (Address bccAddress : bccs) {
                        Element bcc = dom.createElement("bcc");
                        bcc.setTextContent(bccAddress.toString());
                        head.appendChild(bcc);
                    }
                }

                if (message.getReceivedDate() != null) {
                    Element received = dom.createElement("received");
                    received.setTextContent(DateUtils.format(message
                            .getReceivedDate(), DateUtils.FORMAT_RFC_1123
                            .get(0)));
                    head.appendChild(received);
                }

                if (message.getSentDate() != null) {
                    Element sent = dom.createElement("sent");
                    sent.setTextContent(DateUtils.format(message.getSentDate(),
                            DateUtils.FORMAT_RFC_1123.get(0)));
                    head.appendChild(sent);
                }

                // Add the email body
                if (message.getContentType() != null) {
                    MediaType contentType = MediaType.valueOf(message
                            .getContentType());

                    if (MediaType.TEXT_PLAIN.equals(contentType)) {
                        Representation content = new InputRepresentation(
                                message.getInputStream(), contentType);

                        Element body = dom.createElement("body");
                        email.appendChild(head);

                        CDATASection bodyContent = dom
                                .createCDATASection(content.getText());
                        body.appendChild(bodyContent);
                    }
                }

                // Set the result document
                response.setEntity(result);
            }
        }

    }

    /**
     * Handles a SMTP or SMTPS request.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @throws IOException
     * @throws MessagingException
     */
    private void handleSmtp(Request request, Response response)
            throws IOException, MessagingException {
        // Parse the SMTP URI
        String smtpHost = request.getResourceRef().getHostDomain();
        int smtpPort = request.getResourceRef().getHostPort();

        if (smtpPort == -1) {
            // No port specified, the default one should be used
            smtpPort = request.getProtocol().getDefaultPort();
        }

        if ((smtpHost == null) || (smtpHost.equals(""))) {
            throw new IllegalArgumentException("Invalid SMTP host specified");
        }

        // Parse the email to extract necessary info
        DomRepresentation dom = new DomRepresentation(request.getEntity());
        Document email = dom.getDocument();
        Element root = (Element) email.getElementsByTagName("email").item(0);
        Element header = (Element) root.getElementsByTagName("head").item(0);
        String subject = header.getElementsByTagName("subject").item(0)
                .getTextContent();
        String from = header.getElementsByTagName("from").item(0)
                .getTextContent();

        NodeList toList = header.getElementsByTagName("to");
        String[] to = new String[toList.getLength()];
        for (int i = 0; i < toList.getLength(); i++) {
            to[i] = toList.item(i).getTextContent();
        }

        NodeList ccList = header.getElementsByTagName("cc");
        String[] cc = new String[ccList.getLength()];
        for (int i = 0; i < ccList.getLength(); i++) {
            cc[i] = ccList.item(i).getTextContent();
        }

        NodeList bccList = header.getElementsByTagName("bcc");
        String[] bcc = new String[bccList.getLength()];
        for (int i = 0; i < bccList.getLength(); i++) {
            bcc[i] = bccList.item(i).getTextContent();
        }

        String text = root.getElementsByTagName("body").item(0)
                .getTextContent();

        // Check if authentication required
        boolean authenticate = ((getLogin(request) != null) && (getPassword(request) != null));

        String transport = null;

        if (Protocol.SMTP.equals(request.getProtocol())) {
            transport = "smtp";
        } else if (Protocol.SMTPS.equals(request.getProtocol())) {
            transport = "smtps";
        }

        Properties props = System.getProperties();
        props.put("mail." + transport + ".host", smtpHost);
        props.put("mail." + transport + ".port", Integer.toString(smtpPort));
        props.put("mail." + transport + ".auth", Boolean.toString(authenticate)
                .toLowerCase());
        props.put("mail." + transport + ".starttls.enable", Boolean
                .toString(isStartTls()));

        // Open the JavaMail session
        Session session = Session.getDefaultInstance(props);
        session.setDebug(isDebug());
        Transport tr = session.getTransport(transport);

        if (tr != null) {
            // Check if authentication is needed
            if (authenticate) {
                tr.connect(smtpHost, getLogin(request), getPassword(request));
            } else {
                tr.connect();
            }

            // Actually send the message
            if (tr.isConnected()) {
                getLogger()
                        .info(
                                "JavaMail client connection successfully established. Attempting to send the message");

                // Create a new message
                Message msg = new MimeMessage(session);

                // Set the FROM and TO fields
                msg.setFrom(new InternetAddress(from));

                for (String element : to) {
                    msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress(element));
                }

                for (String element : cc) {
                    msg.addRecipient(Message.RecipientType.CC,
                            new InternetAddress(element));
                }

                for (String element : bcc) {
                    msg.addRecipient(Message.RecipientType.BCC,
                            new InternetAddress(element));
                }

                // Set the subject and content text
                msg.setSubject(subject);
                msg.setText(text);
                msg.setSentDate(new Date());
                msg.saveChanges();

                // Send the message
                tr.sendMessage(msg, msg.getAllRecipients());
                tr.close();

                getLogger().info(
                        "JavaMail client successfully sent the message.");
            }
        }
    }

    /**
     * Indicates if the connector should generate JavaMail debug messages.
     * 
     * @return True the connector should generate JavaMail debug messages.
     */
    public boolean isDebug() {
        return Boolean.parseBoolean(getParameters().getFirstValue("debug",
                "false"));
    }

    /**
     * Indicates if the SMTP protocol should attempt to start a TLS tunnel.
     * 
     * @return True if the SMTP protocol should attempt to start a TLS tunnel.
     */
    public boolean isStartTls() {
        return Boolean.parseBoolean(getParameters().getFirstValue("startTls",
                "false"));
    }

}
