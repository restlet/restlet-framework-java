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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.data.CharacterSet;
import org.restlet.data.DefaultStatus;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Methods;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;
import org.restlet.data.Representation;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.FileReference;
import com.noelios.restlet.data.FileRepresentation;
import com.noelios.restlet.data.ReferenceList;
import com.noelios.restlet.util.ByteUtils;

/**
 * Local file client connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class FileClient extends ContextClient
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.impl.FileClient");

   /**
    * Constructor.
    * @param commonExtensions Indicates if the common extensions should be added.
    */
   public FileClient(boolean commonExtensions)
   {
   	super(Protocols.FILE, commonExtensions);
   }
   
   /**
    * Returns the supported protocols. 
    * @return The supported protocols.
    */
   public static List<Protocol> getProtocols()
   {
   	return Arrays.asList(new Protocol[]{Protocols.FILE});
   }
   
   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      FileReference fr = new FileReference(call.getResourcePath());
      File file = null;
      
      if(fr.getScheme().equalsIgnoreCase("file"))
      {
         file = fr.getFile();
      }
      else
      {
         throw new IllegalArgumentException("Only FILE resource URIs are allowed here");
      }

      if(call.getMethod().equals(Methods.GET) || call.getMethod().equals(Methods.HEAD))
		{
 			if((file != null) && file.exists())
 			{
 				Representation output = null;
 				
 				if(file.isDirectory())
 				{
 					// Return the directory listing
 					File[] files = file.listFiles();
 					ReferenceList rl = new ReferenceList(files.length);
 					
 					for(File entry : files)
 					{
 						try
						{
							rl.add(new FileReference(entry));
						}
						catch (IOException ioe)
						{
							logger.log(Level.WARNING, "Unable to create file reference", ioe);
						}
 					}
 					
 					output = rl.getRepresentation();
 				}
 				else
 				{
 					// Return the file content
               output = new FileRepresentation(file, getDefaultMediaType(), getTimeToLive());
               String[] tokens = file.getName().split("\\.");
               Metadata metadata;
               
               // We found a potential variant
               for(int j = 1; j < tokens.length; j++)
               {
               	metadata = getMetadata(tokens[j]);
                  if(metadata instanceof MediaType) output.getMetadata().setMediaType((MediaType)metadata);
                  if(metadata instanceof CharacterSet) output.getMetadata().setCharacterSet((CharacterSet)metadata);
                  if(metadata instanceof Encoding) output.getMetadata().setEncoding((Encoding)metadata);
                  if(metadata instanceof Language) output.getMetadata().setLanguage((Language)metadata);

                  int dashIndex = tokens[j].indexOf('-');
                  if((metadata == null) && (dashIndex != -1))
                  {
                     // We found a language extension with a region area specified
                     // Try to find a language matching the primary part of the extension
                     String primaryPart = tokens[j].substring(0, dashIndex);
                     metadata = getMetadata(primaryPart);
                     if(metadata instanceof Language) output.getMetadata().setLanguage((Language)metadata);
                  }
               }
 				}
 				
 				call.setOutput(output);
 				call.setStatus(Statuses.SUCCESS_OK);
 			}
 			else
 			{
 				call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
 			}
		}
		else if(call.getMethod().equals(Methods.POST))
		{
			call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
		else if(call.getMethod().equals(Methods.PUT))
		{
			File tmp = null;

			if(file.exists())
			{
				if(file.isDirectory())
				{
					call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Can't put a new representation of a directory"));
				}
				else
				{
					// Replace the content of the file
					// First, create a temporary file
					try
					{
						tmp = File.createTempFile("restlet-upload", "bin");
						
						if(call.getInput() != null)
						{
							ByteUtils.write(call.getInput().getStream(), new FileOutputStream(tmp));
						}
					}
					catch (IOException ioe)
					{
						logger.log(Level.WARNING, "Unable to create the temporary file", ioe);
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create a temporary file"));
					}
					
					// Then delete the existing file
					if(file.delete())
					{
						// Finally move the temporary file to the existing file location
						if(tmp.renameTo(file))
						{
							if(call.getInput() == null)
							{
								call.setStatus(Statuses.SUCCESS_NO_CONTENT);
							}
							else
							{
								call.setStatus(Statuses.SUCCESS_OK);
							}
						}
						else
						{
							logger.log(Level.WARNING, "Unable to move the temporary file to replace the existing file");
							call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to move the temporary file to replace the existing file"));
						}
					}
					else
					{
						logger.log(Level.WARNING, "Unable to delete the existing file");
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to delete the existing file"));
					}
				}
			}
			else
			{
				// No existing file or directory found
				if(fr.getPath().endsWith("/"))
				{
					// Create a new directory and its necessary parents
					if(file.mkdirs())
					{
						call.setStatus(Statuses.SUCCESS_NO_CONTENT);
					}
					else
					{
						logger.log(Level.WARNING, "Unable to create the new directory");
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new directory"));
					}
				}
				else
				{
					File parent = file.getParentFile(); 
					if((parent != null) && parent.isDirectory())
					{
						if(!parent.exists())
						{
							// Create the parent directories then the new file
							if(!parent.mkdirs())
							{
								logger.log(Level.WARNING, "Unable to create the parent directory");
								call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the parent directory"));
							}
						}
					}
					
					// Create the new file
					try
					{
						if(file.createNewFile())
						{
							if(call.getInput() == null)
							{
								call.setStatus(Statuses.SUCCESS_NO_CONTENT);
							}
							else
							{
								ByteUtils.write(call.getInput().getStream(), new FileOutputStream(tmp));
								call.setStatus(Statuses.SUCCESS_OK);
							}
						}
						else
						{
							logger.log(Level.WARNING, "Unable to create the new file");
							call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new file"));
						}
					}
					catch (FileNotFoundException fnfe)
					{
						logger.log(Level.WARNING, "Unable to create the new file", fnfe);
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new file"));
					}
					catch (IOException ioe)
					{
						logger.log(Level.WARNING, "Unable to create the new file", ioe);
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Unable to create the new file"));
					}
				}
			}
		}
		else if(call.getMethod().equals(Methods.DELETE))
		{
			if(file.delete())
			{
				call.setStatus(Statuses.SUCCESS_NO_CONTENT);
			}
			else
			{
				if(file.isDirectory())
				{
					if(file.listFiles().length == 0)
					{
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Couldn't delete the empty directory"));
					}
					else
					{
						call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Couldn't delete the non-empty directory"));
					}
				}
				else
				{
					call.setStatus(new DefaultStatus(Statuses.SERVER_ERROR_INTERNAL, "Couldn't delete the file"));
				}
			}
		}
		else
		{
			call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
   }

}
