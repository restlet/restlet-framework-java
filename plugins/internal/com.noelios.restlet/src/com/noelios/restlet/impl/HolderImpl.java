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

package com.noelios.restlet.impl;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Handler;
import org.restlet.Holder;
import org.restlet.data.Request;
import org.restlet.data.Response;


/**
 * Application implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HolderImpl extends Holder
{
	/** The first handler. */
	private Handler first;
	
	/**
	 * Constructor.
    * @param context The context.
    * @param next The attached handler.
	 */
	public HolderImpl(Context context, Handler next)
	{
		super(context, next);
	}

	/**
	 * Allows filtering before processing by the next handler. Does nothing by default.
    * @param request The request to handle.
    * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		if(getFirst() != null)
		{
			getFirst().handle(request, response);
		}
	}
	
	/** Start hook. */
	public void start() throws Exception
	{
		Filter lastFilter = null;

		// Logging of calls
		if (isLoggingEnabled())
		{
			lastFilter = new LogFilter(getContext(), getLoggingName(), getLoggingFormat());
			setFirst(lastFilter);
		}

		// Addition of status pages
		if (isStatusEnabled())
		{
			Filter statusFilter = new ApplicationStatusFilter(this);
			if (lastFilter != null) lastFilter.setNext(statusFilter);
			if (getFirst() == null) setFirst(statusFilter);
			lastFilter = statusFilter;
		}

		// Reattach the original filter's attached handler
		if (getFirst() == null) 
		{
			setFirst(getNext());
		}
		else
		{
			lastFilter.setNext(getNext());
		}
		
		super.start();
	}

	/**
	 * Returns the first handler.
	 * @return the first handler.
	 */
	private Handler getFirst()
	{
		return this.first;
	}

	/**
	 * Sets the first handler.
	 * @param first The first handler.
	 */
	private void setFirst(Handler first)
	{
		this.first = first;
	}

}
