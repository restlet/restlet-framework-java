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

package org.restlet.representation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;

/**
 * Represents an Unicode string that can be converted to any character set supported by Java. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class StringRepresentation extends StreamRepresentation
{
	private String value;

	/**
	 * Constructor. The following metadata are used by default: "text/plain" media type, no language and the 
	 * ISO-8859-1 character set.
	 * @param value The string value. 
	 */
	public StringRepresentation(String value)
	{
		this(value, MediaType.TEXT_PLAIN);
	}

	/**
	 * Constructor. The following metadata are used by default: no language and the ISO-8859-1 character set.
	 * @param value The string value.
	 * @param mediaType The media type. 
	 */
	public StringRepresentation(String value, MediaType mediaType)
	{
		this(value, mediaType, null);
	}

	/**
	 * Constructor. The following metadata are used by default: "text/plain" media type, no language and the 
	 * ISO-8859-1 character set.
	 * @param value The string value.
	 * @param language The language.
	 */
	public StringRepresentation(String value, Language language)
	{
		this(value, MediaType.TEXT_PLAIN, language);
	}

	/**
	 * Constructor. The following metadata are used by default: ISO-8859-1 character set.
	 * @param value The string value.
	 * @param mediaType The media type.
	 * @param language The language.
	 */
	public StringRepresentation(String value, MediaType mediaType, Language language)
	{
		this(value, mediaType, language, CharacterSet.ISO_8859_1);
	}

	/**
	 * Constructor.
	 * @param value The string value.
	 * @param mediaType The media type.
	 * @param language The language.
	 * @param characterSet The character set.
	 */
	public StringRepresentation(String value, MediaType mediaType, Language language,
			CharacterSet characterSet)
	{
		super(mediaType);
		this.value = value;
		setMediaType(mediaType);
		setLanguage(language);
		setCharacterSet(characterSet);
		updateSize();
	}

	/**
	 * Updates the expected size according to the current string value.
	 */
	protected void updateSize()
	{
		if (getValue() != null)
		{
			setSize(getValue().length());
		}
		else
		{
			setSize(UNKNOWN_SIZE);
		}
	}

	/**
	 * Converts the representation to a string value. Be careful when using this method as the conversion of 
	 * large content to a string fully stored in memory can result in OutOfMemoryErrors being thrown.
	 * @return The representation as a string value.
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * Sets the string value.
	 * @param value The string value.
	 */
	public void setValue(String value)
	{
		this.value = value;
		updateSize();
	}

	/**
	 * Returns a stream with the representation's content.
	 * This method is ensured to return a fresh stream for each invocation unless it 
	 * is a transient representation, in which case null is returned.
	 * @return A stream with the representation's content.
	 * @throws IOException
	 */
	public InputStream getStream() throws IOException
	{
		if (getValue() != null)
		{
			return new ByteArrayInputStream(getValue().getBytes(getCharacterSet().getName()));
		}
		else
		{
			return null;
		}
	}

	/**
	 * Writes the representation to a byte stream.
	 * This method is ensured to write the full content for each invocation unless it 
	 * is a transient representation, in which case an exception is thrown.
	 * @param outputStream The output stream.
	 * @throws IOException
	 */
	public void write(OutputStream outputStream) throws IOException
	{
		if (getValue() != null)
		{
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, getCharacterSet()
					.getName());
			osw.write(getValue());
			osw.flush();
		}
	}

}
