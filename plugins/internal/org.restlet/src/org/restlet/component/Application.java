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

package org.restlet.component;

import org.restlet.Restlet;

/**
 * Component holding a user application, typically within a parent container. The role of an 
 * application is to standardize the configuration of an application for aspects such as call logging,
 * authentication, status pages, virtual hosting, etc.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Application extends Component 
{
   /** The parent container. */
	private Container container;
   
   /**
    * Constructor.
    * @param container The parent container.
    */
   public Application(Container container)
   {
      this(container, null);
   }

   /**
    * Constructor.
    * @param container The parent container.
    * @param root The root Restlet.
    */
   public Application(Container container, Restlet root)
   {
   	super(container.getContext(), root);
   }

   /**
	 * Returns the parent container.
	 * @return The parent container.
	 */
	public Container getContainer()
	{
		return this.container;
	}

}
