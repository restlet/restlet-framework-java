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

package org.restlet.ext.data;

import java.io.File;
import java.io.IOException;

import org.restlet.data.Reference;

/**
 * Reference to a file resource.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class FileReference extends Reference
{
	/**
	 * Constructor.
	 * @param ref File reference.
	 */
	public FileReference(Reference ref)
	{
		super(ref.toString());
	}

	/**
	 * Constructor.
	 * @param file The file whose path must be used.
	 * @throws IOException
	 */
	public FileReference(File file) throws IOException
	{
		this(file.getCanonicalPath());
	}

	/**
	 * Constructor.
	 * @param filePath The local file path.
	 */
	public FileReference(String filePath)
	{
		this("", filePath);
	}
	
	/**
	 * Constructor.
	 * @param hostName The authority (can be a host name or the special "localhost" or an empty value).
	 * @param filePath The file path.
	 */
	public FileReference(String hostName, String filePath)
	{
		super("file://" + hostName + "/" + normalizePath(filePath));
	}

	/**
	 * Gets the local file corresponding to the reference. Only URIs referring to the "localhost" or to an empty
	 * authority are supported.
	 * @return The local file corresponding to the reference.
	 */
	public File getFile()
	{
		File result = null;
		
		String hostName = getAuthority();
		
		if((hostName == null) || hostName.equals("") || hostName.equalsIgnoreCase("localhost"))
		{
			String filePath = getPath();
			result = new File(filePath);
		}
		else
		{
			throw new RuntimeException("Can't resolve files on remote host machines");
		}
		
		return result;
	}
	
   /**
    * Localize a path by converting all the separator characters to the system-dependant separator character.
    * @param path The path to localize.
    * @return The localized path.
    */
   public static String localizePath(String path)
   {
      StringBuilder result = new StringBuilder();
      char nextChar;
      for(int i = 0; i < path.length(); i++)
      {
         nextChar = path.charAt(i);
         if((nextChar == '/') || (nextChar == '\\'))
         {
            // Convert the URI separator to the system dependent path separator
            result.append(File.separatorChar);
         }
         else
         {
            result.append(nextChar);
         }
      }

      return result.toString();
   }

   /**
    * Normalize a path by converting all the system-dependant separator characters to the standard '/' 
    * separator character.
    * @param path The path to normalize.
    * @return The normalize path.
    */
   public static String normalizePath(String path)
   {
      StringBuilder result = new StringBuilder();
      char nextChar;
      for(int i = 0; i < path.length(); i++)
      {
         nextChar = path.charAt(i);
         if((nextChar == '\\'))
         {
            // Convert the Windows style path separator to the standard path separator
            result.append('/');
         }
         else
         {
            result.append(nextChar);
         }
      }

      return result.toString();
   }
	
}
