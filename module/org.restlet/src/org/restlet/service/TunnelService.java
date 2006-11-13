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

package org.restlet.service;

import org.restlet.data.ClientInfo;

/**
 * Service providing tunnelling of method names or client preferences. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class TunnelService
{
	/** Indicates if the service has been enabled. */
	private boolean enabled;

	/** Indicates if the method name can be tunneled. */
	private boolean methodTunnel;

	/** The name of the attribute containing the method name. */
	private String methodAttribute;

	/** Indicates if the client preferences can be tunneled. */
	private boolean preferencesTunnel;

	/** The name of the attribute containing the accepted character set. */
	private String characterSetAttribute;

	/** The name of the attribute containing the accepted encoding. */
	private String encodingAttribute;

	/** The name of the attribute containing the accepted language. */
	private String languageAttribute;

	/** The name of the attribute containing the accepted media type. */
	private String mediaTypeAttribute;

	/**
	 * Constructor.
	 * @param enabled True if the service has been enabled.
	 * @param methodTunnel Indicates if the method name can be tunneled.
	 * @param preferencesTunnel Indicates if the client preferences can be tunneled.
	 */
	public TunnelService(boolean enabled, boolean methodTunnel, boolean preferencesTunnel)
	{
		this.enabled = enabled;
		this.methodTunnel = methodTunnel;
		this.methodAttribute = "method";
		this.preferencesTunnel = preferencesTunnel;
		this.characterSetAttribute = "charset";
		this.encodingAttribute = "encoding";
		this.languageAttribute = "language";
		this.mediaTypeAttribute = "media";
	}

	/**
	 * Indicates if the service should be enabled.
	 * @return True if the service should be enabled.
	 */
	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	/**
	 * Indicates if the service should be enabled.
	 * @param enabled True if the service should be enabled.
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * Indicates if the request from a given client can be tunnelled. The default implementation always
	 * return true. This could be customize to restrict the usage of the tunnel service.
	 * @param client The client to test.
	 * @return True if the request from a given client can be tunnelled.
	 */
	public boolean allowClient(ClientInfo client)
	{
		return true;
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
	 * Returns the character set attribute name.
	 * @return The character set attribute name.
	 */
	public String getCharacterSetAttribute()
	{
		return this.characterSetAttribute;
	}

	/**
	 * Sets the character set attribute name.
	 * @param attributeName The character set attribute name.
	 */
	public void setCharacterSetAttribute(String attributeName)
	{
		this.characterSetAttribute = attributeName;
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
	 * Returns the name of the attribute containing the accepted encoding.
	 * @return The name of the attribute containing the accepted encoding.
	 */
	public String getEncodingAttribute()
	{
		return this.encodingAttribute;
	}

	/**
	 * Sets the name of the attribute containing the accepted encoding.
	 * @param attributeName The name of the attribute containing the accepted encoding.
	 */
	public void setEncodingAttribute(String attributeName)
	{
		this.encodingAttribute = attributeName;
	}

	/**
	 * Returns the name of the attribute containing the accepted language.
	 * @return The name of the attribute containing the accepted language.
	 */
	public String getLanguageAttribute()
	{
		return this.languageAttribute;
	}

	/**
	 * Sets the name of the attribute containing the accepted language.
	 * @param attributeName The name of the attribute containing the accepted language.
	 */
	public void setLanguageAttribute(String attributeName)
	{
		this.languageAttribute = attributeName;
	}

	/**
	 * Returns the name of the attribute containing the accepted media type.
	 * @return The name of the attribute containing the accepted media type.
	 */
	public String getMediaTypeAttribute()
	{
		return this.mediaTypeAttribute;
	}

	/**
	 * Sets the name of the attribute containing the accepted media type.
	 * @param attributeName The name of the attribute containing the accepted media type.
	 */
	public void setMediaTypeAttribute(String attributeName)
	{
		this.mediaTypeAttribute = attributeName;
	}
	
}
