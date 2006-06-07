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

package com.noelios.restlet.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;

import com.noelios.restlet.data.FileReference;

/**
 * File client connector call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class FileCall extends ContextCall
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.impl.FileCall");

   /** The wrapped File. */
   protected File file;
   
   /**
    * Constructor.
    * @param method The method name.
    * @param requestUri The request URI.
    * @throws IOException
    */
   public FileCall(String method, String requestUri) throws IOException
   {
      super(method, requestUri);

      FileReference fr = new FileReference(requestUri);
      if(fr.getScheme().equalsIgnoreCase("file"))
      {
         this.file = fr.getFile();
      }
      else
      {
         throw new IllegalArgumentException("Only FILE resource URIs are allowed here");
      }
   }
   
   /**
    * Returns the file.
    * @return The file.
    */
   public File getFile()
   {
      return this.file;
   }

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public WritableByteChannel getRequestChannel()
   {
   	try
		{
			FileOutputStream fos = new FileOutputStream(getFile());
			return fos.getChannel();
		}
		catch (FileNotFoundException fnfe)
		{
			logger.log(Level.WARNING, "Couldn't get the request channel.", fnfe);
			return null;
		}
   }
   
   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public OutputStream getRequestStream()
   {
   	try
		{
			return new FileOutputStream(getFile());
		}
		catch (FileNotFoundException fnfe)
		{
			logger.log(Level.WARNING, "Couldn't get the request channel.", fnfe);
			return null;
		}
   }
   
   /**
    * Returns the response output representation if available. Note that no metadata is associated by default, 
    * you have to manually set them from your headers.
    * @return The response output representation if available.
    */
   public Representation getResponseOutput()
   {
   	Representation result = super.getResponseOutput();

      if(result != null)
      {
      	result.setSize(getFile().length());
      	RepresentationMetadata metadata = result.getMetadata();
      	metadata.setModificationDate(new Date(getFile().lastModified()));

//         if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_TYPE))
//         {
//            ContentType contentType = new ContentType(header.getValue());
//            if(contentType != null) 
//            {
//            	metadata.setMediaType(contentType.getMediaType());
//            	metadata.setCharacterSet(contentType.getCharacterSet());
//            }
//         }
//         else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_EXPIRES))
//         {
//         	metadata.setExpirationDate(parseDate(header.getValue(), false));
//         }
//         else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_ENCODING))
//         {
//         	metadata.setEncoding(new DefaultEncoding(header.getValue()));
//         }
//         else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_LANGUAGE))
//         {
//         	metadata.setLanguage(new DefaultLanguage(header.getValue()));
//         }
      }
   	
   	return result;
   }

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public ReadableByteChannel getResponseChannel()
   {
   	ReadableByteChannel result = null;
      
      try
      {
      	result = new FileInputStream(getFile()).getChannel();
      }
      catch(IOException ioe)
      {
       	result = null;
      }
      
      return result;
   }
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public InputStream getResponseStream()
   {
      InputStream result = null;
      
      try
      {
      	result = new FileInputStream(getFile());
      }
      catch(IOException ioe)
      {
       	result = null;
      }
      
      return result;
   }
}
