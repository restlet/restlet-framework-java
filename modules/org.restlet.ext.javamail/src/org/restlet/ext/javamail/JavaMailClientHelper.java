/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.javamail;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.FetchProfile;
import javax.mail.Flags;
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
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.ClientHelper;
import org.restlet.representation.Representation;
import org.w3c.dom.DOMException;

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
 *    &lt;email href=&quot;/1234&quot;/&gt;
 *    &lt;email href=&quot;/5678&quot;/&gt;
 *    &lt;email href=&quot;/9012&quot;/&gt;
 *    &lt;email href=&quot;/3456&quot;/&gt;
 * &lt;/emails&gt;
 * </pre>
 * 
 * To retrieve an individual email, just add the href attribute at the end of
 * the POP URI, such as: pop://host/1234<br>
 * <br>
 * Here is the list of parameters that are supported. They should be set in the
 * Client's context before it is started:
 * <table>
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
 * <tr>
 * <td>representationMessageClass</td>
 * <td>String</td>
 * <td>null</td>
 * <td>If not null, represents the name of a class that extends the JavaMail
 * "javax.mail.Message" class. This class is able to generate a Message from an
 * XML representation and a JavaMail Session. The constructor must accept a
 * {@link Representation} and a JavaMail Session objects as parameters in this
 * order.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public class JavaMailClientHelper extends ClientHelper {

    /** POP protocol. */
    public static final Protocol POP = new Protocol("pop", "POP",
            "Post Office Protocol", 110);

    /** Basic POP scheme. Based on the USER/PASS commands. */
    public static final ChallengeScheme POP_BASIC = new ChallengeScheme(
            "POP_BASIC", "Basic",
            "Basic POP authentication (USER/PASS commands)");

    /** Digest POP scheme. Based on the APOP command. */
    public static final ChallengeScheme POP_DIGEST = new ChallengeScheme(
            "POP_DIGEST", "Digest", "Digest POP authentication (APOP command)");

    /** POPS protocol (via SSL/TLS socket).. */
    public static final Protocol POPS = new Protocol("pops", "POPS",
            "Post Office Protocol (Secure)", 995);

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public JavaMailClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.SMTP);
        getProtocols().add(Protocol.SMTPS);
        getProtocols().add(POP);
        getProtocols().add(POPS);
    }

    /**
     * Creates a JavaMail message by parsing an XML representation.
     * 
     * @param xmlMessage
     *            The XML message to parse.
     * @param session
     *            The current JavaMail session.
     * @return The created JavaMail message.
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     */
    @SuppressWarnings("unchecked")
    protected Message createMessage(Representation xmlMessage, Session session)
            throws IOException, AddressException, MessagingException {
        final String representationMessageClassName = getRepresentationMessageClass();
        if (representationMessageClassName == null) {
            return new RepresentationMessage(xmlMessage, session);
        }

        try {
            final Class<? extends RepresentationMessage> representationMessageClass = (Class<? extends RepresentationMessage>) Class
                    .forName(representationMessageClassName);
            return representationMessageClass.getConstructor(
                    Representation.class, Session.class).newInstance(
                    xmlMessage, session);
        } catch (Exception e) {
            getLogger().log(
                    Level.SEVERE,
                    "Unable to create a new instance of "
                            + representationMessageClassName, e);
            return new RepresentationMessage(xmlMessage, session);
        }
    }

    /**
     * Creates an XML representation based on a JavaMail message.
     * 
     * @param message
     *            The JavaMail message to format.
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
     *            The list of JavaMail messages to format.
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
     *            The high-level request.
     * @return The login.
     */
    private String getLogin(Request request) {
        if ((request != null) && (request.getChallengeResponse() != null)) {
            return request.getChallengeResponse().getIdentifier();
        }

        return null;
    }

    /**
     * Returns the request password.
     * 
     * @param request
     *            The high-level request.
     * @return The password.
     */
    private String getPassword(Request request) {
        if ((request != null) && (request.getChallengeResponse() != null)) {
            return new String(request.getChallengeResponse().getSecret());
        }

        return null;
    }

    /**
     * Returns the full name of the class used for generating JavaMail Message
     * instances from an XML representation and a JavaMail Session.
     * 
     * @return The full name of the class used for generating JavaMail Message
     *         instances from an XML representation and a JavaMail Session.
     */
    public String getRepresentationMessageClass() {
        return getHelpedParameters()
                .getFirstValue("representationMessageClass");
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            final Protocol protocol = request.getProtocol();

            if (Protocol.SMTP.equals(protocol)
                    || Protocol.SMTPS.equals(protocol)) {
                handleSmtp(request, response);
            } else if (POP.equals(protocol) || POPS.equals(protocol)) {
                handlePop(request, response);
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e.getMessage());
        } catch (NoSuchProviderException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        } catch (AddressException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        } catch (MessagingException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    /**
     * Handles a POP or POPS request.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @throws IOException
     * @throws MessagingException
     * @throws IOException
     */
    private void handlePop(Request request, Response response)
            throws MessagingException, IOException {

        // Parse the POP URI
        final String popHost = request.getResourceRef().getHostDomain();
        int popPort = request.getResourceRef().getHostPort();
        final String path = request.getResourceRef().getPath();

        if (popPort == -1) {
            // No port specified, the default one should be used
            popPort = request.getProtocol().getDefaultPort();
        }

        if ((popHost == null) || (popHost.equals(""))) {
            throw new IllegalArgumentException("Invalid POP host specified");
        }

        // Check if authentication required
        final boolean authenticate = ((getLogin(request) != null) && (getPassword(request) != null));
        final boolean apop = authenticate
                && (POP_DIGEST.equals(request.getChallengeResponse()
                        .getScheme()));

        String transport = null;

        if (POP.equals(request.getProtocol())) {
            transport = "pop3";
        } else if (POPS.equals(request.getProtocol())) {
            transport = "pop3s";
        }

        final Properties props = System.getProperties();
        props.put("mail." + transport + ".host", popHost);
        props.put("mail." + transport + ".port", Integer.toString(popPort));
        props.put("mail." + transport + ".apop.enable", Boolean.toString(apop));

        // States whether or not to update the folder by removing deleted
        // messages.
        boolean updateFolder = false;

        final Session session = Session.getDefaultInstance(props);
        session.setDebug(isDebug());
        final Store store = session.getStore(transport);
        store.connect(getLogin(request), getPassword(request));
        final POP3Folder inbox = (POP3Folder) store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        final FetchProfile profile = new FetchProfile();
        profile.add(UIDFolder.FetchProfileItem.UID);
        final Message[] messages = inbox.getMessages();
        inbox.fetch(messages, profile);

        if ((path == null) || path.equals("") || path.equals("/")) {
            if (Method.GET.equals(request.getMethod())
                    || Method.HEAD.equals(request.getMethod())) {
                // Set the result document
                response.setEntity(createRepresentation(messages, inbox));
            } else {
                response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                response.getAllowedMethods().add(Method.GET);
                response.getAllowedMethods().add(Method.HEAD);
            }
        } else if (path.startsWith("/")) {
            // Retrieve the specified message
            final String mailUid = path.substring(1);
            Message message = null;

            for (int i = 0; (message == null) && (i < messages.length); i++) {
                final String uid = inbox.getUID(messages[i]);

                if (mailUid.equals(uid)) {
                    message = messages[i];
                }
            }

            if (message == null) {
                // Message not found
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                        "No message matches the given UID: " + mailUid);
            } else {
                if (Method.GET.equals(request.getMethod())
                        || Method.HEAD.equals(request.getMethod())) {
                    // Set the result document
                    response.setEntity(createRepresentation(message));
                } else if (Method.DELETE.equals(request.getMethod())) {
                    message.setFlag(Flags.Flag.DELETED, true);
                    updateFolder = true;
                } else {
                    response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                    response.getAllowedMethods().add(Method.GET);
                    response.getAllowedMethods().add(Method.HEAD);
                    response.getAllowedMethods().add(Method.DELETE);
                }
            }
        }

        inbox.close(updateFolder);
        store.close();
    }

    /**
     * Handles a SMTP or SMTPS request.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
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
            final String smtpHost = request.getResourceRef().getHostDomain();
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
            final boolean authenticate = ((getLogin(request) != null) && (getPassword(request) != null));
            String transport = null;

            if (Protocol.SMTP.equals(request.getProtocol())) {
                transport = "smtp";
            } else if (Protocol.SMTPS.equals(request.getProtocol())) {
                transport = "smtps";
            }

            final Properties props = System.getProperties();
            props.put("mail." + transport + ".host", smtpHost);
            props
                    .put("mail." + transport + ".port", Integer
                            .toString(smtpPort));
            props.put("mail." + transport + ".auth", Boolean.toString(
                    authenticate).toLowerCase());
            props.put("mail." + transport + ".starttls.enable", Boolean
                    .toString(isStartTls()));

            // Open the JavaMail session
            final Session session = Session.getDefaultInstance(props);
            session.setDebug(isDebug());
            final Transport tr = session.getTransport(transport);

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
                    final Message msg = createMessage(request.getEntity(),
                            session);

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
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "debug", "false"));
    }

    /**
     * Indicates if the SMTP protocol should attempt to start a TLS tunnel.
     * 
     * @return True if the SMTP protocol should attempt to start a TLS tunnel.
     */
    public boolean isStartTls() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "startTls", "false"));
    }

}
