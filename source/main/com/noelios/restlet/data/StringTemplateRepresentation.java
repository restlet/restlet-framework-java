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

import org.restlet.data.MediaType;

import com.noelios.restlet.util.MapModel;
import com.noelios.restlet.util.Model;
import com.noelios.restlet.util.StringTemplate;

/**
 * Representation based on a simple string template.
 * Note that the string value is dynamically computed, each time it is accessed.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class StringTemplateRepresentation extends StringRepresentation implements Model
{
	/**
	 * The string template.
	 */
	protected StringTemplate template;
	
	/**
	 * The template model.
	 */
	protected Model model;
   
   /**
    * Constructor.
    * @param pattern The template pattern to process.
    * @param model The template model to use.
    * @param mediaType The representation's media type.
    */
   public StringTemplateRepresentation(CharSequence pattern, Model model, MediaType mediaType)
   {
      super(null, mediaType);
      this.template = new StringTemplate(pattern);
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
    * @param delimiterStart The string that defines instructions start delimiters.
    * @param delimiterEnd The string that defines instructions end delimiters.
    * @param model The template model to use.
    * @param mediaType The representation's media type.
    */
   public StringTemplateRepresentation(CharSequence pattern, String delimiterStart, String delimiterEnd, Model model, MediaType mediaType)
   {
      super(null, mediaType);
      this.template = new StringTemplate(pattern, delimiterStart, delimiterEnd);
      this.model = model;
   }
	
   /**
    * Constructor.
    * @param pattern The template pattern to process.
    * @param delimiterStart The string that defines instructions start delimiters.
    * @param delimiterEnd The string that defines instructions end delimiters.
    * @param mediaType The representation's media type.
    */
   public StringTemplateRepresentation(CharSequence pattern, String delimiterStart, String delimiterEnd, MediaType mediaType)
   {
      this(pattern, delimiterStart, delimiterEnd, new MapModel(), mediaType);
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

}
