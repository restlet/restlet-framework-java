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

import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Tests where every Filter should run through.
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public abstract class AbstractFilterTestCase extends RestletTestCase
{
	/**
	 * Returns a Filter to be used for the tests. 
	 * @return Filter instance.
	 */
	protected abstract Filter getFilter();

	/**
	 * Returns a request. 
	 * @return Request instance.
	 */
	protected abstract Request getRequest();

	/**
	 * Returns a response. 
	 * @param request The associated request.
	 * @return Response instance.
	 */
	protected abstract Response getResponse(Request request);

	/**
	 * Returns a restlet. 
	 * @return Restlet instance.
	 */
	protected abstract Restlet getRestlet();

	/**
	 * Returns a restlet class. 
	 * @return Restlet class.
	 */
	protected abstract Class getRestletClass();

	/**
	 * Test Restlet instance attaching/detaching. 
	 */
	public void testAttachDetachInstance() throws Exception
	{
		Filter filter = getFilter();
		assertFalse(filter.hasNext());
		filter.setNext(getRestlet());
		filter.start();
		assertTrue(filter.isStarted());
		assertFalse(filter.isStopped());
		Request request = getRequest();
		Response response = getResponse(request);
		filter.handle(request, response);
		assertTrue(filter.hasNext());
		filter.setNext(null);
		assertFalse(filter.hasNext());
	}

	/**
	 * Test with null target. 
	 */
	public void testIllegalTarget() throws Exception
	{
		Filter filter = getFilter();
		filter.start();
		assertTrue(filter.isStarted());
		assertFalse(filter.isStopped());
		assertFalse(filter.hasNext());
		Request request = getRequest();
		Response response = getResponse(request);
		try
		{
			filter.handle(request, response);
			fail("Filter handles call without a target");
		}
		catch (Exception ex)
		{
			// noop.
		}
	}

	/**
	 * Test not started Filter. 
	 */
	public void testIllegalStartedState() throws Exception
	{
		Filter filter = getFilter();
		filter.setNext(getRestlet());
		assertTrue(filter.hasNext());
		assertFalse(filter.isStarted());
		assertTrue(filter.isStopped());
		Request request = getRequest();
		Response response = getResponse(request);
		try
		{
			filter.handle(request, response);

			if (!filter.isStarted())
			{
				fail("Filter handles call without being started");
			}
		}
		catch (Exception ex)
		{
			// noop.
		}
	}

}
