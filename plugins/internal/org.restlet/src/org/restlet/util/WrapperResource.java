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

package org.restlet.util;

import java.util.List;
import java.util.logging.Logger;

import org.restlet.Resource;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Result;
import org.restlet.representation.Representation;

/**
 * Resource wrapper. Useful for application developer who need to enrich the resource
 * with application related properties and behavior.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperResource extends Resource
{
   /** The wrapped resource. */
   private Resource wrappedResource;

   /**
    * Constructor.
    * @param wrappedResource The wrapped resource.
    */
   public WrapperResource(Resource wrappedResource)
   {
      this.wrappedResource = wrappedResource;
   }

   /**
    * Returns the wrapped Resource.
    * @return The wrapped Resource.
    */
   protected Resource getWrappedResource()
   {
      return this.wrappedResource;
   }

	/**
	 * Indicates if it is allowed to delete the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowDelete()
	{
		return getWrappedResource().allowDelete();
	}

	/**
	 * Indicates if it is allowed to get the variants. The default value is true. 
	 * @return True if the method is allowed.
	 */
	public boolean allowGet()
	{
		return getWrappedResource().allowGet();
	}

	/**
	 * Indicates if it is allowed to post to the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowPost()
	{
		return getWrappedResource().allowPost();
	}

	/**
	 * Indicates if it is allowed to put to the resource. The default value is false. 
	 * @return True if the method is allowed.
	 */
	public boolean allowPut()
	{
		return getWrappedResource().allowPut();
	}

	/**
	 * Asks the resource to delete itself and all its representations.
	 * @return The result information. 
	 */
	public Result delete()
	{
		return getWrappedResource().delete();
	}

	/**
	 * Returns the list of methods allowed on the requested resource.
	 * @return The list of allowed methods.
	 */
	public List<Method> getAllowedMethods()
	{
		return getWrappedResource().getAllowedMethods();
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
	 * Returns the list of all the identifiers for the resource. The list is composed of the official identifier
	 * followed by all the alias identifiers.
	 * @return The list of all the identifiers for the resource.
	 */
	public ReferenceList getIdentifiers()
	{
		return getWrappedResource().getIdentifiers();
	}

	/**
	 * Returns the logger to use.
	 * @return The logger to use.
	 */
	public Logger getLogger()
	{
		return getWrappedResource().getLogger();
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

	/**
	 * Posts a variant representation in the resource.
	 * @param entity The posted entity. 
	 * @return The result information.
	 */
	public Result post(Representation entity)
	{
		return getWrappedResource().post(entity);
	}

	/**
	 * Puts a variant representation in the resource.
	 * @param variant A new or updated variant representation. 
	 * @return The result information.
	 */
	public Result put(Representation variant)
	{
		return getWrappedResource().put(variant);
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
	 * Sets the official identifier from a URI string.
	 * @param identifierUri The official identifier to parse.
	 */
	public void setIdentifier(String identifierUri)
	{
		getWrappedResource().setIdentifier(identifierUri);
	}

	/**
	 * Sets a new list of all the identifiers for the resource.  
	 * @param identifiers The new list of identifiers. 
	 */
	public void setIdentifiers(ReferenceList identifiers)
	{
		getWrappedResource().setIdentifiers(identifiers);
	}

	/**
	 * Sets the logger to use.
	 * @param logger The logger to use.
	 */
	public void setLogger(Logger logger)
	{
		getWrappedResource().setLogger(logger);
	}

	/**
	 * Sets a new list of variants. 
	 * @param variants The new list of variants.
	 */
	public void setVariants(List<Representation> variants)
	{
		getWrappedResource().setVariants(variants);
	}

}
