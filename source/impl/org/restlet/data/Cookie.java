/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
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




