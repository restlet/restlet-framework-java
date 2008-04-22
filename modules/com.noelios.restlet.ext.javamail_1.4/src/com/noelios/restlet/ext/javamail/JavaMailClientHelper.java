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
import java.util.Properties;
import java.util.logging.Level;

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

import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.w3c.dom.DOMException;

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
     * Creates a JavaMail message by parsing an XML representation.
     * 
     * @param xmlMessage
     *                The XML message to parse.
     * @param session
     *                The current JavaMail session.
     * @return The created JavaMail message.
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     */
    protected Message createMessage(Representation xmlMessage, Session session)
            throws IOException, AddressException, MessagingException {
        return new RepresentationMessage(xmlMessage, session);
    }

    /**
     * Creates an XML representation based on a JavaMail message.
     * 
     * @param message
     *                The JavaMail message to format.
     * @return The XML representation.
     * @throws DOMException
     * @throws IOException
     * @throws MessagingException
     */
    protected Representation createRepresentation(Message message)
            throws DOMException, IOException, MessagingException {
        return new MessageRepresentation(message);
    }

    /**
     * Creates an XML representation based on a list of JavaMail messages.
     * 
     * @param messages
     *                The list of JavaMail messages to format.
     * @return The XML representation.
     * @throws IOException
     * @throws MessagingException
     */
    protected Representation createRepresentation(Message[] messages,
            POP3Folder inbox) throws IOException, MessagingException {
        return new MessagesRepresentation(messages, inbox);
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
            // Set the result document
            response.setEntity(createRepresentation(messages, inbox));
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
                // Set the result document
                response.setEntity(createRepresentation(message));
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
        if (!Method.POST.equals(request.getMethod())) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.getAllowedMethods().add(Method.POST);
        } else {
            // Parse the SMTP URI
            String smtpHost = request.getResourceRef().getHostDomain();
            int smtpPort = request.getResourceRef().getHostPort();

            if (smtpPort == -1) {
                // No port specified, the default one should be used
                smtpPort = request.getProtocol().getDefaultPort();
            }

            if ((smtpHost == null) || (smtpHost.equals(""))) {
                throw new IllegalArgumentException(
                        "Invalid SMTP host specified");
            }

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
            props
                    .put("mail." + transport + ".port", Integer
                            .toString(smtpPort));
            props.put("mail." + transport + ".auth", Boolean.toString(
                    authenticate).toLowerCase());
            props.put("mail." + transport + ".starttls.enable", Boolean
                    .toString(isStartTls()));

            // Open the JavaMail session
            Session session = Session.getDefaultInstance(props);
            session.setDebug(isDebug());
            Transport tr = session.getTransport(transport);

            if (tr != null) {
                // Check if authentication is needed
                if (authenticate) {
                    tr.connect(smtpHost, getLogin(request),
                            getPassword(request));
                } else {
                    tr.connect();
                }

                // Actually send the message
                if (tr.isConnected()) {
                    getLogger()
                            .info(
                                    "JavaMail client connection successfully established. Attempting to send the message");

                    // Create the JavaMail message
                    Message msg = createMessage(request.getEntity(), session);

                    // Send the message
                    tr.sendMessage(msg, msg.getAllRecipients());
                    tr.close();

                    getLogger().info(
                            "JavaMail client successfully sent the message.");
                }
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
