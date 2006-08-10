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

package org.restlet;

import java.util.logging.Logger;

import org.restlet.data.Reference;

/**
 * Data associated to a call that are contextual to the current Restlet handling it. They may not 
 * necessarily change for each Restlet in the processing chain, but they can potentially change while
 * the other call's data are expected to be more stable during the processing.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContextData
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(ContextData.class.getCanonicalName());

	/** The base reference. */
	protected Reference baseRef;
	
	/** The parent call. */
	protected Call call;

	/**
	 * Constructor. 
	 * @param call The parent call.
	 */
	public ContextData(Call call)
	{
		this.baseRef = null;
		this.call = call;
	}
	
	/**
	 * Forwards a call to the owner component for processing. This can be useful when some sort of internal 
	 * redirection or dispatching is needed. Note that you can pass either an existing call or a fresh call 
	 * instance to this method. When the method returns, verification and further processing can still be 
	 * done, the client will only receive the response to the call when the Restlet handle method returns. 
	 * @param call The call to forward.
	 */
	public void forward(Call call)
	{
//		setBaseRef(null);
//		getOwner().handle(call);
	}

	/**
	 * Returns the base reference.
	 * @return The base reference.
	 */
	public Reference getBaseRef()
	{
	   return this.baseRef;
	}

	/**
	 * Sets the base reference that will serve to compute relative resource references.
	 * @param baseUri The base absolute URI.
	 */
	public void setBaseRef(String baseUri)
	{
		setBaseRef(new Reference(baseUri));
	}
	
	/**
	 * Sets the base reference that will serve to compute relative resource references.
	 * @param baseRef The base reference.
	 */
	public void setBaseRef(Reference baseRef)
	{
	   if(this.call.getResourceRef() == null)
	   {
	      logger.warning("You must specify a resource reference before setting a base reference");
	   }
	   else if((baseRef != null) && !baseRef.isParent(this.call.getResourceRef()))
	   {
	      logger.warning("You must specify a base reference that is a parent of the resource reference");
	   }
	
	   this.baseRef = baseRef;
	}

	/**
	 * Returns the resource reference relative to the context's base reference.
	 * @return The relative resource reference.
	 */
	public Reference getRelativeRef()
	{
		return this.call.getResourceRef().getRelativeRef(getBaseRef());
	}

	/**
	 * Returns the resource path relative to the context's base reference.
	 * @return The relative resource path .
	 */
	public String getRelativePath()
	{
		return getRelativeRef().getPath();
	}
	
}
