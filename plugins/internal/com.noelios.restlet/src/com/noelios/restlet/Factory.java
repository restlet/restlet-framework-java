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

package com.noelios.restlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
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
import org.restlet.data.ClientData;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.LanguagePref;
import org.restlet.data.Languages;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypePref;
import org.restlet.data.MediaTypes;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;
import org.restlet.data.Resource;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.util.Base64;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.FormUtils;

/**
 * Factory for the Noelios Restlet Engine.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Factory extends org.restlet.Factory
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(Factory.class.getCanonicalName());

   public static final String VERSION_LONG = org.restlet.Factory.VERSION_LONG;
   public static final String VERSION_SHORT = org.restlet.Factory.VERSION_SHORT;
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
      String line = null;
      String provider = null;

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
               line = reader.readLine();

               while(line != null)
               {
                  provider = getProviderClassName(line);

                  if((provider != null) && (!provider.equals("")))
                  {
                     // Instantiate the factory
                     try
                     {
                        Class<? extends Client> providerClass = (Class<? extends Client>) Class.forName(provider);
                        this.clients.add(providerClass.getConstructor(Component.class, ParameterList.class).newInstance(null, null));
                     }
                     catch(Exception e)
                     {
                        logger.log(Level.SEVERE, "Unable to register the client connector " + provider, e);
                     }
                  }

                  line = reader.readLine();
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
               line = reader.readLine();

               while(line != null)
               {
                  provider = getProviderClassName(line);

                  if((provider != null) && (!provider.equals("")))
                  {
                     // Instantiate the factory
                     try
                     {
                        Class<? extends Server> providerClass = (Class<? extends Server>) Class.forName(provider);
                        this.servers.add(providerClass.getConstructor(Component.class, ParameterList.class, String.class, int.class).newInstance(null, null, null, new Integer(-1)));
                     }
                     catch(Exception e)
                     {
                        logger.log(Level.SEVERE, "Unable to register the server connector " + provider, e);
                     }
                  }

                  line = reader.readLine();
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
   }

   /**
    * Parses a line to extract the provider class name.
    * @param line The line to parse.
    * @return The provider's class name or an empty string.
    */
   private String getProviderClassName(String line)
   {
   	int index = line.indexOf('#');
   	if(index != -1) line = line.substring(0, index);
      return line.trim();
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

   /**
    * Returns the best variant representation for a given resource according the the client preferences.
    * @param client The client preferences.
    * @param variants The list of variants to compare.
    * @param fallbackLanguage The language to use if no preference matches.
    * @return The best variant representation.
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public Representation getBestVariant(ClientData client, List<Representation> variants, Language fallbackLanguage)
   {
      if(variants == null)
      {
         return null;
      }
      else
      {
         Parameter currentParam = null;
         Language currentLanguage = null;
         Language variantLanguage = null;
         MediaType currentMediaType = null;
         MediaType variantMediaType = null;

         boolean compatiblePref = false;
         boolean compatibleLanguage = false;
         boolean compatibleMediaType = false;

         Representation currentVariant = null;
         Representation bestVariant = null;

         LanguagePref currentLanguagePref = null;
         LanguagePref bestLanguagePref = null;
         MediaTypePref currentMediaTypePref = null;
         MediaTypePref bestMediaTypePref = null;

         float bestQuality = 0;
         float currentScore = 0;
         float bestLanguageScore = 0;
         float bestMediaTypeScore = 0;

         // For each available variant, we will compute the negotiation score
         // which is dependant on the language score and on the media type score
         for(Iterator iter1 = variants.iterator(); iter1.hasNext();)
         {
            currentVariant = (Representation)iter1.next();
            variantLanguage = currentVariant.getLanguage();
            variantMediaType = currentVariant.getMediaType();

            // If no language preference is defined, assume that all languages are acceptable 
            List<LanguagePref> languagePrefs = client.getLanguagePrefs();
            if(languagePrefs.size() == 0) languagePrefs.add(new LanguagePref(Languages.ALL));
            
            // For each language preference defined in the call
            // Calculate the score and remember the best scoring preference
            for(Iterator<LanguagePref> iter2 = languagePrefs.iterator(); (variantLanguage != null) && iter2.hasNext();)
            {
               currentLanguagePref = iter2.next();
               currentLanguage = currentLanguagePref.getLanguage();
               compatiblePref = true;
               currentScore = 0;

               // 1) Compare the main tag
               if(variantLanguage.getMainTag().equals(currentLanguage.getMainTag()))
               {
                  currentScore += 100;
               }
               else if(!currentLanguage.getMainTag().equals("*"))
               {
                  compatiblePref = false;
               }
               else if(currentLanguage.getSubTag() != null)
               {
                  // Only "*" is an acceptable language range
                  compatiblePref = false;
               }
               else
               {
                  // The valid "*" range has the lowest valid score
                  currentScore++;
               }

               if(compatiblePref)
               {
                  // 2) Compare the sub tags
                  if((currentLanguage.getSubTag() == null) || (variantLanguage.getSubTag() == null))
                  {
                     if(variantLanguage.getSubTag() == currentLanguage.getSubTag())
                     {
                        currentScore += 10;
                     }
                     else
                     {
                        // Don't change the score
                     }
                  }
                  else if(currentLanguage.getSubTag().equals(variantLanguage.getSubTag()))
                  {
                     currentScore += 10;
                  }
                  else
                  {
                     // SubTags are different
                     compatiblePref = false;
                  }

                  // 3) Do we have a better preference?
                  // currentScore *= currentPref.getQuality();
                  if(compatiblePref && ((bestLanguagePref == null) || (currentScore > bestLanguageScore)))
                  {
                     bestLanguagePref = currentLanguagePref;
                     bestLanguageScore = currentScore;
                  }
               }
            }

            // Are the preferences compatible with the current variant language?
            compatibleLanguage = (variantLanguage == null) ||
                                 (bestLanguagePref != null) ||
                                 (variantLanguage.equals(fallbackLanguage));

            // If no media type preference is defined, assume that all media types are acceptable 
            List<MediaTypePref> mediaTypePrefs = client.getMediaTypePrefs();
            if(mediaTypePrefs.size() == 0) mediaTypePrefs.add(new MediaTypePref(MediaTypes.ALL));

            // For each media range preference defined in the call
            // Calculate the score and remember the best scoring preference
            for(Iterator<MediaTypePref> iter2 = mediaTypePrefs.iterator(); compatibleLanguage && iter2.hasNext();)
            {
               currentMediaTypePref = iter2.next();
               currentMediaType = currentMediaTypePref.getMediaType();
               compatiblePref = true;
               currentScore = 0;

               // 1) Compare the main types
               if(currentMediaType.getMainType().equals(variantMediaType.getMainType()))
               {
                  currentScore += 1000;
               }
               else if(!currentMediaType.getMainType().equals("*"))
               {
                  compatiblePref = false;
               }
               else if(!currentMediaType.getSubType().equals("*"))
               {
                  // Ranges such as "*/html" are not supported
                  // Only "*/*" is acceptable in this case
                  compatiblePref = false;
               }

               if(compatiblePref)
               {
                  // 2) Compare the sub types
                  if(variantMediaType.getSubType().equals(currentMediaType.getSubType()))
                  {
                     currentScore += 100;
                  }
                  else if(!currentMediaType.getSubType().equals("*"))
                  {
                     // Subtype are different
                     compatiblePref = false;
                  }

                  if(compatiblePref && (variantMediaType.getParameters() != null))
                  {
                     // 3) Compare the parameters
                     // If current media type is compatible with the current
                     // media range then the parameters need to be checked too
                     for(Iterator iter3 = variantMediaType.getParameters().iterator(); iter3
                           .hasNext();)
                     {
                        currentParam = (Parameter)iter3.next();

                        if(isParameterFound(currentParam, currentMediaType))
                        {
                           currentScore++;
                        }
                     }
                  }

                  // 3) Do we have a better preference?
                  // currentScore *= currentPref.getQuality();
                  if(compatiblePref && ((bestMediaTypePref == null) || (currentScore > bestMediaTypeScore)))
                  {
                     bestMediaTypePref = currentMediaTypePref;
                     bestMediaTypeScore = currentScore;
                  }
               }
            }

            // Are the preferences compatible with the current media type?
            compatibleMediaType = (variantMediaType == null) ||
                                  (bestMediaTypePref != null);

            if(compatibleLanguage && compatibleMediaType)
            {
               // Do we have a compatible media type?
               float currentQuality = 0;
               if(bestLanguagePref != null)
               {
                  currentQuality += (bestLanguagePref.getQuality() * 10F);
               }
               else if((variantLanguage != null) && variantLanguage.equals(fallbackLanguage))
               {
                  currentQuality += 0.1F * 10F;
               }

               if(bestMediaTypePref != null)
               {
                  // So, let's conclude on the current variant, its quality
                  currentQuality += bestMediaTypePref.getQuality();
               }

               if(bestVariant == null)
               {
                  bestVariant = currentVariant;
                  bestQuality = currentQuality;
               }
               else if(currentQuality > bestQuality)
               {
                  bestVariant = currentVariant;
                  bestQuality = currentQuality;
               }
            }

            // Reset the preference variables
            bestLanguagePref = null;
            bestLanguageScore = 0;
            bestMediaTypePref = null;
            bestMediaTypeScore = 0;
         }

         return bestVariant;
      }
   }

   /**
    * Indicates if the searched parameter is specified in the given media range.
    * @param searchedParam The searched parameter.
    * @param mediaRange The media range to inspect.
    * @return True if the searched parameter is specified in the given media range.
    */
   private boolean isParameterFound(Parameter searchedParam, MediaType mediaRange)
   {
      boolean result = false;

      for(Iterator iter = mediaRange.getParameters().iterator(); !result && iter.hasNext();)
      {
         result = searchedParam.equals((Parameter)iter.next());
      }

      return result;
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
    * Sets the best representation of a given resource according to the client preferences.<br/>
    * If no representation is found, sets the status to "Not found".<br/>
    * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public void setOutput(Call call, Resource resource, Language fallbackLanguage)
   {
      List<Representation> variants = resource.getVariants();

      if((variants == null) || (variants.size() < 1))
      {
         // Resource not found
      	call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
      else
      {
         // Compute the best variant
      	Representation bestVariant = call.getClient().getBestVariant(variants, fallbackLanguage);

         if(bestVariant == null)
         {
            // No variant was found matching the call preferences
            call.setStatus(Statuses.CLIENT_ERROR_NOT_ACCEPTABLE);
         }
         else
         {
            // Was the representation modified since the last client call?
            Date modifiedSince = (call.getCondition() == null) ? null : call.getCondition().getModifiedSince();
            if((modifiedSince == null) || DateUtils.after(modifiedSince, bestVariant.getModificationDate()))
            {
               // Yes, set the best representation as the call output
            	call.setOutput(bestVariant);
            	call.setStatus(Statuses.SUCCESS_OK);
            }
            else
            {
               // No, indicates it to the client
            	call.setStatus(Statuses.REDIRECTION_NOT_MODIFIED);
            }
         }
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
