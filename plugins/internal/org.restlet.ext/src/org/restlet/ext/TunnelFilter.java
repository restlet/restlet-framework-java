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

package org.restlet.ext;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Filter extracting some attributes from a call.
 * Multiple extractions can be defined, based on the query part of the resource reference,
 * on the entity form (posted from a browser), on the context matches or on the call's template model.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class TunnelFilter extends ExtractFilter
{
	/** Indicates if the method name can be tunneled. */
	private boolean methodTunnel;

	/** The name of the attribute containing the method name. */
	private String methodAttribute;

	/** Indicates if the client preferences can be tunneled. */
	private boolean preferencesTunnel;

	/** The name of the attribute containing the accepted character sets. */
	private String characterSetsAttribute;

	/** The name of the attribute containing the accepted encodings. */
	private String encodingsAttribute;

	/** The name of the attribute containing the accepted languages. */
	private String languagesAttribute;

	/** The name of the attribute containing the accepted media types. */
	private String mediaTypesAttribute;

	/** Indicates if the resource reference can be tunneled. */
	private boolean uriTunnel;

	/** The name of the attribute containing the resource reference. */
	private String uriAttribute;

	/**
	 * Constructor.
	 * @param context The owner component.
	 * @param methodTunnel Indicates if the method name can be tunneled.
	 * @param preferencesTunnel Indicates if the client preferences can be tunneled.
	 * @param uriTunnel Indicates if the resource reference can be tunneled.
	 */
	public TunnelFilter(Context context, boolean methodTunnel, boolean preferencesTunnel,
			boolean uriTunnel)
	{
		super(context);
		this.methodTunnel = methodTunnel;
		this.methodAttribute = "method";
		this.preferencesTunnel = preferencesTunnel;
		this.characterSetsAttribute = "charset";
		this.encodingsAttribute = "encoding";
		this.languagesAttribute = "language";
		this.mediaTypesAttribute = "media";
		this.uriTunnel = uriTunnel;
		this.uriAttribute = "uri";
	}

	/**
	 * Allows filtering before its handling by the target Restlet. Does nothing by default.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void beforeHandle(Request request, Response response)
	{
		super.beforeHandle(request, response);

		// Tunnels the extracted attributes into the proper call objects.
		if (isMethodTunnel())
		{
			String methodName = (String) request.getAttributes().get(getMethodAttribute());

			if (methodName != null)
			{
				request.setMethod(Method.valueOf(methodName));
			}
		}

		if (isPreferencesTunnel())
		{
			// Extract the header values
			String acceptCharset = (String) request.getAttributes().get(
					getCharacterSetsAttribute());
			String acceptEncoding = (String) request.getAttributes().get(
					getEncodingsAttribute());
			String acceptLanguage = (String) request.getAttributes().get(
					getLanguagesAttribute());
			String acceptMediaType = (String) request.getAttributes().get(
					getMediaTypesAttribute());

			// Parse the headers and update the call preferences
			if (acceptCharset != null)
			{
				request.getClientInfo().getAcceptedCharacterSets().clear();
				CharacterSet cs = CharacterSet.valueOf(acceptCharset);
				if (cs != null)
				{
					request.getClientInfo().getAcceptedCharacterSets().add(
							new Preference<CharacterSet>(cs));
				}
			}

			if (acceptEncoding != null)
			{
				request.getClientInfo().getAcceptedEncodings().clear();
				Encoding enc = Encoding.valueOf(acceptEncoding);
				if (enc != null)
				{
					request.getClientInfo().getAcceptedEncodings().add(
							new Preference<Encoding>(enc));
				}
			}

			if (acceptLanguage != null)
			{
				request.getClientInfo().getAcceptedLanguages().clear();
				Language lang = Language.valueOf(acceptLanguage);
				if (lang != null)
				{
					request.getClientInfo().getAcceptedLanguages().add(
							new Preference<Language>(lang));
				}
			}

			if (acceptMediaType != null)
			{
				request.getClientInfo().getAcceptedMediaTypes().clear();
				MediaType mt = MediaType.valueOf(acceptMediaType);
				if (mt != null)
				{
					request.getClientInfo().getAcceptedMediaTypes().add(
							new Preference<MediaType>(mt));
				}
			}
		}

		if (isUriTunnel())
		{
			String uri = (String) request.getAttributes().get(getUriAttribute());

			if (uri != null)
			{
				request.setResourceRef(uri);
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
	 * Sets the method attribute name.
	 * @param attributeName The method attribute name.
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
