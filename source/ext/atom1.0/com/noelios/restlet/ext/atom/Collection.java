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

package com.noelios.restlet.ext.atom;

import org.restlet.Manager;
import org.restlet.RestletCall;
import org.restlet.data.Methods;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.Statuses;

/**
 * Atom Protocol collection, part of a workspace.
 */
public class Collection
{
	/**
	 * The parent workspace.
	 */
	protected Workspace workspace;
	
	/**
	 * The title.
	 */
	protected String title;
	
	/**
	 * The hypertext reference.
	 */
	protected Reference href;
	
	/**
	 * The type of members.
	 */
	protected MemberType memberType;
	
	/**
	 * Constructor.
	 * @param workspace The parent workspace.
	 * @param title The title.
	 * @param href The hypertext reference.
	 */
	public Collection(Workspace workspace, String title, String href)
	{
		this.workspace = workspace;
		this.title = title;
		this.href = Manager.createReference(href);
		this.memberType = null;
	}

	/**
	 * Returns the parent workspace.
	 * @return The parent workspace.
	 */
	public Workspace getWorkspace()
	{
		return this.workspace;
	}
	
	/**
	 * Sets the parent workspace.
	 * @param workspace The parent workspace.
	 */
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
	}
	
	/**
	 * Returns the title.
	 * @return The title.
	 */
	public String getTitle()
	{
		return this.title;
	}

	/**
	 * Sets the title.
	 * @param title The title.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns the hypertext reference.
	 * @return The hypertext reference.
	 */
	public Reference getHref()
	{
		return this.href;
	}

	/**
	 * Sets the hypertext reference.
	 * @param href The hypertext reference.
	 */
	public void setHref(Reference href)
	{
		this.href = href;
	}

	/**
	 * Returns the type of members.
	 * @return The type of members.
	 */
	public MemberType getMemberType()
	{
		return this.memberType;
	}

	/**
	 * Sets the type of members.
	 * @param memberType The type of members.
	 */
	public void setMemberType(MemberType memberType)
	{
		this.memberType = memberType;
	}

	/**
	 * Posts a member to the collection resulting in the creation of a new resource.
	 * @param member The member representation to post.
	 * @return The reference of the new resource.
	 * @throws Exception 
	 */
	public Reference postMember(Representation member) throws Exception 
	{
		RestletCall post = Manager.createCall();
		post.setMethod(Methods.POST);
		post.setResourceRef(getHref());
		post.setInput(member);
		getWorkspace().getService().getClient().handle(post);
		
		if(post.getStatus().equals(Statuses.SUCCESS_CREATED))
		{
			return post.getRedirectionRef();
		}
		else
		{
			throw new Exception("Couldn't post the member representation. Status returned: " + post.getStatus().getDescription());
		}
	}

	/**
	 * Returns the feed representation.  
	 * @return The feed representation.
	 * @throws Exception 
	 */
	public Feed getFeed() throws Exception
	{
		RestletCall get = Manager.createCall();
		get.setMethod(Methods.GET);
		get.setResourceRef(getHref());
		getWorkspace().getService().getClient().handle(get);
		
		if(get.getStatus().equals(Statuses.SUCCESS_OK))
		{
			return new Feed(get.getOutput());
		}
		else
		{
			throw new Exception("Couldn't get the feed representation. Status returned: " + get.getStatus().getDescription());
		}
	}
	
}
