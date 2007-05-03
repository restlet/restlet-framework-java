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

package com.noelios.restlet.impl;

import java.util.regex.Pattern;

import org.restlet.Restlet;

/**
 * Represents a mapping between a path pattern and a target Restlet.
 * @see java.util.regex.Pattern
 */
public class RestletMapping extends RestletTarget
{
   /** The URI pattern. */
   Pattern pattern;

   /**
    * Constructor.
    * @param pattern The URI pattern.
    * @param target The target interface.
    */
   public RestletMapping(String pattern, Restlet target)
   {
      super(target);
      this.pattern = Pattern.compile(pattern);
   }

   /**
    * Constructor.
    * @param pattern The URI pattern.
    * @param targetClass The target class.
    */
   public RestletMapping(String pattern, Class<? extends Restlet> targetClass)
   {
      super(targetClass);
      this.pattern = Pattern.compile(pattern);
   }

   /**
    * Returns the URI pattern.
    * @return The URI pattern.
    */
   public Pattern getPattern()
   {
      return this.pattern;
   }

}
