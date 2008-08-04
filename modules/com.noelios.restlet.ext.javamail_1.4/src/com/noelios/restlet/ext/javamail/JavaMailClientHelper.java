/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.javamail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.noelios.restlet.ClientHelper;
import com.noelios.restlet.Engine;

/**
 * Client connector to a mail server. Currently only the SMTP protocol is
 * supported. To send an email, specify a SMTP URI as the ressource reference of
 * the call and use an XML email as the content of the call. An SMTP URI has the
 * following syntax: smtp://host[:port]<br/> <br/> The default port used is 25
 * for SMTP and 465 for SMTPS. Use the Call.getSecurity().setLogin() and
 * setPassword() methods for authentication.<br/> <br/> Sample XML email:<br/>
 * {@code <?xml version="1.0" encoding="ISO-8859-1" ?>}<br/> {@code <email>}<br/>
 * &nbsp;&nbsp;{@code   <head>}<br/> &nbsp;&nbsp;&nbsp;&nbsp;{@code      <subject>Account activation</subject>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code      <from>support@restlet.org</from>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code      <to>user@domain.com</to>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code      <cc>log@restlet.org</cc>}<br/>
 * &nbsp;&nbsp;{@code   </head>}<br/> &nbsp;&nbsp;{@code   <body><![CDATA[Your account was sucessfully created!]]></body>}<br/>
 * {@code </email>}
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class JavaMailClientHelper extends ClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public JavaMailClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.SMTP);
        getProtocols().add(Protocol.SMTP_STARTTLS);
        getProtocols().add(Protocol.SMTPS);
    }

    /**
     * Creates a high-level request.
     * 
     * @param smtpURI
     *            The SMTP server's URI (ex: smtp://localhost).
     * @param email
     *            The email to send (valid XML email).
     * @param login
     *            Authenticate using this login name.
     * @param password
     *            Authenticate using this password.
     */
    public static Request create(String smtpURI, Representation email,
            String login, String password) {
        Request result = create(smtpURI, email);
        result.getAttributes().put("login", login);
        result.getAttributes().put("password", password);
        return result;
    }

    /**
     * Creates a high-level request.
     * 
     * @param smtpURI
     *            The SMTP server's URI (ex: smtp://localhost).
     * @param email
     *            The email to send (valid XML email).
     */
    public static Request create(String smtpURI, Representation email) {
        Request result = new Request();
        result.getClientInfo().setAgent(Engine.VERSION_HEADER);
        result.setMethod(Method.POST);
        result.setResourceRef(smtpURI);
        result.setEntity(email);
        return result;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
	@Override
    public void handle(Request request, Response response) {
        try {
            // Parse the SMTP URI
            URI smtpURI = new URI(request.getResourceRef().toString());
            String smtpHost = smtpURI.getHost();
            int smtpPort = smtpURI.getPort();
            // String smtpUserInfo = smtpURI.getUserInfo();

            Protocol defaultProtocol = getClient().getProtocols().get(0);

            if (defaultProtocol != null) {
                if (smtpPort == -1) {
                    if ((defaultProtocol.equals(Protocol.SMTP))
                            || (defaultProtocol.equals(Protocol.SMTP_STARTTLS))) {
                        // Use the default SMTP port
                        smtpPort = 25;
                    } else if (defaultProtocol.equals(Protocol.SMTPS)) {
                        smtpPort = 465;
                    }
                }

                if ((smtpHost == null) || (smtpHost.equals(""))) {
                    throw new IllegalArgumentException(
                            "Invalid SMTP host specified");
                }

                // Parse the email to extract necessary info
                DocumentBuilder docBuilder = DocumentBuilderFactory
                        .newInstance().newDocumentBuilder();
                Document email = docBuilder.parse(request.getEntity()
                        .getStream());

                Element root = (Element) email.getElementsByTagName("email")
                        .item(0);
                Element header = (Element) root.getElementsByTagName("head")
                        .item(0);
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

                // Prepare the connection to the SMTP server
                Session session = null;
                Transport transport = null;
                Properties props = System.getProperties();

                // Check if authentication required
                boolean authenticate = ((getLogin(request) != null) && (getPassword(request) != null));

                // Connect to the SMTP server
                if (defaultProtocol.equals(Protocol.SMTP)
                        || defaultProtocol.equals(Protocol.SMTP_STARTTLS)) {
                    props.put("mail.smtp.host", smtpHost);
                    props.put("mail.smtp.port", Integer.toString(smtpPort));
                    props.put("mail.smtp.auth", Boolean.toString(authenticate)
                            .toLowerCase());
                    props.put("mail.smtp.starttls.enable", Boolean.toString(
                            getClient().getProtocols().get(0).equals(
                                    Protocol.SMTP_STARTTLS)).toLowerCase());
                    session = Session.getDefaultInstance(props);
                    // session.setDebug(true);
                    transport = session.getTransport("smtp");
                } else if (defaultProtocol.equals(Protocol.SMTPS)) {
                    props.put("mail.smtps.host", smtpHost);
                    props.put("mail.smtps.port", Integer.toString(smtpPort));
                    props.put("mail.smtps.auth", Boolean.toString(authenticate)
                            .toLowerCase());
                    session = Session.getDefaultInstance(props);
                    // session.setDebug(true);
                    transport = session.getTransport("smtps");
                }

                if (transport != null) {
                    // Check if authentication is needed
                    if (authenticate) {
                        transport.connect(smtpHost, getLogin(request),
                                getPassword(request));
                    } else {
                        transport.connect();
                    }

                    // Actually send the message
                    if (transport.isConnected()) {
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
                        transport.sendMessage(msg, msg.getAllRecipients());
                        transport.close();

                        getLogger()
                                .info(
                                        "JavaMail client successfully sent the message.");
                    }
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
        } catch (NoSuchProviderException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
        } catch (AddressException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
        } catch (MessagingException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
        } catch (SAXException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
        } catch (URISyntaxException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
        } catch (ParserConfigurationException e) {
            getLogger().log(Level.WARNING, "JavaMail client error", e);
        }

    }

    /**
     * Returns the login stored as an attribute.
     * 
     * @param request
     *            The high-level request.
     * @return The high-level request.
     */
    private String getLogin(Request request) {
        return (String) request.getAttributes().get("login");
    }

    /**
     * Returns the password stored as an attribute.
     * 
     * @param request
     *            The high-level request.
     * @return The high-level request.
     */
    private String getPassword(Request request) {
        return (String) request.getAttributes().get("password");
    }

}
