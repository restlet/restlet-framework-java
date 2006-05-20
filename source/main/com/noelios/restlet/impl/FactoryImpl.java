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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Chainlet;
import org.restlet.Factory;
import org.restlet.Maplet;
import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.*;

import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.util.Base64;
import com.noelios.restlet.util.FormUtils;

/**
 * Noelios Restlet Engine.<br/>
 * Also acts as a factory implementation.
 */
public class FactoryImpl extends Factory
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.FactoryImpl");

   public static final String VERSION_LONG = "1.0 beta 12";
   public static final String VERSION_SHORT = "1.0b12";
   public static final String VERSION_HEADER = "Noelios-Restlet-Engine/" + VERSION_SHORT;

   /**
    * Map from protocol to client connector class.
    */
   protected Map<Protocol, Class<? extends Client>> clients;

   /**
    * Map from protocol to server connector class.
    */
   protected Map<Protocol, Class<? extends Server>> servers;

   /**
    * Constructor.
    */
   @SuppressWarnings("unchecked")
	public FactoryImpl()
   {
      this.clients = new TreeMap<Protocol, Class<? extends Client>>();
      this.servers = new TreeMap<Protocol, Class<? extends Server>>();

      // Find the factory class name
      String providerName = null;
      String providerClassName = null;

      // Find the factory class name
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL configURL;

      // Register the client connector providers
      try
      {
         for(Enumeration<URL> configUrls = cl.getResources("meta-inf/services/org.restlet.connector.Client"); configUrls.hasMoreElements();)
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
                        java.lang.reflect.Method getMethod = providerClass.getMethod("getProtocols", (Class[])null);
                        List<Protocol> supportedProtocols = (List<Protocol>)getMethod.invoke(null, (Object[])null);

                        for(Protocol protocol : supportedProtocols)
                        {
                           if(!this.clients.containsKey(protocol))
                           {
                              this.clients.put(protocol, providerClass);
                           }
                        }
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
         for(Enumeration<URL> configUrls = cl.getResources("meta-inf/services/org.restlet.connector.Server"); configUrls.hasMoreElements();)
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
                  java.lang.reflect.Method getMethod = providerClass.getMethod("getProtocols", (Class[])null);
                  List<Protocol> supportedProtocols = (List<Protocol>)getMethod.invoke(null, (Object[])null);

                  for(Protocol protocol : supportedProtocols)
                  {
                     if(!this.servers.containsKey(protocol))
                     {
                        this.servers.put(protocol, providerClass);
                     }
                  }
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
      Factory.setInstance(new FactoryImpl());
   }

   /**
    * Create a new client connector for a given protocol.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @return The new client connector.
    */
   public Client createClient(Protocol protocol, String name)
   {
      Client result = null;

      try
      {
         Class<? extends Client> providerClass = this.clients.get(protocol);

         if((providerClass != null) && (protocol != null))
         {
            result = providerClass.getConstructor(Protocol.class, String.class).newInstance(protocol, name);
         }
         else
         {
            logger.log(Level.WARNING, "No client connector supports the " + protocol.getName() + " protocol.");
         }
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Exception while instantiation the client connector.", e);
      }

      return result;
   }

   /**
    * Creates a delegate call.
    * @return A delegate call.
    */
   public Call createCall()
   {
      return new CallImpl();
   }

   /**
    * Creates a delegate Chainlet for internal usage by the AbstractChainlet.<br/>
    * If you need a Chainlet for your application, you should be subclassing the AbstractChainlet instead.
    * @param parent The parent component.
    * @return A new Chainlet.
    */
   public Chainlet createChainlet(Component parent)
   {
      return new ChainletImpl(parent);
   }

   /**
    * Creates a delegate Maplet for internal usage by the DefaultMaplet.<br/>
    * If you need a Maplet for your application, you should be using the DefaultMaplet instead.
    * @param parent The parent component.
    * @return A new Maplet.
    */
   public Maplet createMaplet(Component parent)
   {
      return new MapletImpl(parent);
   }

   /**
    * Create a new server connector for internal usage by the GenericClient.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param delegate The target Server that will provide the actual handle(ServerCall) implementation.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new server connector.
    */
   public Server createServer(Protocol protocol, String name, Server delegate, String address, int port)
   {
      Server result = null;

      try
      {
         Class<? extends Server> providerClass = this.servers.get(protocol);

         if((providerClass != null) && (protocol != null))
         {
            result = providerClass.getConstructor(Protocol.class, String.class, Server.class, String.class, int.class).newInstance(protocol, name, delegate, address, port);
         }
         else
         {
            logger.log(Level.WARNING, "No server connector supports the " + protocol.getName() + " protocol.");
         }
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Exception while instantiation the server connector.", e);
      }

      return result;
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
    * Formats a list of parameters.
    * @param parameters The list of parameters.
    * @return The encoded parameters string.
    */
   public String format(List<Parameter> parameters)
   {
      String result = null;

      try
      {
         result = FormUtils.format(parameters);
      }
      catch (IOException e)
      {
         logger.log(Level.WARNING, "Unexpected error while formating a query string.", e);
      }

      return result;
   }

   /**
    * Parses a post into a given form.
    * @param form The target form.
    * @param post The posted form.
    */
   public void parsePost(Form form, Representation post) throws IOException
   {
      if(post != null)
      {
         FormUtils.parsePost(form, post);
      }
   }

   /**
    * Parses a query into a given form.
    * @param form The target form.
    * @param query Query string.
    */
   public void parseQuery(Form form, String query) throws IOException
   {
      if((query != null) && !query.equals(""))
      {
         FormUtils.parseQuery(form, query);
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

}
