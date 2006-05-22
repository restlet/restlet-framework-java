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

/**
 * Fluent builder for any object.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DefaultBuilder
{
	/** The parent builder. */
	protected DefaultBuilder parent;
	
	/** The wrapped node. */
	protected Object node;
	
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
	public DefaultBuilder(DefaultBuilder parent, Object node)
	{
		this.parent = parent;
		this.node = node;
	}
	
   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
	public Object getNode()
	{
		return this.node;
	}

	/**
	 * Go up one level in the builders tree.
	 * @return The parent builder.
	 */
	public DefaultBuilder up()
	{
		return parent;
	}

	/**
	 * Go up several levels in the builders tree.
	 * @return The ancestor builder.
	 */
	public DefaultBuilder up(int levels)
	{
		DefaultBuilder result = parent;
		for(int i = 0; (result != null) && (i < levels - 1); i++)
		{
			result = result.up();
		}
		
		return result;
	}

	/**
	 * Go up to the first parent Maplet builder.
	 * @return The parent Maplet builder.
	 */
	public MapletBuilder upMaplet()
	{
		MapletBuilder result = null;
		DefaultBuilder current = this;

		for(boolean goUp = true; (result == null) && goUp; )
		{
			goUp = (current.up() != null);
			if(goUp) current = current.up();
			if(current instanceof MapletBuilder) result = (MapletBuilder)current;
		}
		
		return result;
	}

	/**
	 * Go to the root of the builders tree.
	 * @return The root builder.
	 */
	public DefaultBuilder root()
	{
		DefaultBuilder result = this;

		for(boolean goUp = true; goUp; )
		{
			goUp = (result.up() != null);
			if(goUp) result = result.up();
		}
		
		return result;
	}

	/**
	 * Casts the current builder. 
	 * @return A Maplet builder.
	 */
	public MapletBuilder toMaplet()
	{
		return (MapletBuilder)this;
	}

	/**
	 * Casts the current builder. 
	 * @return A Chainlet builder.
	 */
	public ChainletBuilder toChainlet()
	{
		return (ChainletBuilder)this;
	}

	/**
	 * Casts the current builder. 
	 * @return A Component builder.
	 */
	public ComponentBuilder toComponent()
	{
		return (ComponentBuilder)this;
	}

}
