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

package org.restlet.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Factory;

/**
 * Client specific data related to a call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ClientData
{
	/** The IP addresses. */
	private List<String> addresses;

	/** The name. */
	private String name;

	/** The character set preferences. */
	private List<CharacterSetPref> characterSetPrefs;

	/** The encoding preferences. */
	private List<EncodingPref> encodingPrefs;

	/** The language preferences. */
	private List<LanguagePref> languagePrefs;

	/** The media preferences. */
	private List<MediaTypePref> mediaTypePrefs;

	/**
	 * Constructor.
	 */
	public ClientData()
	{
		this.characterSetPrefs = null;
		this.encodingPrefs = null;
		this.languagePrefs = null;
		this.mediaTypePrefs = null;
	}

	/**
	 * Returns the client's IP address.
	 * @return The client's IP address.
	 */
	public String getAddress()
	{
		return (this.addresses == null) ? null : (this.addresses.isEmpty() ? null
				: this.addresses.get(0));
	}

	/**
	 * Returns the list of client IP addresses.<br/> The first address is the one of the 
	 * immediate client component as returned by the getClientAdress() method and the last
	 * address should correspond to the origin client (frequently a user agent). This is 
	 * useful when the user agent is separated from the origin server by a chain of 
	 * intermediary components.
	 * @return The client IP addresses.
	 */
	public List<String> getAddresses()
	{
		if (this.addresses == null) this.addresses = new ArrayList<String>();
		return this.addresses;
	}

	/**
	 * Sets the client's IP address.
	 * @param address The client's IP address.
	 */
	public void setAddress(String address)
	{
		if (getAddresses().isEmpty())
		{
			getAddresses().add(address);
		}
		else
		{
			getAddresses().set(0, address);
		}
	}

	/**
	 * Returns the client's name.
	 * @return The client's name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Sets the client's name.
	 * @param name The client's name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the character set preferences.
	 * @return The character set preferences.
	 */
	public List<CharacterSetPref> getCharacterSetPrefs()
	{
		if (this.characterSetPrefs == null)
			this.characterSetPrefs = new ArrayList<CharacterSetPref>();
		return this.characterSetPrefs;
	}

	/**
	 * Returns the encoding preferences.
	 * @return The encoding preferences.
	 */
	public List<EncodingPref> getEncodingPrefs()
	{
		if (this.encodingPrefs == null) this.encodingPrefs = new ArrayList<EncodingPref>();
		return this.encodingPrefs;
	}

	/**
	 * Returns the language preferences.
	 * @return The language preferences.
	 */
	public List<LanguagePref> getLanguagePrefs()
	{
		if (this.languagePrefs == null) this.languagePrefs = new ArrayList<LanguagePref>();
		return this.languagePrefs;
	}

	/**
	 * Returns the media type preferences.
	 * @return The media type preferences.
	 */
	public List<MediaTypePref> getMediaTypePrefs()
	{
		if (this.mediaTypePrefs == null)
			this.mediaTypePrefs = new ArrayList<MediaTypePref>();
		return this.mediaTypePrefs;
	}

	/**
	 * Returns the best variant representation for a given resource according the the client preferences.
	 * @param resource The resource for which the best representation needs to be set.
	 * @param fallbackLanguage The language to use if no preference matches.
	 * @return The best variant representation.
	 * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
	 */
	public Representation getBestVariant(Resource resource, Language fallbackLanguage)
	{
		return getBestVariant(resource.getVariants(), fallbackLanguage);
	}

	/**
	 * Returns the best variant representation for a given resource according the the client preferences.
	 * @param variants The list of variants to compare.
	 * @param fallbackLanguage The language to use if no preference matches.
	 * @return The best variant representation.
	 * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
	 */
	public Representation getBestVariant(List<Representation> variants,
			Language fallbackLanguage)
	{
		return Factory.getInstance().getBestVariant(this, variants, fallbackLanguage);
	}

}
