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

package com.noelios.restlet.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.restlet.data.MediaType;

import com.noelios.restlet.util.MapModel;
import com.noelios.restlet.util.Model;
import com.noelios.restlet.util.StringTemplate;

/**
 * Representation based on a simple string template.
 * Note that the string value is dynamically computed, each time it is accessed.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class StringTemplateRepresentation extends StreamRepresentation implements Model
{
	/** The string template. */
	private StringTemplate template;

	/** The template model. */
	private Model model;

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
		this.template = new StringTemplate(pattern);
		this.template.setLogger(getLogger());
		this.model = model;
	}

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
		this.template = new StringTemplate(pattern, variableStart,
				variableEnd, instructionStart, instructionEnd);
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
	 * Returns the internal value.
	 * @return The internal value.
	 */
	public String getValue()
	{
		return this.template.process(this.model);
	}

	/**
	 * Returns the model value for a given name.
	 * @param name The name to look-up.
	 * @return The model value for the given name.
	 */
	public String get(String name)
	{
		return this.model.get(name);
	}

	/**
	 * Indicates if the model contains a value for a given name.
	 * @param name The name to look-up.
	 * @return True if the model contains a value for the given name.
	 */
	public boolean contains(String name)
	{
		return this.model.contains(name);
	}

	/**
	 * Puts the model value for a given name.
	 * @param name The name to look-up.
	 * @param value The value to put.
	 */
	public void put(String name, String value)
	{
		this.model.put(name, value);
	}

	/**
	 * Converts the representation to a string.
	 * @return The representation as a string.
	 */
	public String toString()
	{
		return getValue();
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
		}
	}

}
