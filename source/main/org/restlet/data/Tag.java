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

package org.restlet.data;

/**
 * Validation tag equivalent to the HTTP entity tag.<br/><br/> "A strong entity tag may be shared by two
 * entities of a resource only if they are equivalent by octet equality.<br/> A weak entity tag may be shared
 * by two entities of a resource only if the entities are equivalent and could be substituted for each other
 * with no significant change in semantics."
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP Entity Tags</a>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.2">HTTP Entity Tag Cache
 * Validators</a>
 */
public interface Tag extends Metadata
{
   /**
    * Indicates if the tag is weak.
    * @return True if the tag is weak, false if the tag is strong.
    */
   public boolean isWeak();

   /**
    * Sets the tag weakness.
    * @param weak True if the tag is weak, false if the tag is strong.
    */
   public void setWeak(boolean weak);

   /**
    * Returns the opaque tag string.
    * @return The opaque tag string.
    */
   public String getOpaqueTag();

   /**
    * Sets the opaque tag string.
    * @param tag The opaque tag string.
    */
   public void setOpaqueTag(String tag);

}
