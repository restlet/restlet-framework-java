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

package com.noelios.restlet.build;

import com.noelios.restlet.GuardChainlet;

/**
 * Fluent builder for Guard Chainlets.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class GuardChainletBuilder extends ChainletBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped Guard Chainlet.
	 */
   public GuardChainletBuilder(DefaultBuilder parent, GuardChainlet node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public GuardChainlet getNode()
   {
      return (GuardChainlet)getNode();
   }

   /**
    * Adds an authorization entry.
    * @param login The login to authorize.
    * @param password The password associated to the login.
    * @return The current builder.
    */
   public GuardChainletBuilder authorize(String login, String password)
   {
   	getNode().getAuthorizations().put(login, password);
   	return this;
   }
   
}

