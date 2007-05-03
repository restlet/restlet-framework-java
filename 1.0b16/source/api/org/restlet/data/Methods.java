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

package org.restlet.data;

/**
 * Enumeration of call methods.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public enum Methods implements Method
{
   CONNECT, COPY, DELETE, GET, HEAD, LOCK, MKCOL, MOVE, OPTIONS, POST, PROPFIND, PROPPATCH, PUT, TRACE, UNLOCK;

   /**
    * Returns the technical name (ex: "DELETE", "PUT", "MOVE").
    * @return The technical name (ex: "DELETE", "PUT", "MOVE").
    */
   public String getName()
   {
      String result = null;

      switch(this)
      {
         case CONNECT:
            result = "CONNECT";
            break;
         case COPY:
            result = "COPY";
            break;
         case DELETE:
            result = "DELETE";
            break;
         case GET:
            result = "GET";
            break;
         case HEAD:
            result = "HEAD";
            break;
         case LOCK:
            result = "LOCK";
            break;
         case MKCOL:
            result = "MKCOL";
            break;
         case MOVE:
            result = "MOVE";
            break;
         case OPTIONS:
            result = "OPTIONS";
            break;
         case POST:
            result = "POST";
            break;
         case PROPFIND:
            result = "PROPFIND";
            break;
         case PROPPATCH:
            result = "PROPPATCH";
            break;
         case PUT:
            result = "PUT";
            break;
         case TRACE:
            result = "TRACE";
            break;
         case UNLOCK:
            result = "UNLOCK";
            break;
      }

      return result;
   }

   /**
    * Returns the description.
    * @return The description.
    */
   public String getDescription()
   {
      String result = null;

      switch(this)
      {
         case CONNECT:
            result = "Used with a proxy that can dynamically switch to being a tunnel (HTTP)";
            break;
         case COPY:
            result = "Create a duplicate of the source resource, identified by the Request-URI, in the destination resource, identified by the URI in the Destination header (WebDAV)";
            break;
         case DELETE:
            result = "Request that the origin server delete the resource identified by the request URI (HTTP, WebDAV)";
            break;
         case GET:
            result = "Retrieve whatever information (in the form of an entity) is identified by the request URI (HTTP, WebDAV)";
            break;
         case HEAD:
            result = "Identical to GET except that the server must not return a message body in the response (HTTP)";
            break;
         case LOCK:
            result = "Used to take out a lock of any access type (WebDAV)";
            break;
         case MKCOL:
            result = "Used to create a new collection (WebDAV)";
            break;
         case MOVE:
            result = "Logical equivalent of a copy, followed by consistency maintenance processing, followed by a delete of the source (WebDAV)";
            break;
         case OPTIONS:
            result = "Request for information about the communication options available on the request/response chain identified by the URI (HTTP)";
            break;
         case POST:
            result = "Request that the origin server accept the entity enclosed in the request as a new subordinate of the resource identified by the request URI (HTTP, WebDAV)";
            break;
         case PROPFIND:
            result = "Retrieve properties defined on the resource identified by the request URI (WebDAV)";
            break;
         case PROPPATCH:
            result = "Process instructions specified in the request body to set and/or remove properties defined on the resource identified by the request URI (WebDAV)";
            break;
         case PUT:
            result = "Request that the enclosed entity be stored under the supplied request URI (HTTP, WebDAV)";
            break;
         case TRACE:
            result = "Used to invoke a remote, application-layer loop-back of the request message (HTTP)";
            break;
         case UNLOCK:
            result = "Remove the lock identified by the lock token from the request URI, and all other resources included in the lock (WebDAV)";
            break;
      }

      return result;
   }

   /**
    * Sets the name of this REST element.
    * @param name The name of this REST element.
    */
   public void setName(String name)
   {
   	// Read-only
   }

   /**
    * Sets the description of this REST element.
    * @param description The description of this REST element.
    */
   public void setDescription(String description)
   {
   	// Read-only
   }

   /**
    * Returns the URI of the specification describing the method.
    * @return The URI of the specification describing the method.
    */
   public String getUri()
   {
      String result = null;
      String httpBase = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html";
      String webdavBase = "http://www.webdav.org/specs/rfc2518.html";

      switch(this)
      {
         case CONNECT:
            result = httpBase + "#sec9.9";
            break;
         case COPY:
            result = webdavBase + "#METHOD_COPY";
            break;
         case DELETE:
            result = httpBase + "#sec9.7";
            break;
         case GET:
            result = httpBase + "#sec9.3";
            break;
         case HEAD:
            result = httpBase + "#sec9.4";
            break;
         case LOCK:
            result = webdavBase + "#METHOD_LOCK";
            break;
         case MKCOL:
            result = webdavBase + "#METHOD_MKCOL";
            break;
         case MOVE:
            result = webdavBase + "#METHOD_MOVE";
            break;
         case OPTIONS:
            result = httpBase + "#sec9.2";
            break;
         case POST:
            result = httpBase + "#sec9.5";
            break;
         case PROPFIND:
            result = webdavBase + "#METHOD_PROPFIND";
            break;
         case PROPPATCH:
            result = webdavBase + "#METHOD_PROPPATCH";
            break;
         case PUT:
            result = httpBase + "#sec9.6";
            break;
         case TRACE:
            result = httpBase + "#sec9.8";
            break;
         case UNLOCK:
            result = webdavBase + "#METHOD_UNLOCK";
            break;
      }

      return result;
   }

   /**
    * Indicates if the method is equal to a given one.
    * @param method The method to compare to.
    * @return True if the method is equal to a given one.
    */
   public boolean equals(Method method)
   {
      return getName().equalsIgnoreCase(method.getName());
   }

   /**
    * Creates a new method by attempting to reuse an existing enumeration entry.
    * @param methodName The method name.
    * @return The new method.
    */
   public static Method create(String methodName)
   {
   	Method result = null;
   	
      if(methodName != null)
      {
         if(methodName.equalsIgnoreCase(Methods.GET.getName())) result = Methods.GET;
         else if(methodName.equalsIgnoreCase(Methods.POST.getName())) result = Methods.POST;
         else if(methodName.equalsIgnoreCase(Methods.HEAD.getName())) result = Methods.HEAD;
         else if(methodName.equalsIgnoreCase(Methods.OPTIONS.getName())) result = Methods.OPTIONS;
         else if(methodName.equalsIgnoreCase(Methods.PUT.getName())) result = Methods.PUT;
         else if(methodName.equalsIgnoreCase(Methods.DELETE.getName())) result = Methods.DELETE;
         else if(methodName.equalsIgnoreCase(Methods.CONNECT.getName())) result = Methods.CONNECT;
         else if(methodName.equalsIgnoreCase(Methods.COPY.getName())) result = Methods.COPY;
         else if(methodName.equalsIgnoreCase(Methods.LOCK.getName())) result = Methods.LOCK;
         else if(methodName.equalsIgnoreCase(Methods.MKCOL.getName())) result = Methods.MKCOL;
         else if(methodName.equalsIgnoreCase(Methods.MOVE.getName())) result = Methods.MOVE;
         else if(methodName.equalsIgnoreCase(Methods.PROPFIND.getName())) result = Methods.PROPFIND;
         else if(methodName.equalsIgnoreCase(Methods.PROPPATCH.getName())) result = Methods.PROPPATCH;
         else if(methodName.equalsIgnoreCase(Methods.TRACE.getName())) result = Methods.TRACE;
         else if(methodName.equalsIgnoreCase(Methods.UNLOCK.getName())) result = Methods.UNLOCK;
         else result = new DefaultMethod(methodName);
      }
   	
      return result;
   }
   
}
