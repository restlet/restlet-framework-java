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
    * Attaches a restlet instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param restlet The restlet to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Restlet restlet);

   /**
    * Attaches a restlet class. A new instance will be created for each call.
    * @param pathPattern The path pattern used to map calls.
    * @param restletClass The restlet class to attach (must have a constructor taking a RestletContainer
    * parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends Restlet> restletClass);

   /**
    * Detaches a restlet instance.
    * @param restlet The restlet to detach.
    */
   public void detach(Restlet restlet);

   /**
    * Detaches a restlet class.
    * @param restletClass The restlet class to detach.
    */
   public void detach(Class<? extends Restlet> restletClass);

   /**
    * Delegates a call to attached restlets.<br/>
    * If no delegation is possible, an error status (406, not found) will be returned.
    * @param call The call to delegate.
    * @return True if the call was successfully delegated.
    */
   public boolean delegate(UniformCall call);

}
