/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.application;

import org.restlet.data.*;

/**
 * Associates an extension name and a metadata.
 * 
 * @author Alex Milowski (alexml@milowski.org)
 * @author Thierry Boileau
 */
public class MetadataExtension {

	/** The mapped metadata. */
	private final Metadata metadata;

	/** The name of the extension. */
	private final String name;

	/**
	 * Constructor.
	 * 
	 * @param name     The extension name.
	 * @param metadata The metadata.
	 */
	public MetadataExtension(String name, Metadata metadata) {
		this.name = name;
		this.metadata = metadata;
	}

	/**
	 * Returns the character set.
	 * 
	 * @return the character set.
	 */
	public CharacterSet getCharacterSet() {
		return (CharacterSet) getMetadata();
	}

	/**
	 * Returns the encoding.
	 * 
	 * @return the encoding.
	 */
	public Encoding getEncoding() {
		return (Encoding) getMetadata();
	}

	/**
	 * Returns the language.
	 * 
	 * @return the language.
	 */
	public Language getLanguage() {
		return (Language) getMetadata();
	}

	/**
	 * Returns the media type.
	 * 
	 * @return the media type.
	 */
	public MediaType getMediaType() {
		return (MediaType) getMetadata();
	}

	/**
	 * Returns the metadata.
	 * 
	 * @return the metadata.
	 */
	public Metadata getMetadata() {
		return this.metadata;
	}

	/**
	 * Returns the extension name.
	 * 
	 * @return The extension name.
	 */
	public String getName() {
		return this.name;
	}

}
