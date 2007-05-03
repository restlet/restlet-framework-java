/*
 * Copyright 2005-2006 Jérôme LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.javamail;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.connector.AbstractClient;
import org.restlet.connector.ClientCall;
import org.restlet.data.Methods;
import org.restlet.data.Parameter;
import org.restlet.data.Protocols;
import org.restlet.data.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.noelios.restlet.impl.FactoryImpl;

/**
 * Client connector to a mail server.<br/>
 * Currently only the SMTP protocol is supported.<br/>
 * To send an email, specify a SMTP URI as the ressource reference of the call and use an XML
 * email as the content of the call.<br/>
 * An SMTP URI has the following syntax: smtp://[user-info@]host[:port]<br/>
 * The default port used is 25 and user-info for authentication is currently not supported.<br/>
 * <br/>
 * Sample XML email:<br/>
 * <br/>
 * {@code <?xml version="1.0" encoding="ISO-8851-1" ?>}<br/>
 * {@code <email>}<br/> &nbsp;&nbsp;{@code   <head>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code      <subject>Account activation</subject>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code      <from>support@restlet.org</from>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code      <to>user@domain.com</to>}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@code      <cc>log@restlet.org</cc>}<br/>
 * &nbsp;&nbsp;{@code   </head>}<br/>
 * &nbsp;&nbsp;{@code   <body><![CDATA[Your account was sucessfully created!]]></body>}<br/>
 * {@code </email>}
 */
public class JavaMailClient extends AbstractClient
{
   /**
    * Constructor.
    * @param name The unique connector name.
    */
   public JavaMailClient(String name)
   {
      super(Protocols.SMTP, name);
   }
   
   /**
    * Returns a new client call.
    * @param method The request method.
    * @param resourceUri The requested resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @return A new client call.
    */
   public ClientCall createCall(String method, String resourceUri, boolean hasInput)
   {
      return null;
   }
   
   /**
    * Creates an uniform call.
    * @param smtpURI The SMTP server's URI (ex: smtp://localhost).
    * @param email The email to send (valid XML email).
    */
   public static UniformCall create(String smtpURI, Representation email)
   {
      UniformCall result = Manager.createCall();
      result.setClientName(FactoryImpl.VERSION_HEADER);
      result.setMethod(Methods.POST);
      result.setResourceRef(Manager.createReference(smtpURI));
      result.setInput(email);
      return result;
   }

   /**
    * Handles a REST call.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      try
      {
         // Parse the SMTP URI
         URI smtpURI = new URI(call.getResourceRef().toString());
         String smtpHost = smtpURI.getHost();
         int smtpPort = smtpURI.getPort();
         // String smtpUserInfo = smtpURI.getUserInfo();

         if(smtpPort == -1)
         {
            // Use the default SMTP port
            smtpPort = 25;
         }

         if((smtpHost == null) || (smtpHost.equals("")))
         {
            throw new IllegalArgumentException("Invalid SMTP host specified");
         }

         // Parse the email to extract necessary info
         DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document email = docBuilder.parse(call.getInput().getStream());

         Element root = (Element)email.getElementsByTagName("email").item(0);
         Element header = (Element)root.getElementsByTagName("head").item(0);
         String subject = header.getElementsByTagName("subject").item(0).getTextContent();
         String from = header.getElementsByTagName("from").item(0).getTextContent();

         NodeList toList = header.getElementsByTagName("to");
         String[] to = new String[toList.getLength()];
         for(int i = 0; i < toList.getLength(); i++)
         {
            to[i] = toList.item(i).getTextContent();
         }

         NodeList ccList = header.getElementsByTagName("cc");
         String[] cc = new String[ccList.getLength()];
         for(int i = 0; i < ccList.getLength(); i++)
         {
            cc[i] = ccList.item(i).getTextContent();
         }

         NodeList bccList = header.getElementsByTagName("bcc");
         String[] bcc = new String[bccList.getLength()];
         for(int i = 0; i < bccList.getLength(); i++)
         {
            bcc[i] = bccList.item(i).getTextContent();
         }

         String text = root.getElementsByTagName("body").item(0).getTextContent();

         // Prepare the connection to the SMTP server
         Properties props = System.getProperties();
         props.put("mail.smtp.host", smtpHost);
         props.put("mail.smtp.port", Integer.toString(smtpPort));
         props.put("mail.smtp.auth", "false");
         // props.put("mail.smtp.auth", "true");
         // props.put("mail.smtp.starttls.enable", "true");
         // props.put("mail.smtp.debug", "true");
         // props.put("mail.smtp.reportsuccess", "true");
         // props.put("mail.smtp.ehlo", Boolean.FALSE);

         // Connect to the SMTP server
         Session session = Session.getDefaultInstance(props);
         Transport transport = session.getTransport("smtp");
         transport.connect();
         // transport.connect(smtpHost, "user", "pwd");
         
         if(transport.isConnected())
         {
            // Create a new message
            Message msg = new MimeMessage(session);

            // Set the FROM and TO fields
            msg.setFrom(new InternetAddress(from));

            for(int i = 0; i < to.length; i++)
            {
               msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
            }

            for(int i = 0; i < cc.length; i++)
            {
               msg.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
            }

            for(int i = 0; i < bcc.length; i++)
            {
               msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc[i]));
            }

            // Set the subject and content text
            msg.setSubject(subject);
            msg.setText(text);
            
            // Add the custom headers that may have been set by the user
            Parameter customHeader;
            for(Iterator<Parameter> iter = call.getConnectorCall().getRequestHeaders().iterator(); iter.hasNext();)
            {
               customHeader = iter.next();
               msg.addHeader(customHeader.getName(), customHeader.getValue());
            }         

            msg.setSentDate(new Date());
            msg.saveChanges();

            // Send the message
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }

   }

}
