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

package com.noelios.restlet.impl.local;

import java.util.Map;
import java.util.TreeMap;

import org.restlet.Client;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;

import com.noelios.restlet.impl.ClientHelper;

/**
 * Connector to the local resources accessible via file system, class loaders and similar mechanisms.
 * Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>addCommonExtensions</td>
 * 		<td>boolean</td>
 * 		<td>true</td>
 * 		<td>Sets a common list of associations from extensions to metadata.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>encodingExtension</td>
 * 		<td>String</td>
 * 		<td>null</td>
 * 		<td>Set a new extension for an encoding. The value is the extension name and the encoding name
 * separated by a space</td>
 * 	</tr>
 * 	<tr>
 * 		<td>languageExtension</td>
 * 		<td>String</td>
 * 		<td>null</td>
 * 		<td>Set a new extension for a language. The value is the extension name and the language name
 * separated by a space</td>
 * 	</tr>
 * 	<tr>
 * 		<td>mediaTypeExtension</td>
 * 		<td>String</td>
 * 		<td>null</td>
 * 		<td>Set a new extension for a media type. The value is the extension name and the media type name
 * separated by a space</td>
 * 	</tr>
 * 	<tr>
 * 		<td>defaultEncoding</td>
 * 		<td>String</td>
 * 		<td>identity</td>
 * 		<td>Default encoding used when no encoding extension is available.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>defaultMediaType</td>
 * 		<td>String</td>
 * 		<td>text/plain</td>
 * 		<td>Default media type used when no media type extension is available.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>defaultLanguage</td>
 * 		<td>String</td>
 * 		<td>en-us</td>
 * 		<td>Default language used when no language extension is available.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>timeToLive</td>
 * 		<td>int</td>
 * 		<td>600</td>
 * 		<td>Time to live for a file representation before it expires (in seconds).</td>
 * 	</tr>
 * 	<tr>
 * 		<td>webAppPath</td>
 * 		<td>String</td>
 * 		<td>${user.home}/restlet.war</td>
 * 		<td>Path to the Web Application WAR file or directory.</td>
 * 	</tr>
 *	</table>
 * @see com.noelios.restlet.data.ClapReference
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @author Thierry Boileau
 */
public class LocalClientHelper extends ClientHelper
{
	/** Mappings from extensions to metadata. */
	private Map<String, Metadata> metadataMappings;

	/**
	 * Constructor. Note that the common list of metadata associations based on extensions is added, see
	 * the addCommonExtensions() method.
	 * @param client The client to help.
	 */
	public LocalClientHelper(Client client)
	{
		super(client);
		this.metadataMappings = new TreeMap<String, Metadata>();
	}

	/** Starts the Restlet. */
	public void start() throws Exception
	{
		// Optionnaly add the common extensions
		if (isAddCommonExtensions()) addCommonExtensions();

		// Set encoding extensions
		String[] tokens;
		for (Parameter param : getClient().getContext().getParameters().subList(
				"encodingExtension"))
		{
			tokens = param.getValue().split(" ");

			if ((tokens != null) && (tokens.length == 2))
			{
				addExtension(tokens[0], new Encoding(tokens[1]));
			}
			else
			{
				getLogger().warning(
						"Unable to parse the following parameter: encodingExtension = "
								+ param.getValue());
			}
		}

		// Set language extensions
		for (Parameter param : getClient().getContext().getParameters().subList(
				"languageExtension"))
		{
			tokens = param.getValue().split(" ");

			if ((tokens != null) && (tokens.length == 2))
			{
				addExtension(tokens[0], new Language(tokens[1]));
			}
			else
			{
				getLogger().warning(
						"Unable to parse the following parameter: languageExtension = "
								+ param.getValue());
			}
		}

		// Set media type extensions
		for (Parameter param : getClient().getContext().getParameters().subList(
				"mediaTypeExtension"))
		{
			tokens = param.getValue().split(" ");

			if ((tokens != null) && (tokens.length == 2))
			{
				addExtension(tokens[0], new MediaType(tokens[1]));
			}
			else
			{
				getLogger().warning(
						"Unable to parse the following parameter: mediaTypeExtension = "
								+ param.getValue());
			}
		}

		super.start();
	}

	/**
	 * Maps an extension to some metadata (media type, language or character set) to an extension.
	 * @param extension The extension name.
	 * @param metadata The metadata to map.
	 */
	public void addExtension(String extension, Metadata metadata)
	{
		this.metadataMappings.put(extension, metadata);
	}

