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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.data.Methods;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.PreferenceUtils;

/**
 * Chainlet extracting some attributes from a call.
 * Multiple extractions can be defined, based on the query part of the resource reference, 
 * on the input form (posted from a browser), on the context matches or on the call's template model.  
 */
public class TunnelChainlet extends ExtractChainlet
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.TunnelChainlet");

   /** Indicates if the method name can be tunneled. */
   protected boolean methodTunnel;

   /** The name of the attribute containing the method name. */
   protected String methodAttribute;
   
   /** Indicates if the client preferences can be tunneled. */
   protected boolean preferencesTunnel;
   
   /** The name of the attribute containing the accepted character sets. */
   protected String characterSetsAttribute;
   
   /** The name of the attribute containing the accepted encodings. */
   protected String encodingsAttribute;
   
   /** The name of the attribute containing the accepted languages. */
   protected String languagesAttribute;
   
   /** The name of the attribute containing the accepted media types. */
   protected String mediaTypesAttribute;
   
   /** Indicates if the resource reference can be tunneled. */
   protected boolean uriTunnel;

   /** The name of the attribute containing the resource reference. */
   protected String uriAttribute;

   /**
    * Constructor.
    * @param parent The parent component.
    * @param methodTunnel Indicates if the method name can be tunneled.
    * @param preferencesTunnel Indicates if the client preferences can be tunneled.
    * @param uriTunnel Indicates if the resource reference can be tunneled.
    */
   public TunnelChainlet(Component parent, boolean methodTunnel, boolean preferencesTunnel, boolean uriTunnel)
   {
      super(parent);
      this.methodTunnel = methodTunnel;
      this.methodAttribute= "method";
      this.preferencesTunnel = preferencesTunnel;
      this.characterSetsAttribute = "charset";
      this.encodingsAttribute = "encoding";
      this.languagesAttribute = "language";
      this.mediaTypesAttribute = "media";
      this.uriTunnel = uriTunnel;
      this.uriAttribute = "uri";
   }
   
   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      try
      {
   		extractAttributes(call);
   		tunnel(call);
         super.handle(call);
      }
      catch(Exception e)
      {
         logger.log(Level.SEVERE, "Unhandled error intercepted", e);
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
      }
   }

   /**
    * Tunnels the extracted attributes into the proper call objects.
    * @param call The call to update.
    */
   protected void tunnel(Call call)
   {
   	if(isMethodTunnel())
   	{
   		String methodName = (String)call.getAttributes().get(getMethodAttribute());
   		
   		if(methodName != null)
   		{
   			call.setMethod(Methods.create(methodName));
   		}
   	}
   	
   	if(isPreferencesTunnel())
   	{
         // Extract the header values
         String acceptCharset = (String)call.getAttributes().get(getCharacterSetsAttribute());
         String acceptEncoding = (String)call.getAttributes().get(getCharacterSetsAttribute());
         String acceptLanguage = (String)call.getAttributes().get(getCharacterSetsAttribute());
         String acceptMediaType = (String)call.getAttributes().get(getCharacterSetsAttribute());

         // Parse the headers and update the call preferences
         if(acceptCharset != null)
         {
         	call.getPreference().getCharacterSets().clear();
         	PreferenceUtils.parseCharacterSets(acceptCharset, call.getPreference());
         }
         
         if(acceptEncoding != null)
         {
         	call.getPreference().getEncodings().clear();
         	PreferenceUtils.parseEncodings(acceptEncoding, call.getPreference());
         }
         
         if(acceptLanguage != null)
         {
         	call.getPreference().getLanguages().clear();
         	PreferenceUtils.parseLanguages(acceptLanguage, call.getPreference());
         }
         
         if(acceptMediaType != null)
         {
         	call.getPreference().getMediaTypes().clear();
         	PreferenceUtils.parseMediaTypes(acceptMediaType, call.getPreference());
         }
   	}
   	
   	if(isUriTunnel())
   	{
   		String uri = (String)call.getAttributes().get(getUriAttribute());
   		
   		if(uri != null)
   		{
   			call.setResourceRef(uri);
   		}
   	}
   }
   
	/**
	 * Indicates if the method name can be tunneled.
	 * @return True if the method name can be tunneled.
	 */
	public boolean isMethodTunnel()
	{
		return this.methodTunnel;
	}

	/**
	 * Indicates if the method name can be tunneled.
	 * @param methodTunnel True if the method name can be tunneled.
	 */
	public void setMethodTunnel(boolean methodTunnel)
	{
		this.methodTunnel = methodTunnel;
	}

	/**
	 * Returns the method attribute name.
	 * @return The method attribute name.
	 */
	public String getMethodAttribute()
	{
		return this.methodAttribute;
	}

	/**
	 * Returns the method attribute name.
	 * @return The method attribute name.
	 */
	public void setMethodAttribute(String attributeName)
	{
		this.methodAttribute = attributeName;
	}

	/**
	 * Returns the character sets attribute name.
	 * @return The character sets attribute name.
	 */
	public String getCharacterSetsAttribute()
	{
		return this.characterSetsAttribute;
	}

	/**
	 * Sets the character sets attribute name.
	 * @param attributeName The character sets attribute name.
	 */
	public void setCharacterSetsAttribute(String attributeName)
	{
		this.characterSetsAttribute = attributeName;
	}

	/**
	 * Indicates if the client preferences can be tunneled.
	 * @return True if the client preferences can be tunneled.
	 */
	public boolean isPreferencesTunnel()
	{
		return this.preferencesTunnel;
	}

	/**
	 * Indicates if the client preferences can be tunneled.
	 * @param preferencesTunnel True if the client preferences can be tunneled.
	 */
	public void setPreferencesTunnel(boolean preferencesTunnel)
	{
		this.preferencesTunnel = preferencesTunnel;
	}

	/**
	 * Returns the name of the attribute containing the accepted encodings.
	 * @return The name of the attribute containing the accepted encodings.
	 */
	public String getEncodingsAttribute()
	{
		return this.encodingsAttribute;
	}

	/**
	 * Sets the name of the attribute containing the accepted encodings.
	 * @param attributeName The name of the attribute containing the accepted encodings.
	 */
	public void setEncodingsAttribute(String attributeName)
	{
		this.encodingsAttribute = attributeName;
	}

	/**
	 * Returns the name of the attribute containing the accepted languages.
	 * @return The name of the attribute containing the accepted languages.
	 */
	public String getLanguagesAttribute()
	{
		return this.languagesAttribute;
	}

	/**
	 * Sets the name of the attribute containing the accepted languages.
	 * @param attributeName The name of the attribute containing the accepted languages.
	 */
	public void setLanguagesAttribute(String attributeName)
	{
		this.languagesAttribute = attributeName;
	}

	/**
	 * Returns the name of the attribute containing the accepted media types.
	 * @return The name of the attribute containing the accepted media types.
	 */
	public String getMediaTypesAttribute()
	{
		return this.mediaTypesAttribute;
	}

	/**
	 * Sets the name of the attribute containing the accepted media types.
	 * @param attributeName The name of the attribute containing the accepted media types.
	 */
	public void setMediaTypesAttribute(String attributeName)
	{
		this.mediaTypesAttribute = attributeName;
	}

	/**
	 * Indicates if the resource reference can be tunneled.
	 * @return True if the resource reference can be tunneled.
	 */
	public boolean isUriTunnel()
	{
		return this.uriTunnel;
	}

	/**
	 * @param uriTunnel the uriTunnel to set
	 */
	public void setUriTunnel(boolean uriTunnel)
	{
		this.uriTunnel = uriTunnel;
	}

	/**
	 * Returns the name of the attribute containing the resource reference.
	 * @return The name of the attribute containing the resource reference.
	 */
	public String getUriAttribute()
	{
		return this.uriAttribute;
	}

	/**
	 * Sets the name of the attribute containing the resource reference.
	 * @param attributeName The name of the attribute containing the resource reference.
	 */
	public void setUriAttribute(String attributeName)
	{
		this.uriAttribute = attributeName;
	}
 
}
