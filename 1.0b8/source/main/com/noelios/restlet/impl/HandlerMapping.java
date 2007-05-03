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

package com.noelios.restlet.impl;

import java.util.regex.Pattern;

import org.restlet.UniformInterface;


/**
 * Represents a mapping between a path pattern and a target handler.
 * @see java.util.regex.Pattern
 */
public class HandlerMapping extends HandlerTarget
{
   /** The path pattern. */
   Pattern pathPattern;

   /**
    * Constructor.
    * @param pathPattern The path pattern.
    * @param target The target interface.
    */
   public HandlerMapping(String pathPattern, UniformInterface target)
   {
      super(target);
      this.pathPattern = Pattern.compile(pathPattern, Pattern.CASE_INSENSITIVE);
   }

   /**
    * Constructor.
    * @param pathPattern The path pattern.
    * @param targetClass The target class.
    */
   public HandlerMapping(String pathPattern, Class<? extends UniformInterface> targetClass)
   {
      super(targetClass);
      this.pathPattern = Pattern.compile(pathPattern, Pattern.CASE_INSENSITIVE);
   }

   /**
    * Returns the path pattern.
    * @return The path pattern.
    */
   public Pattern getPathPattern()
   {
      return this.pathPattern;
   }

}
