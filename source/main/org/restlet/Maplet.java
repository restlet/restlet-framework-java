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

package org.restlet;

/**
 * Mapper of calls to other restlets. Automatic delegation is provided for attached restlets.
 * Note that during the delegation, the restlet call paths are supposed to be modified.
 */
public interface Maplet extends Restlet
{
   /**
    * Attaches a target instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, UniformInterface target);

   /**
    * Attaches a target class. A new instance will be created for each call.
    * @param pathPattern The path pattern used to map calls.
    * @param targetClass The target class to attach (can have a constructor taking a RestletContainer
    * parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends UniformInterface> targetClass);

   /**
    * Detaches a target instance.
    * @param target The target instance to detach.
    */
   public void detach(UniformInterface target);

   /**
    * Detaches a target class.
    * @param targetClass The restlet class to detach.
    */
   public void detach(Class<? extends UniformInterface> targetClass);

   /**
    * Delegates a call to one of the attached targets.<br/>
    * If no delegation is possible, a 404 error status (Client error, Not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(UniformCall call);

}
