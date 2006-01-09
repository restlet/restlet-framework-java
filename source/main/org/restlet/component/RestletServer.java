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

package org.restlet.component;

/**
 * Origin server composed of restlets containers. Each container is managing a resources namespace.
 */
public interface RestletServer extends OriginServer
{
   /**
    * Adds a restlet container.
    * @param name The unique name of the container.
    * @param container The container to add.
    * @return The added container.
    */
   public RestletContainer addContainer(String name, RestletContainer container);

   /**
    * Removes a restlet container.
    * @param name The name of the container to remove.
    */
   public void removeContainer(String name);

   /**
    * Returns the default container handling direct calls to the server.
    * @return The default container.
    */
   public RestletContainer getDefaultContainer();

   /**
    * Sets the default container handling direct calls to the server.
    * @param container The default container.
    */
   public void setDefaultContainer(RestletContainer container);

}
