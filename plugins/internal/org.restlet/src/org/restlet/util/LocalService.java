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

package org.restlet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Application;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;

/**
 * Service providing access to local resources. The resources will be resolved from the Web Application 
 * context, which is similar to the notion of ServletContext in the Servlet specification. If the 
 * application is packaged as a WAR file, then the resources correspond to files 
 * at the root of the archive, including the META-INF and WEB-INF directories. Examples: 
 * war:/rootDir/subDir/file.html or war:/WEB-INF/temlates/layout.fmt
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class LocalService extends Service
{
	/** The default encoding for local representations. */
	private Encoding defaultEncoding;

	/** The default language for local representations. */
	private Language defaultLanguage;

	/** The default media type for local representations. */
	private MediaType defaultMediaType;

	/** The mappings from extension names to metadata. */
	private Map<String, Metadata> metadataMappings;

	/** The list of index names (ex: index.html). */
	private List<String> indexNames;
	
	/**
	 * Constructor.
	 * @param application The parent appplication.
	 * @param enabled True if the service has been enabled.
	 */
	public LocalService(Application application, boolean enabled)
	{
		super(application, enabled);
		this.defaultEncoding = null;
		this.defaultLanguage = null;
		this.defaultMediaType = null;
		this.metadataMappings = new TreeMap<String, Metadata>();
		this.indexNames = new ArrayList<String>();
	}

	/**
	 * Returns the default encoding for local representations.
	 * @return The default encoding for local representations.
	 */
	public Encoding getDefaultEncoding()
	{
		return this.defaultEncoding;
	}

	/**
	 * Returns the default language for local representations.
	 * @return The default language for local representations.
	 */
	public Language getDefaultLanguage()
	{
		return this.defaultLanguage;
	}

	/**
	 * Returns the default media type for local representations.
	 * @return The default media type for local representations.
	 */
	public MediaType getDefaultMediaType()
	{
		return this.defaultMediaType;
	}

	/**
	 * Returns the list of index names (ex: index.html).
	 * @return The list of index names (ex: index.html).
	 */
	public List<String> getIndexNames()
	{
		return this.indexNames;
	}

	/**
	 * Returns the mappings from extension names to metadata.
	 * @return The mappings from extension names to metadata.
	 */
	public Map<String, Metadata> getMetadataMappings()
	{
		return this.metadataMappings;
	}

	/**
	 * Sets the default encoding for local representations.
	 * @param defaultEncoding The default encoding for local representations.
	 */
	public void setDefaultEncoding(Encoding defaultEncoding)
	{
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Sets the default language for local representations.
	 * @param defaultLanguage The default language for local representations.
	 */
	public void setDefaultLanguage(Language defaultLanguage)
	{
		this.defaultLanguage = defaultLanguage;
	}

	/**
	 * Sets the default media type for local representations.
	 * @param defaultMediaType The default media type for local representations.
	 */
	public void setDefaultMediaType(MediaType defaultMediaType)
	{
		this.defaultMediaType = defaultMediaType;
	}

}
