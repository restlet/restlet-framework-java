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

package org.restlet;

import java.util.List;

/**
 * Restlet call wrapper.<br/>
 * Useful for application developers who need to enrich the call with application related things.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 */
public class RestletCallWrapper extends UniformCallWrapper implements RestletCall
{
   /** The wrapped restlet call. */
   RestletCall wrappedCall;

   /**
    * Constructor.
    * @param wrappedCall The wrapped call
    */
   public RestletCallWrapper(RestletCall wrappedCall)
   {
      super(wrappedCall);
   }

   /**
    * Returns the wrapped call.
    * @return The wrapped call
    */
   public RestletCall getWrappedCall()
   {
      return (RestletCall)super.getWrappedCall();
   }
   
   /**
    * Returns the list of restlets paths. The list is sorted according to the handlers hierarchy.
    * @return The list of restlets paths.
    */
   public List<String> getPaths()
   {
      return getWrappedCall().getPaths();
   }

   /**
    * Returns one of the paths in the list. The first path is the resource path relatively to the current
    * restlet. The second path is the current reslet path relatively to the parent restlet. All the hierarchy
    * of restlet paths is also available depending on the restlet tree.
    * @param index Index of the path in the list.
    * @param strip Indicates if leading and ending slashes should be stripped.
    * @return The path at the given index.
    */
   public String getPath(int index, boolean strip)
   {
      return getWrappedCall().getPath(index, strip);
   }

   /**
    * Returns the list of substring matched in the current restlet's path.
    * @return The list of substring matched.
    * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Matcher.html#group(int)">Matcher.group()</a>
    */
   public List<String> getMatches()
   {
      return getWrappedCall().getMatches();
   }

}
