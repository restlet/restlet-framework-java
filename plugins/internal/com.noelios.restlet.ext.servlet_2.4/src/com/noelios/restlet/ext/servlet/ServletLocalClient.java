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

package com.noelios.restlet.ext.servlet;

import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import org.restlet.Call;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;
import org.restlet.data.Status;

import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.impl.connector.LocalClient;

/**
 * Local client connector based on a Servlet context (JEE Web application context).
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServletLocalClient extends LocalClient
{
   /** The Servlet context to use. */
	private ServletContext servletContext;

   /**
    * Constructor.
    * @param servletContext The Servlet context.
    */
   public ServletLocalClient(ServletContext servletContext)
   {
      this.servletContext = servletContext;
   }
   
   /**
    * Returns the Servlet context.
    * @return The Servlet context.
    */
   public ServletContext getServletContext()
   {
      return this.servletContext;
   }

   /**
    * Handles a call using the current Web Application.
    * @param call The call to handle.
    */
   protected void handleWebApp(Call call)
   {
      if(call.getMethod().equals(Method.GET) || call.getMethod().equals(Method.HEAD))
		{
      	String basePath = call.getResourceRef().toString();
      	int lastSlashIndex = basePath.lastIndexOf('/');
      	String entry = (lastSlashIndex == -1) ? basePath : basePath.substring(lastSlashIndex + 1);
      	Representation output = null;
			
			if(basePath.endsWith("/"))
			{
				// Return the directory listing
				Set entries = getServletContext().getResourcePaths(basePath);
				ReferenceList rl = new ReferenceList(entries.size());
				rl.setListRef(call.getResourceRef());
				
				for(Iterator iter = entries.iterator(); iter.hasNext();)
				{
					entry = (String)iter.next();
					rl.add(new Reference(basePath + entry.substring(basePath.length())));
				}
				
				output = rl.getRepresentation();
			}
			else
			{
				// Return the entry content
            output = new InputRepresentation(getServletContext().getResourceAsStream(basePath), getDefaultMediaType());
            updateMetadata(entry, output);
            
            // See if the Servlet context specified a particular Mime Type
            String mediaType = getServletContext().getMimeType(basePath);
            
            if(mediaType != null)
            {
            	output.setMediaType(new MediaType(mediaType));
            }
			}
			
			call.setOutput(output);
			call.setStatus(Status.SUCCESS_OK);
		}
		else
		{
			call.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
   }
   
}
