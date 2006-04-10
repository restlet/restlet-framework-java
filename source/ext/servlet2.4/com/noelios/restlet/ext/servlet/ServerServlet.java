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

package com.noelios.restlet.ext.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.component.Component;
import org.restlet.connector.Server;
import org.restlet.connector.ServerCall;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;

/**
 * Servlet connector acting as a HTTP server.
 * @see <a href="http://java.sun.com/j2ee/">J2EE home page</a>
 */
public class ServerServlet extends HttpServlet implements Server
{
   /** The Servlet context initialization parameter's name containing the target's class name. */
   public static final String NAME_TARGET_CLASS = "org.restlet.target.class";
   public static final String NAME_TARGET_ATTRIBUTE = "org.restlet.target.attribute";

   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** Indicates if the connector was started. */
   protected boolean started;

   /** The handler of Jetty calls. */
   protected UniformInterface handler;

   /**
    * Constructor.
    */
   public ServerServlet()
   {
      this.started = false;
      this.handler = null;
   }
   
   /**
    * Returns the connector's protocol.
    * @return The connector's protocol.
    */
   public Protocol getProtocol()
   {
      return Protocols.HTTP;
   }

   /** Start hook. */
   public void start()
   {
      this.started = true;
   }

   /** Stop hook. */
   public void stop()
   {
      this.started = false;
   }

   /**
    * Indicates if the connector is started.
    * @return True if the connector is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the connector is stopped.
    * @return True if the connector is stopped.
    */
   public boolean isStopped()
   {
      return !isStarted();
   }

   /**
    * Returns the container.
    * @return The container.
    */
   public Component getContainer()
   {
   	return null;
   }

   /**
    * Services a HTTP Servlet request as an uniform call.
    * @param request The HTTP Servlet request.
    * @param response The HTTP Servlet response.
    */
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      handle((ServerCall)new ServletCall(request, response));
   }

   /**
    * Returns the call handler.<br/>
    * For the first invocation, we look for an existing target in the application context, using the NAME_TARGET_ATTRIBUTE parameter.<br/>
    * We lookup for the attribute name in the servlet configuration, then in the application context.<br/>
    * If no target exists, we try to instantiate one based on the class name set in the NAME_TARGET_CLASS parameter.<br/>
    * We lookup for the class name in the servlet configuration, then in the application context.<br/>
    * Once the target is found, we wrap the servlet request and response into a uniform call and ask the target to handle it.<br/>
    * When the handling is done, we write the result back into the result object and return from the service method.
    * @return The call handler.
    */
   public UniformInterface getTarget()
   {
      if(this.handler != null)
      {
         return this.handler;
      }
      else
      {
         synchronized(ServerServlet.class)
         {
            // Look in the application context attribute if another servlet already set the target
            // First, look in the servlet configuration for the attribute name
            String targetAttributeName = getServletConfig().getInitParameter(NAME_TARGET_ATTRIBUTE);
            if(targetAttributeName == null)
            {
               // Then, look in the application context
               targetAttributeName = getServletContext().getInitParameter(NAME_TARGET_ATTRIBUTE);
            }

            if(targetAttributeName != null)
            {
               // Look up the attribute for a target
               this.handler = (UniformInterface)getServletContext().getAttribute(targetAttributeName);

               if(this.handler == null)
               {
                  // Try to instantiate a new target
                  // First, look in the servlet configuration for the class name
                  String targetClassName = getServletConfig().getInitParameter(NAME_TARGET_CLASS);
                  if(targetClassName == null)
                  {
                     // Then, look in the web application context
                     targetClassName = getServletContext().getInitParameter(NAME_TARGET_CLASS);
                  }

                  if(targetClassName != null)
                  {
                     try
                     {
                        // Load the target class using the given class name
                        Class targetClass = Class.forName(targetClassName);

                        // Create a new instance of the target class
                        // and store it for reuse by other ServerServlets.
                        this.handler = (UniformInterface)targetClass.newInstance();
                        getServletContext().setAttribute(NAME_TARGET_ATTRIBUTE, this.handler);
                     }
                     catch(ClassNotFoundException e)
                     {
                        log("[Noelios Restlet Engine] - The ServerServlet couldn't find the class. Please check that your classpath includes " + targetClassName);
                     }
                     catch(InstantiationException e)
                     {
                        log("[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the class. Please check this class has an empty constructor " + targetClassName);
                     }
                     catch(IllegalAccessException e)
                     {
                        log("[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the class. Please check that you have to proper access rights to " + targetClassName);
                     }
                  }
                  else
                  {
                     log("[Noelios Restlet Engine] - The ServerServlet couldn't find the class name of the target handler. Please set the initialization parameter called " + NAME_TARGET_CLASS);
                  }
               }
            }
            else
            {
               log("[Noelios Restlet Engine] - The ServerServlet couldn't find the attribute name of the target handler. Please set the initialization parameter called " + NAME_TARGET_ATTRIBUTE);
            }
         }

         return this.handler;
      }
   }

   /**
    * Sets the call handler.
    * @param handler The call handler.
    */
   public void setTarget(UniformInterface handler)
   {
      this.handler = handler;
   }

   /**
    * Handles the HTTP protocol call.<br/>
    * The default behavior is to create an UniformCall and invoke the "handle(UniformCall)" method.
    * @param call The HTTP protocol call.
    */
   public void handle(ServerCall call) throws IOException
   {
      UniformCall uniformCall = call.toUniform();
      handle(uniformCall);
      call.setResponse(uniformCall);
      call.sendResponseHeaders();
      call.sendResponseOutput(uniformCall.getOutput());
   }

   /**
    * Handles a uniform call.
    * The default behavior is to as the attached handler to handle the call. 
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      getTarget().handle(call);
   }

   /**
    * Returns the name of this REST connector.
    * @return The name of this REST connector.
    */
   public String getName()
   {
      return getServletConfig().getServletName();
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Servlet HTTP server";
   }

   /**
    * Not directly supported.<br/>
    * SSL must be configured at the Servlet Container level. 
    */
   public void configureSsl(String keystorePath, String keystorePassword, String keyPassword)
   {
      throw new IllegalArgumentException("Not directly supported. SSL must be configured at the Servlet Container level.");
   }

}
