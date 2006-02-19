/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractRestlet;
import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;
import org.restlet.data.Reference;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.UniformModel;
import com.noelios.restlet.util.StringTemplate;

/**
 * Rewrites URIs then redirects the call or the client to a new destination.
 * @see com.noelios.restlet.util.UniformModel
 */
public class RedirectRestlet extends AbstractRestlet
{
   public static final int MODE_CLIENT_PERMANENT = 1;
   public static final int MODE_CLIENT_FOUND = 2;
   public static final int MODE_CLIENT_TEMPORARY = 3;
   public static final int MODE_CONNECTOR = 4;
   public static final int MODE_CONTAINER = 5;

   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.RedirectRestlet");

   /** The target URI pattern. */
   protected String targetPattern;

   /** The connector name. */
   protected String connectorName;

   /** The redirection mode. */
   protected int mode;

   /**
    * Constructor.
    * @param container The parent container.
    * @param targetPattern The pattern to build the target URI.
    * @param mode The redirection mode.
    */
   public RedirectRestlet(RestletContainer container, String targetPattern, int mode)
   {
      super(container);
      this.targetPattern = targetPattern;
      this.mode = mode;
   }

   /**
    * Sets the connector name.
    * @param name The connector name.
    */
   public void setConnectorName(String name)
   {
      this.connectorName = name;
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      try
      {
         // Create the template engine
         StringTemplate te = new StringTemplate(this.targetPattern);

         // Create the template data model
         String targetUri = te.process(new UniformModel(call, null));
         Reference target = Manager.createReference(targetUri);

         switch(this.mode)
         {
            case MODE_CLIENT_PERMANENT:
               logger.log(Level.INFO, "Permanently redirecting client to: " + targetUri);
               call.setRedirectionRef(target);
               call.setStatus(Statuses.REDIRECTION_MOVED_PERMANENTLY);
            break;

            case MODE_CLIENT_FOUND:
               logger.log(Level.INFO, "Redirecting client to found location: " + targetUri);
               call.setRedirectionRef(target);
               call.setStatus(Statuses.REDIRECTION_FOUND);
            break;
            
            case MODE_CLIENT_TEMPORARY:
               logger.log(Level.INFO, "Temporarily redirecting client to: " + targetUri);
               call.setRedirectionRef(target);
               call.setStatus(Statuses.REDIRECTION_MOVED_TEMPORARILY);
            break;

            case MODE_CONNECTOR:
               logger.log(Level.INFO, "Redirecting to connector " + this.connectorName + ": " + targetUri);
               call.setResourceRef(target);
               call.getResourcePaths().clear();
               call.getResourceMatches().clear();
               getContainer().callClient(this.connectorName, call);
            break;

            case MODE_CONTAINER:
               logger.log(Level.INFO, "Redirecting to container: " + targetUri);
               call.setResourceRef(target);
               call.getResourcePaths().clear();
               call.getResourceMatches().clear();
               getContainer().handle(call);
            break;
         }
      }
      catch(IOException ioe)
      {
         logger.log(Level.WARNING, "Error during redirection", ioe);
      }
   }

}

