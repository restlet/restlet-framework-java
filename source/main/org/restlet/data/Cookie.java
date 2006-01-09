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
 * Cookie received from a user agent.
 */
public interface Cookie extends Parameter
{
   /**
    * Returns the cookie specification version.
    * @return The cookie specification version.
    */
   public int getVersion();

   /**
    * Sets the cookie specification version.
    * @param version The cookie specification version.
    */
   public void setVersion(int version);

   /**
    * Returns the validity path.
    * @return The validity path.
    */
   public String getPath();

   /**
    * Sets the validity path.
    * @param path The validity path.
    */
   public void setPath(String path);

   /**
    * Returns the domain name.
    * @return The domain name.
    */
   public String getDomain();

   /**
    * Sets the domain name.
    * @param domain The domain name.
    */
   public void setDomain(String domain);

}
