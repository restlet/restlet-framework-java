/*
 * Copyright 2005-2006 Jerome LOUVEL
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
import org.restlet.Manager;
import org.restlet.Maplet;
import org.restlet.RestletCall;
import org.restlet.Restlet;
import org.restlet.component.Component;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.*;

import com.noelios.restlet.util.Base64;

/**
 * Noelios Restlet Engine.<br/>
 * Also acts as a factory implementation.
 */
public class FactoryImpl implements Factory
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.FactoryImpl");

   public static final String VERSION_LONG = "1.0 beta 10";
   public static final String VERSION_SHORT = "1.0b10";
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
      Manager.registerFactory(new FactoryImpl());
   }

   /**
    * Creates a new uniform call.
    * @return A new uniform call.
    */
   public RestletCall createCall()
   {
      return new RestletCallImpl();
   }

   /**
    * Creates a challenge response for a specific scheme (ex: HTTP BASIC authentication)
    * using a login and a password as the credentials.
    * @param scheme The challenge scheme to use.
    * @param userId The user identifier to use.
    * @param password The user password.
    * @return The challenge response to attach to an uniform call.
    */
   public ChallengeResponse createChallengeResponse(ChallengeScheme scheme, String userId, String password)
   {
      if(scheme.equals(ChallengeSchemes.HTTP_BASIC))
      {
         String credentials = userId + ':' + password;

         try
         {
            return new ChallengeResponseImpl(scheme, Base64.encodeBytes(credentials.getBytes("US-ASCII")));
         }
         catch(UnsupportedEncodingException e)
         {
            throw new RuntimeException("Unsupported encoding, unable to encode credentials");
         }
      }
      else if(scheme.equals(ChallengeSchemes.SMTP_PLAIN))
      {
         String credentials = "^@" + userId + "^@" + password;

         try
         {
            return new ChallengeResponseImpl(scheme, Base64.encodeBytes(credentials.getBytes("US-ASCII")));
         }
         catch(UnsupportedEncodingException e)
         {
            throw new RuntimeException("Unsupported encoding, unable to encode credentials");
         }
      }
      else
      {
         throw new IllegalArgumentException("Challenge scheme not supported by this implementation");
      }
   }

   /**
    * Creates a new character set from its standard name.
    * @param name The standard character set name.
    * @return The new character set.
    */
   public CharacterSet createCharacterSet(String name)
   {
      return (name == null) ? null : new CharacterSetImpl(name);
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
    * Creates a delegate Chainlet for internal usage by the AbstractChainlet.<br/>
    * If you need a Chainlet for your application, you should be subclassing the AbstractChainlet instead. 
    * @param parent The parent component.
    * @return A new Chainlet.
    */
   public Chainlet createDelegateChainlet(Component parent)
   {
      return new ChainletImpl(parent);
   }

   /**
    * Creates a delegate Maplet for internal usage by the DefaultMaplet.<br/>
    * If you need a Maplet for your application, you should be using the DefaultMaplet instead. 
    * @param parent The parent component.
    * @return A new Maplet.
    */
   public Maplet createDelegateMaplet(Component parent)
   {
      return new MapletImpl(parent);
   }

   /**
    * Creates a new encoding from its standard name.
    * @param name The standard encoding name.
    * @return The new encoding.
    */
   public Encoding createEncoding(String name)
   {
      return (name == null) ? null : new EncodingImpl(name);
   }

   /**
    * Creates an empty form.
    * @param query Query string to parse or null for an empty form.
    * @return A new form.
    */
   public Form createForm(String query) throws IOException
   {
      return new FormImpl(query);
   }

   /**
    * Creates a new language from its standard name.
    * @param name The standard language name.
    * @return The new language.
    */
   public Language createLanguage(String name)
   {
      return (name == null) ? null : new LanguageImpl(name);
   }

   /**
    * Creates a new media type from its standard name.
    * @param name The standard media type name.
    * @return The new media type.
    */
   public MediaType createMediaType(String name)
   {
      return (name == null) ? null : new MediaTypeImpl(name);
   }

   /**
    * Creates a new method from its standard name.
    * @param name The standard method name.
    * @return The new method.
    */
   public Method createMethod(String name)
   {
      return (name == null) ? null : new MethodImpl(name);
   }
   
   /**
    * Creates a new representation metadata.
    * @param mediaType The representation mediatype.
    */
   public RepresentationMetadata createRepresentationMetadata(MediaType mediaType)
   {
      return new DefaultRepresentationMetadata(mediaType);
   }

   /**
    * Create a new server connector for a given protocol.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param target The target Restlet.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    * @return The new server connector.
    */
   public Server createServer(Protocol protocol, String name, Restlet target, String address, int port)
   {
      Server result = null;

      try
		{
         Class<? extends Server> providerClass = this.servers.get(protocol);
         
         if((providerClass != null) && (protocol != null))
         {
         	result = providerClass.getConstructor(Protocol.class, String.class, Restlet.class, String.class, int.class).newInstance(protocol, name, target, address, port);
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
    * Creates a new status from its standard code.
    * @param code The standard status code.
    * @return The new status.
    */
   public Status createStatus(int code)
   {
      return new StatusImpl(code);
   }

   /**
    * Creates a new tag.
    * @param name The tag name.
    * @return The new tag.
    */
   public Tag createTag(String name)
   {
      return (name == null) ? null : new TagImpl(name);
   }

}
