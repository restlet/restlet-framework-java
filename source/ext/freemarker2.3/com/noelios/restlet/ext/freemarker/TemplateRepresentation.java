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
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class TemplateRepresentation extends OutputRepresentation
{
   /** The FreeMarker template's name. */
   protected String templateName;

   /** The FreeMarker configuration. */
   Configuration config;

   /** The template's data model. */
   protected Object dataModel;

   /**
    * Constructor.
    * @param templateName The FreeMarker template's name. The full path is resolved by the configuration. 
    * @param config The FreeMarker configuration.
    * @param dataModel The FreeMarker template's data model.
    * @param mediaType The representation's media type.
    */
   public TemplateRepresentation(String templateName, Configuration config, Object dataModel,
         MediaType mediaType)
   {
      super(mediaType);
      this.config = config;
      this.dataModel = dataModel;
      this.templateName = templateName;
   }

   /**
    * Returns the FreeMarker template's data model.
    * @return The FreeMarker template's data model.
    */
   public Object getDataModel()
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
    * @param templateName The FreeMarker template's name. The full path is resolved by the configuration. 
    * @param config The FreeMarker configuration.
    * @return The loaded template.
    * @throws IOException
    */
   private static Template loadTemplate(String templateName, Configuration config) throws IOException
   {
      return config.getTemplate(templateName);
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
         Template template = loadTemplate(templateName, config);
         if(getCharacterSet() != null)
         {
            tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream, getCharacterSet().getName()));
         }
         else
         {
            tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream, template.getEncoding()));
         }

         template.process(getDataModel(), tmplWriter);
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