	/**
	 * Adds a common list of associations from extensions to metadata.
	 * The list of languages extensions:<br/>
	 * <ul>
	 *  <li>en: English</li>
	 *  <li>es: Spanish</li>
	 *  <li>fr: French</li>
	 * </ul><br/>
	 * The list of media type extensions:<br/>
	 * <ul>
	 *  <li>css: CSS stylesheet</li>
	 *  <li>doc: Microsoft Word document</li>
	 *  <li>gif: GIF image</li>
	 *  <li>html: HTML document</li>
	 *  <li>ico: Windows icon (Favicon)</li>
	 *  <li>jpeg, jpg: JPEG image</li>
	 *  <li>js: JavaScript document</li>
	 *  <li>json: JavaScript Object Notation document</li>
	 *  <li>pdf: Adobe PDF document</li>
	 *  <li>png: PNG image</li>
	 *  <li>ppt: Microsoft Powerpoint document</li>
	 *  <li>rdf:  Description Framework document</li>
	 *  <li>txt: Plain text</li>
	 *  <li>swf: Shockwave Flash object</li>
	 *  <li>xhtml: XHTML document</li>
	 *  <li>xml: XML document</li>
	 *  <li>zip: Zip archive</li>
	 * </ul>
	 */
	public void addCommonExtensions()
	{
		addExtension("en", Language.ENGLISH);
		addExtension("es", Language.SPANISH);
		addExtension("fr", Language.FRENCH);

		addExtension("css", MediaType.TEXT_CSS);
		addExtension("doc", MediaType.APPLICATION_WORD);
		addExtension("gif", MediaType.IMAGE_GIF);
		addExtension("html", MediaType.TEXT_HTML);
		addExtension("ico", MediaType.IMAGE_ICON);
		addExtension("jpeg", MediaType.IMAGE_JPEG);
		addExtension("jpg", MediaType.IMAGE_JPEG);
		addExtension("js", MediaType.APPLICATION_JAVASCRIPT);
		addExtension("json", MediaType.APPLICATION_JSON);
		addExtension("pdf", MediaType.APPLICATION_PDF);
		addExtension("png", MediaType.IMAGE_PNG);
		addExtension("ppt", MediaType.APPLICATION_POWERPOINT);
		addExtension("rdf", MediaType.APPLICATION_RDF);
		addExtension("txt", MediaType.TEXT_PLAIN);
		addExtension("svg", MediaType.IMAGE_SVG);
		addExtension("swf", MediaType.APPLICATION_FLASH);
		addExtension("xhtml", MediaType.APPLICATION_XHTML_XML);
		addExtension("xml", MediaType.TEXT_XML);
		addExtension("zip", MediaType.APPLICATION_ZIP);
	}

	/**
	 * Returns the metadata mapped to an extension.
	 * @param extension The extension name.
	 * @return The mapped metadata.
	 */
	public Metadata getMetadata(String extension)
	{
		return this.metadataMappings.get(extension);
	}

	/**
	 * Updates some representation metadata based on a given entry name with extensions. 
	 * @param entryName The entry name with extensions.
	 * @param representation The representation to update.
	 */
	public void updateMetadata(String entryName, Representation representation)
	{
		String[] tokens = entryName.split("\\.");
		Metadata current;

		// We found a potential variant
		for (int j = 1; j < tokens.length; j++)
		{
			current = getMetadata(tokens[j]);
			if (current != null)
			{
				// Metadata extension detected 
				if (current instanceof MediaType)
					representation.setMediaType((MediaType) current);
				if (current instanceof CharacterSet)
					representation.setCharacterSet((CharacterSet) current);
				if (current instanceof Encoding)
					representation.setEncoding((Encoding) current);
				if (current instanceof Language)
					representation.setLanguage((Language) current);
			}

			int dashIndex = tokens[j].indexOf('-');
			if ((representation != null) && (dashIndex != -1))
			{
				// We found a language extension with a region area specified
				// Try to find a language matching the primary part of the extension
				String primaryPart = tokens[j].substring(0, dashIndex);
				current = getMetadata(primaryPart);
				if (current instanceof Language)
					representation.setLanguage((Language) current);
			}
		}
	}

	/**
	 * Returns the default encoding.
	 * Used when no encoding extension is available.
	 * @return The default encoding.
	 */
	public Encoding getDefaultEncoding()
	{
		return new Encoding(getParameters().getFirstValue("defaultEncoding", "identity"));
	}

	/**
	 * Returns the default media type.
	 * Used when no media type extension is available.
	 * @return The default media type.
	 */
	public MediaType getDefaultMediaType()
	{
		return new MediaType(getParameters()
				.getFirstValue("defaultMediaType", "text/plain"));
	}

	/**
	 * Returns the default language.
	 * Used when no language extension is available.
	 * @return The default language.
	 */
	public Language getDefaultLanguage()
	{
		return new Language(getParameters().getFirstValue("defaultLanguage", "en-us"));
	}

	/**
	 * Returns the time to live for a file representation before it expires (in seconds).
	 * @return The time to live for a file representation before it expires (in seconds).
	 */
	public int getTimeToLive()
	{
		return Integer.parseInt(getParameters().getFirstValue("timeToLive", "600"));
	}

	/**
	 * Indicates if a common list of associations from extensions to metadata should be set.
	 * @return True if a common list of associations from extensions to metadata should be set.
	 */
	public boolean isAddCommonExtensions()
	{
		return Boolean.parseBoolean(getParameters().getFirstValue("addCommonExtensions",
				"true"));
	}

}
