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

package com.noelios.restlet.data;

import org.restlet.data.Tag;

/**
 * Default validation tag implementation.
 */
public class TagImpl implements Tag
{
   /** The opaque tag string. */
   protected String tag;

   /** The tag weakness. */
   protected boolean weak;

   /**
    * Constructor.
    * @param tag
    * @param weak
    */
   public TagImpl(String tag, boolean weak)
   {
      this.tag = tag;
      this.weak = weak;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Representation tag";
   }

   /**
    * Returns the equivalent HTTP string.
    * @return The equivalent HTTP string.
    * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP Entity Tags</a>
    */
   public String getName()
   {
      StringBuilder sb = new StringBuilder();
      if(isWeak()) sb.append("W/");
      return sb.append('"').append(getOpaqueTag()).append('"').toString();
   }

   /**
    * Indicates if the tag is weak.
    * @return True if the tag is weak, false if the tag is strong.
    */
   public boolean isWeak()
   {
      return weak;
   }

   /**
    * Sets the tag weakness.
    * @param weak True if the tag is weak, false if the tag is strong.
    */
   public void setWeak(boolean weak)
   {
      this.weak = weak;
   }

   /**
    * Returns the opaque tag string.
    * @return The opaque tag string.
    */
   public String getOpaqueTag()
   {
      return tag;
   }

   /**
    * Sets the opaque tag string.
    * @param tag The opaque tag string.
    */
   public void setOpaqueTag(String tag)
   {
      this.tag = tag;
   }

}
