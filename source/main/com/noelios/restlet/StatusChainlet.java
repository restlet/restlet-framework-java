/*
 * Copyright 2005 Jérôme LOUVEL
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

package com.noelios.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractChainlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.RestletContainer;
import org.restlet.data.MediaTypes;
import org.restlet.data.Representation;
import org.restlet.data.Status;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.StringRepresentation;

/**
 * Chainlet associating an output representation to the filtered calls, 
 * after the handling, based on the call status (error, information, etc.).<br/>
 * In order to customize the default representation, just subclass this class and
 * override the "getRepresentation" method.<br/>
 * If any exception occurs during the call handling, a "server internal error"
 * status is automatically associated to the call. Of course, you can personalize the
 * representation of this error. Also, if no status is set (null), then the 
 * "success ok" status is assumed.<br/>  
 * However, it is important to pay proper attention to
 * the 
 */
public class StatusChainlet extends AbstractChainlet
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.RedirectRestlet");

   /** Indicates whether an existing representation should be overwritten. */
   protected boolean overwrite;

   /** Email address of the administrator to contact in case of error. */
   protected String email;
   
   /**
    * Constructor.
    * @param container The parent container.
    * @param overwrite Indicates whether an existing representation should be overwritten.
    * @param email Email address of the administrator to contact in case of error.  
    */
   public StatusChainlet(RestletContainer container, boolean overwrite, String email)
   {
      super(container);
      this.overwrite = overwrite;
      this.email = email;
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException
   {
      // Normally handle the call
      try
      {
         super.handle(call);
      }
      catch(Exception e)
      {
         logger.log(Level.WARNING, "Unhandled error intercepted", e);
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
      }

      // If no status is set, then the "success ok" status is assumed.
      if(call.getStatus() == null)
      {
         call.setStatus(Statuses.SUCCESS_OK);
      }

      // Do we need to get an output representation for the current status?
      if(!call.getStatus().equals(Statuses.SUCCESS_OK) && ((call.getOutput() != null) || overwrite))
      {
         call.setOutput(getRepresentation(call.getStatus(), call));
      }
   }
   
   /**
    * Returns a representation for the given status.<br/>
    * In order to customize the default representation, this method can be overriden.
    * @param status The status to represent.
    * @param call The related call that was handled.
    * @return The representation of the given status.
    */
   public Representation getRepresentation(Status status, RestletCall call)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("<html>\n");
      sb.append("<head>\n");
      sb.append("   <title>Status page</title>\n");
      sb.append("</head>\n");
      sb.append("<body>\n");

      sb.append("<h3>");
      if(status.getDescription() != null)
      {
         sb.append(status.getDescription());
      }
      else
      {
         sb.append("No description available for this result status");
      }
      sb.append("</h3>");
      sb.append("<p>You can get technical details <a href=\"");
      sb.append(status.getUri());
      sb.append("\">here</a>.<br/>\n");
      
      if(email != null)
      {
         sb.append("For further assistance, you can contact the <a href=\"mailto:");
         sb.append(email);
         sb.append("\">administrator</a>.\n");
      }
      
      sb.append("</p>\n");
      sb.append("</body>\n");
      sb.append("</html>\n");
      
      return new StringRepresentation(sb.toString(), MediaTypes.TEXT_HTML);
   }
   
}
