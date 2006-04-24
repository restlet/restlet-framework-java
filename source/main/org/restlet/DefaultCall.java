/*
 * Copyright 2005-2006 Jerome LOUVEL
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

import org.restlet.data.Method;
import org.restlet.data.Reference;

/**
 * Default Restlet call that can be easily subclassed.<br/>
 * Useful for application developer who need to call client connectors.
 */
public class DefaultCall extends WrapperCall
{
   /**
    * Constructor.
    */
   public DefaultCall()
   {
      super(Factory.getInstance().createCall());
   }
   
   /**
    * Constructor.
    * @param method The call's method.
    * @param resourceRef The resource reference.
    */
   public DefaultCall(Method method, Reference resourceRef)
   {
   	this();
   	setMethod(method);
   	setResourceRef(resourceRef);
   }
   
   /**
    * Constructor.
    * @param method The call's method.
    * @param resourceUri The resource URI.
    */
   public DefaultCall(Method method, String resourceUri)
   {
   	this();
   	setMethod(method);
   	setResourceRef(resourceUri);
   }
   
}
