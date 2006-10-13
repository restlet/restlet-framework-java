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
package org.restlet.test;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Thin layer around an AbstractFilter.
 * Takes care about being started and having a target when it should handle
 * a call.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public class MockFilter extends Filter
{
	public MockFilter(Context context)
	{
		super(context);
	}

	@Override
	public void handle(Request request, Response response)
	{
		if (!super.isStarted())
		{
			throw new IllegalStateException("Filter is not started");
		}
		if (!super.hasNext())
		{
			throw new IllegalStateException("Target is not set");
		}
		super.handle(request, response);
	}

}
