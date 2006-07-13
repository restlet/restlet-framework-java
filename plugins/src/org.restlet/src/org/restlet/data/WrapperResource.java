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

import java.util.List;

import org.restlet.WrapperRestlet;

/**
 * Resource wrapper. Useful for application developer who need to enrich the resource 
 * with application related properties and behavior.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperResource extends WrapperRestlet implements Resource
{
   /**
    * Constructor.
    * @param wrappedResource The wrapped resource.
    */
   public WrapperResource(Resource wrappedResource)
   {
   	super(wrappedResource);
   }

   /**
    * Returns the wrapped Resource.
    * @return The wrapped Resource.
    */
   public Resource getWrappedResource()
   {
   	return (Resource)getWrappedRestlet();
   }
   
	/**
	 * Returns the official identifier.
	 * @return The official identifier.
	 */
	public Reference getIdentifier()
	{
		return getWrappedResource().getIdentifier();
	}
	
	/**
	 * Sets the official identifier.
	 * @param identifier The official identifier.
	 */
	public void setIdentifier(Reference identifier)
	{
		getWrappedResource().setIdentifier(identifier);
	}
	
	/**
	 * Returns the list of all the identifiers for the resource. The list is composed of the official identifier
	 * followed by all the alias identifiers.
	 * @return The list of all the identifiers for the resource.
	 */
	public ReferenceList getIdentifiers()
	{
		return getWrappedResource().getIdentifiers();
	}
	
	/**
	 * Returns the list of variants. Each variant is described by metadata and can provide several instances 
	 * of the variant's representation.
	 * @return The list of variants.
	 */
	public List<Representation> getVariants()
	{
		return getWrappedResource().getVariants();
	}

}
