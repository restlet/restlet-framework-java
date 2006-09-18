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

import com.noelios.restlet.DirectoryFinder;

/**
 * Fluent builder for directory handlers.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @see com.noelios.restlet.DirectoryFinder
 */
public class DirectoryFinderBuilder extends RestletBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public DirectoryFinderBuilder(ObjectBuilder parent, DirectoryFinder node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public DirectoryFinder getNode()
   {
      return (DirectoryFinder)super.getNode();
   }

	/** 
	 * Indicates if content negotation should be enabled.
	 * @param negotiate True if content negotation should be enabled.
    * @return The current builder.
	 */
   public DirectoryFinderBuilder negotiate(boolean negotiate)
   {
   	getNode().setNegotiationEnabled(negotiate);
   	return this;
   }

	/**
	 * Indicates if the display of directory listings is allowed when no index file is found.
	 * @param allow True if the display of directory listings is allowed when no index file is found.
    * @return The current builder.
    */
   public DirectoryFinderBuilder listing(boolean allow)
   {
   	getNode().setListingAllowed(allow);
   	return this;
   }

   /**
    * Indicates if the subdirectories are deeply accessible (true by default).
    * @param deeplyAccessible True if the subdirectories are deeply accessible.
    * @return The current builder.
    */
   public DirectoryFinderBuilder deeply(boolean deeplyAccessible)
   {
   	getNode().setDeeplyAccessible(deeplyAccessible);
   	return this;
   }

   /** 
    * Indicates if modifications to context resources are allowed.
    * @param modifiable True if modifications to context resources are allowed.
    */
   public DirectoryFinderBuilder modifiable(boolean modifiable)
   {
   	getNode().setModifiable(modifiable);
   	return this;
   }

}

