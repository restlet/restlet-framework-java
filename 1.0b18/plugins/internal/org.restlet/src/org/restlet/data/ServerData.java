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

package org.restlet.data;

/**
 * Server specific data related to a call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServerData
{
   /** The server IP address. */
	private String address;

   /** The server name. */
	private String name;

   /**
    * Returns the server's IP address.
    * @return The server's IP address.
    */
   public String getAddress()
   {
      return this.address;
   }

   /**
    * Returns the server's name (ex: web server name).
    * @return The server's name.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Sets the server's IP address.
    * @param address The server's IP address.
    */
   public void setAddress(String address)
   {
      this.address = address;
   }

   /**
    * Sets the server's name (ex: web server name).
    * @param name The server's name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

}
