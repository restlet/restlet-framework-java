/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.component.Component;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeSchemes;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.util.Base64;
import com.noelios.restlet.util.FormUtils;

/**
 * Noelios Restlet Engine.<br/>
 * Also acts as a factory implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Factory extends org.restlet.Factory
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(Factory.class.getCanonicalName());

   public static final String VERSION_LONG = Factory.VERSION_LONG;
   public static final String VERSION_SHORT = Factory.VERSION_SHORT;
   public static final String VERSION_HEADER = "Noelios-Restlet-Engine/" + VERSION_SHORT;

   /** List of available client connectors. */
   protected List<Client> clients;

   /** List of available server connectors. */
   protected List<Server> servers;

   /**
    * Constructor.
    */
   @SuppressWarnings("unchecked")
   public Factory()
   {
      this.clients = new ArrayList<Client>();
      this.servers = new ArrayList<Server>();

      // Find the factory class name
      String providerName = null;
      String providerClassName = null;

      // Find the factory class name
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL configURL;

      // Register the client connector providers
      try
      {
         for(Enumeration<URL> configUrls = cl.getResources("META-INF/services/org.restlet.connector.Client"); configUrls.hasMoreElements();)
         {
            configURL = configUrls.nextElement();

            try
            {
               BufferedReader reader = new BufferedReader(new InputStreamReader(configURL.openStream(), "utf-8"));
               providerName = reader.readLine();

               while(providerName != null)
               {
                  providerClassName = providerName.substring(0, providerName.indexOf('#')).trim();

                  if(providerClassName == null)
                  {
                     logger.log(Level.SEVERE, "Unable to process the following connector provider: " + providerName + ". Please check your JAR file metadata.");
                  }
                  else
                  {
                     // Instantiate the factory
                     try
                     {
                        Class<? extends Client> providerClass = (Class<? extends Client>) Class.forName(providerClassName);
                        this.clients.add(providerClass.getConstructor(Component.class, ParameterList.class).newInstance(null, null));
                     }
                     catch(Exception e)
                     {
                        logger.log(Level.SEVERE, "Unable to register the client connector " + providerClassName, e);
                     }
                  }

                  providerName = reader.readLine();
               }
            }
            catch (Exception e)
            {
               logger.log(Level.SEVERE, "Unable to read the provider descriptor: " + configURL.toString());
            }
         }
      }
      catch (IOException ioe)
      {
         logger.log(Level.SEVERE, "Exception while detecting the client connectors.", ioe);
      }

      // Register the server connector providers
      try
      {
         for(Enumeration<URL> configUrls = cl.getResources("META-INF/services/org.restlet.connector.Server"); configUrls.hasMoreElements();)
         {
            configURL = configUrls.nextElement();

            try
            {
               BufferedReader reader = new BufferedReader(new InputStreamReader(configURL.openStream(), "utf-8"));
               providerName = reader.readLine();
               providerClassName = providerName.substring(0, providerName.indexOf('#')).trim();
            }
            catch (Exception e)
            {
               logger.log(Level.SEVERE, "Unable to read the provider descriptor: " + configURL.toString());
            }

            if(providerClassName == null)
            {
               logger.log(Level.SEVERE, "Unable to process the following connector provider: " + providerName + ". Please check your JAR file metadata.");
            }
            else
            {
               // Instantiate the factory
               try
               {
                  Class<? extends Server> providerClass = (Class<? extends Server>) Class.forName(providerClassName);
                  this.servers.add(providerClass.getConstructor(Component.class, ParameterList.class, String.class, int.class).newInstance(null, null, null, new Integer(-1)));
               }
               catch(Exception e)
               {
                  logger.log(Level.SEVERE, "Unable to register the server connector " + providerClassName, e);
               }
            }
         }
      }
      catch (IOException ioe)
      {
         logger.log(Level.SEVERE, "Exception while detecting the client connectors.", ioe);
      }
   }

   /**
    * Registers the Noelios Restlet Engine
    */
   public static void register()
   {
      Factory.setInstance(new Factory());
   }

   /**
    * Create a new client connector for a given protocol.
    * @param protocols The connector protocols.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @return The new client connector.
    */
   public Client createClient(List<Protocol> protocols, Component owner, ParameterList parameters)
   {
     	for(Client client : this.clients)
     	{
        	if(client.getProtocols().containsAll(protocols))
     		{
     	      try
     	      {
     	         return client.getClass().getConstructor(Component.class, ParameterList.class).newInstance(owner, parameters);
     	      }
     	      catch (Exception e)
     	      {
     	         logger.log(Level.SEVERE, "Exception while instantiation the client connector.", e);
     	      }
     			
     			return client;
     		}
     	}

     	logger.log(Level.WARNING, "No available client connector supports the required protocols: " + protocols);
      return null;
   }

   /**
    * Creates a delegate call.
    * @return A delegate call.
    */
   public Call createCall()
   {
      return new DefaultCall();
   }

   /**
    * Create a new server connector for internal usage by the GenericClient.
    * @param protocols The connector protocols.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new server connector.
    */
   public Server createServer(List<Protocol> protocols, Component owner, ParameterList parameters, String address, int port)
   {
     	for(Server server : this.servers)
     	{
     		if(server.getProtocols().containsAll(protocols))
     		{
     	      try
     	      {
     	         return server.getClass().getConstructor(Component.class, ParameterList.class, String.class, int.class).newInstance(owner, parameters, address, port);
     	      }
     	      catch (Exception e)
     	      {
     	         logger.log(Level.SEVERE, "Exception while instantiation the server connector.", e);
     	      }
     		}
     	}

     	// Couldn't find a matching connector
     	StringBuilder sb = new StringBuilder();
     	sb.append("No available server connector supports the required protocols: ");

     	for(Protocol p : protocols)
     	{
     		sb.append(p.getName()).append(" ");
     	}
     	logger.log(Level.WARNING, sb.toString());
     	
      return null;
   }

   /**
    * Creates a string-base representation.
    * @param value The represented string.
    * @param mediaType The representation's media type.
    */
   public Representation createRepresentation(String value, MediaType mediaType)
   {
      return new StringRepresentation(value, mediaType);
   }

   /**
    * Parses an URL encoded Web form.
    * @param form The target form.
    * @param webForm The posted form.
    */
   public void parse(Form form, Representation webForm) throws IOException
   {
      if(webForm != null)
      {
         FormUtils.parsePost(form, webForm);
      }
   }

   /**
    * Parses an URL encoded query string into a given form.
    * @param form The target form.
    * @param queryString Query string.
    */
   public void parse(Form form, String queryString) throws IOException
   {
      if((queryString != null) && !queryString.equals(""))
      {
         FormUtils.parseQuery(form, queryString);
      }
   }

   /**
    * Sets the credentials of a challenge response using a user ID and a password.<br/>
    * @param response The challenge response to set.
    * @param userId The user identifier to use.
    * @param password The user password.
    */
   public void setCredentials(ChallengeResponse response, String userId, String password)
   {
      try
      {
         if(response.getScheme().equals(ChallengeSchemes.HTTP_BASIC))
         {
            String credentials = userId + ':' + password;
            response.setCredentials(Base64.encodeBytes(credentials.getBytes("US-ASCII")));
         }
         else if(response.getScheme().equals(ChallengeSchemes.SMTP_PLAIN))
         {
            String credentials = "^@" + userId + "^@" + password;
            response.setCredentials(Base64.encodeBytes(credentials.getBytes("US-ASCII")));
         }
         else
         {
            throw new IllegalArgumentException("Challenge scheme not supported by this implementation");
         }
      }
      catch(UnsupportedEncodingException e)
      {
         throw new RuntimeException("Unsupported encoding, unable to encode credentials");
      }
   }

   /**
    * Creates a URI-based handler attachment that will score target instance shared by all calls.
    * The score will be proportional to the number of chararacters matched by the pattern, from the start
    * of the context resource path.
    * @param router The parent router.
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
	public Scorer createScorer(Router router, String pattern, Restlet target)
	{
		return new PatternScorer(router, pattern, target);
	}

}
