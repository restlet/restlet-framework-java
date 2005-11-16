/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
