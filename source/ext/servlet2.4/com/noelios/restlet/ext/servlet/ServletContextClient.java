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
import org.restlet.component.Component;
import org.restlet.data.DefaultMediaType;
import org.restlet.data.Methods;
import org.restlet.data.ParameterList;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Representation;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.ContextReference;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ContextReference.AuthorityType;
import com.noelios.restlet.impl.ContextClient;

/**
 * Context client connector based on a Servlet context (JEE Web application context).
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServletContextClient extends ContextClient
{
   /** The Servlet context to use. */
   protected ServletContext context;

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param context The Servlet context.
    */
   public ServletContextClient(Component owner, ParameterList parameters, ServletContext context)
   {
      super(owner, parameters);
      this.context = context;
   }
   
   /**
    * Returns the Servlet context.
    * @return The Servlet context.
    */
   public ServletContext getContext()
   {
      return this.context;
   }

   /**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      String scheme = call.getResourceRef().getScheme();
      
      if(scheme.equalsIgnoreCase("file"))
      {
      	handleFile(call);
      }
      else if(scheme.equalsIgnoreCase("context"))
      {
			ContextReference cr = new ContextReference(call.getResourceRef());
			
      	if(cr.getAuthorityType() == AuthorityType.CLASS)
      	{
      		handleClassLoader(call, getClass().getClassLoader());
      	}
      	else if(cr.getAuthorityType() == AuthorityType.SYSTEM)
      	{
      		handleClassLoader(call, ClassLoader.getSystemClassLoader());
      	}
      	else if(cr.getAuthorityType() == AuthorityType.THREAD)
      	{
      		handleClassLoader(call, Thread.currentThread().getContextClassLoader());
      	}
      	else if(cr.getAuthorityType() == AuthorityType.WEB_APPLICATION)
      	{
      		handleServletContext(call);
	      }
      }
      else
      {
         throw new IllegalArgumentException("Protocol not supported by the connector. Only FILE and CONTEXT are supported.");
      }
   }

   protected void handleServletContext(Call call)
   {
      if(call.getMethod().equals(Methods.GET) || call.getMethod().equals(Methods.HEAD))
		{
      	String basePath = call.getResourceRef().toString();
      	int lastSlashIndex = basePath.lastIndexOf('/');
      	String entry = (lastSlashIndex == -1) ? basePath : basePath.substring(lastSlashIndex + 1);
			Representation output = null;
			
			if(basePath.endsWith("/"))
			{
				// Return the directory listing
				Set entries = getContext().getResourcePaths(basePath);
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
            output = new InputRepresentation(getContext().getResourceAsStream(basePath), getDefaultMediaType());
            updateMetadata(entry, output.getMetadata());
            
            // See if the Servlet context specified a particular Mime Type
            String mediaType = getContext().getMimeType(basePath);
            
            if(mediaType != null)
            {
            	output.getMetadata().setMediaType(new DefaultMediaType(mediaType));
            }
			}
			
			call.setOutput(output);
			call.setStatus(Statuses.SUCCESS_OK);
		}
		else
		{
			call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
   }
   
}
