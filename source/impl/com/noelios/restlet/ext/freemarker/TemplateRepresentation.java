/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.ext.freemarker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.restlet.data.MediaType;

import com.noelios.restlet.data.OutputRepresentation;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * FreeMarker template representation. Useful for dynamic string-based representations.
 * @see <a href="http://freemarker.org/">FreeMarker home page</a>
 */
public class TemplateRepresentation extends OutputRepresentation
{
   /** The FreeMarker template path. */
   protected String templatePath;

   /** The FreeMarker configuration. */
   Configuration config;

   /** The template's data model. */
   protected Object dataModel;

   /**
    * Constructor.
    * @param templatePath The FreeMarker template's path.
    * @param config The FreeMarker configuration.
    * @param dataModel The FreeMarker template's data model.
    * @param mediaType The representation's media type.
    */
   public TemplateRepresentation(String templatePath, Configuration config, Object dataModel,
         MediaType mediaType)
   {
      super(mediaType);
      this.config = config;
      this.dataModel = dataModel;
      this.templatePath = templatePath;
   }

   /**
    * Returns the FreeMarker template's data model.
    * @return The FreeMarker template's data model.
    */
   public Object getValues()
   {
      return this.dataModel;
   }

   /**
    * Sets the FreeMarker template's data model.
    * @param dataModel The FreeMarker template's data model.
    * @return The FreeMarker template's data model.
    */
   public Object setDataModel(Object dataModel)
   {
      this.dataModel = dataModel;
      return dataModel;
   }

   /**
    * Loads a template from the file system.
    * @param templatePath The FreeMarker template's path.
    * @param config The FreeMarker configuration.
    * @return The loaded template.
    * @throws IOException
    */
   private static Template loadTemplate(String templatePath, Configuration config) throws IOException
   {
      return config.getTemplate(templatePath);
   }

   /**
    * Writes the datum as a stream of bytes.
    * @param outputStream The stream to use when writing.
    */
   public void write(OutputStream outputStream) throws IOException
   {
      Writer tmplWriter = null;

      try
      {
         Template template = loadTemplate(templatePath, config);
         if(getCharacterSet() != null)
         {
            tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream, getCharacterSet().getName()));
         }
         else
         {
            tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream, template.getEncoding()));
         }

         template.process(getValues(), tmplWriter);
      }
      catch(TemplateException te)
      {
         throw new IOException("Template processing error");
      }
      finally
      {
         if(tmplWriter != null) tmplWriter.close();
      }
   }

}
