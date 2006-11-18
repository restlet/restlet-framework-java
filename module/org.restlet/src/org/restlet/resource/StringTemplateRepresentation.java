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

package org.restlet.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.util.MapModel;
import org.restlet.util.Model;
import org.restlet.util.StringTemplate;

/**
 * Representation based on a simple string template.
 * Note that the string value is dynamically computed, each time it is accessed.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class StringTemplateRepresentation extends StreamRepresentation
{
	/** The string template. */
	private StringTemplate template;

	/** The template model. */
	private Model model;

	/**
	 * Constructor.
	 * @param pattern The template pattern to process.
	 * @param mediaType The representation's media type.
	 */
	public StringTemplateRepresentation(CharSequence pattern, MediaType mediaType)
	{
		this(pattern, new MapModel(), mediaType);
	}

	/**
	 * Constructor.
	 * @param pattern The template pattern to process.
	 * @param model The template model to use.
	 * @param mediaType The representation's media type.
	 */
	public StringTemplateRepresentation(CharSequence pattern, Model model,
			MediaType mediaType)
	{
		super(mediaType);
		setCharacterSet(CharacterSet.ISO_8859_1);
		this.template = new StringTemplate(pattern);
		this.template.setLogger(getLogger());
		this.model = model;
	}

	/**
	 * Constructor.
	 * @param pattern The template pattern to process.
	 * @param variableStart The string that defines instructions start delimiters.
	 * @param variableEnd The string that defines instructions end delimiters.
	 * @param instructionStart The string that defines instructions start delimiters.
	 * @param instructionEnd The string that defines instructions end delimiters.
	 * @param mediaType The representation's media type.
	 */
	public StringTemplateRepresentation(CharSequence pattern, String variableStart,
			String variableEnd, String instructionStart, String instructionEnd,
			MediaType mediaType)
	{
		this(pattern, variableStart, variableEnd, instructionStart, instructionEnd,
				new MapModel(), mediaType);
	}

	/**
	 * Constructor.
	 * @param pattern The template pattern to process.
	 * @param variableStart The string that defines instructions start delimiters.
	 * @param variableEnd The string that defines instructions end delimiters.
	 * @param instructionStart The string that defines instructions start delimiters.
	 * @param instructionEnd The string that defines instructions end delimiters.
	 * @param model The template model.
	 * @param mediaType The representation's media type.
	 */
	public StringTemplateRepresentation(CharSequence pattern, String variableStart,
			String variableEnd, String instructionStart, String instructionEnd, Model model,
			MediaType mediaType)
	{
		super(mediaType);
		setCharacterSet(CharacterSet.ISO_8859_1);
		this.template = new StringTemplate(pattern, variableStart, variableEnd,
				instructionStart, instructionEnd);
		this.template.setLogger(getLogger());
		this.model = model;
	}

	/**
	 * Returns the data model.
	 * @return The data model.
	 */
	public Model getModel()
	{
		return this.model;
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
		String value = getValue();

		if (value != null)
		{
			if (getCharacterSet() != null)
			{
				return new ByteArrayInputStream(value.getBytes(getCharacterSet().getName()));
			}
			else
			{
				return new ByteArrayInputStream(value.getBytes());
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * Converts the representation to a string value. Be careful when using this method as the conversion of 
	 * large content to a string fully stored in memory can result in OutOfMemoryErrors being thrown.
	 * @return The representation as a string value.
	 */
	public String getValue()
	{
		return this.template.format(this.model);
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
		String value = getValue();

		if (value != null)
		{
			OutputStreamWriter osw = null;

			if (getCharacterSet() != null)
			{
				osw = new OutputStreamWriter(outputStream, getCharacterSet().getName());
			}
			else
			{
				osw = new OutputStreamWriter(outputStream);
			}

			osw.write(value);
			osw.flush();
		}
	}

}
