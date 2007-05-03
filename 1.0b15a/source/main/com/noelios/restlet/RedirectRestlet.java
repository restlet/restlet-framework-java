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

package com.noelios.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.data.Reference;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.CallModel;
import com.noelios.restlet.util.StringTemplate;

/**
 * Rewrites URIs then redirects the call or the client to a new destination.
 * @see com.noelios.restlet.util.StringTemplate
 * @see com.noelios.restlet.util.CallModel
 * @see <a href="http://www.restlet.org/tutorial#part10">Tutorial: URI rewriting and redirection</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class RedirectRestlet extends AbstractRestlet
{
   /**
    * In this mode, the client is permanently redirected to the URI generated from the target URI pattern.<br/>
    * See Statuses.REDIRECTION_MOVED_PERMANENTLY.  
    */
   public static final int MODE_CLIENT_PERMANENT = 1;

   /**
    * In this mode, the client is simply redirected to the URI generated from the target URI pattern.<br/>
    * See Statuses.REDIRECTION_FOUND. 
    */
   public static final int MODE_CLIENT_FOUND = 2;

   /**
    * In this mode, the client is temporarily redirected to the URI generated from the target URI pattern.<br/>
    * See Statuses.REDIRECTION_MOVED_TEMPORARILY.  
    */
   public static final int MODE_CLIENT_TEMPORARY = 3;

   /**
    * In this mode, the call is sent to the connector indicated by the "connectorName" property. 
    * Once the connector has completed the call handling, the call is normally returned to the client.
    * In this case, you can view the RedirectRestlet as acting as a proxy Restlet.<br/>
    * Remember to attach the connector you want to use to the parent Restlet container, using the exact same
    * name as the one you provided to the setConnectorName method. 
    */
   public static final int MODE_CONNECTOR = 4;

   /**
    * In this mode, the call is internally redirected within the parent component. This is useful when 
    * there are multiple ways to access to the same resource.<br/>
    * Be careful when specifying the target pattern or infinite loops may occur.
    */
   public static final int MODE_INTERNAL = 5;

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
    * @param parent The parent component.
    * @param targetPattern The pattern to build the target URI (using StringTemplate syntax and the CallModel for variables).
    * @param mode The redirection mode.
    * @see com.noelios.restlet.util.StringTemplate
    * @see com.noelios.restlet.util.CallModel
    */
   public RedirectRestlet(Component parent, String targetPattern, int mode)
   {
      super(parent);
      this.targetPattern = targetPattern;
      this.mode = mode;
      this.connectorName = null;
   }

   /**
    * Constructor for the connector mode.
    * @param parent The parent component.
    * @param targetPattern The pattern to build the target URI (using StringTemplate syntax and the CallModel for variables).
    * @param connectorName The connector name.
    * @see com.noelios.restlet.util.StringTemplate
    * @see com.noelios.restlet.util.CallModel
    */
   public RedirectRestlet(Component parent, String targetPattern, String connectorName)
   {
      super(parent);
      this.targetPattern = targetPattern;
      this.mode = MODE_CONNECTOR;
      this.connectorName = connectorName;
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
	public void handle(Call call)
   {
      // Create the template engine
      StringTemplate te = new StringTemplate(this.targetPattern);

      // Create the template data model
      String targetUri = te.process(new CallModel(call, ""));
      Reference target = new Reference(targetUri);

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
            getOwner().callClient(this.connectorName, call);
         break;

         case MODE_INTERNAL:
            logger.log(Level.INFO, "Redirecting internally: " + targetUri);
            call.setResourceRef(target);
            getOwner().handle(call);
         break;
      }
   }

}
