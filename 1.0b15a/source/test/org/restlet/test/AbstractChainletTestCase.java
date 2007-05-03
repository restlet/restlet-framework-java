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

import org.restlet.Call;
import org.restlet.Chainlet;
import org.restlet.Restlet;

/**
 * Tests where every chainlet should run through.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public abstract class AbstractChainletTestCase extends RestletTestCase
{
	/**
	 * Returns a chainlet to be used for the tests. 
	 *
	 * @return Chainlet instance.
	 */
	protected abstract Chainlet getChainlet();

	/**
	 * Returns a call. 
	 *
	 * @return Call instance.
	 */
	protected abstract Call getCall();

	/**
	 * Returns a restlet. 
	 *
	 * @return Restlet instance.
	 */
	protected abstract Restlet getRestlet();

	/**
	 * Returns a restlet class. 
	 *
	 * @return Restlet class.
	 */
	protected abstract Class getRestletClass();

	/**
	 * Test Restlet instance attaching/detaching. 
	 */
	public void testAttachDetachInstance() throws Exception
	{
		Chainlet chainlet = getChainlet();
		assertFalse(chainlet.hasTarget());
		chainlet.attach(getRestlet());
		chainlet.start();
		assertTrue(chainlet.isStarted());
		assertFalse(chainlet.isStopped());
		Call call = getCall();
		chainlet.handle(call);
		assertTrue(chainlet.hasTarget());
		chainlet.detach();
		assertFalse(chainlet.hasTarget());
	}

	/**
	 * Test Restlet class attaching/detaching. 
	 */
	@SuppressWarnings("unchecked")
	public void testAttachDetachClass() throws Exception
	{
		Chainlet chainlet = getChainlet();
		assertFalse(chainlet.hasTarget());
		chainlet.attach(getRestletClass());
		chainlet.start();
		assertTrue(chainlet.isStarted());
		assertFalse(chainlet.isStopped());
		Call call = getCall();
		chainlet.handle(call);
		assertTrue(chainlet.hasTarget());
		chainlet.detach();
		assertFalse(chainlet.hasTarget());
	}

	/**
	 * Test with null target. 
	 */
	public void testIllegalTarget() throws Exception
	{
		Chainlet chainlet = getChainlet();
		chainlet.start();
		assertTrue(chainlet.isStarted());
		assertFalse(chainlet.isStopped());
		assertFalse(chainlet.hasTarget());
		Call call = getCall();
		try
		{
			chainlet.handle(call);
			fail("Chainlet handles call without a target");
		}
		catch (Exception ex)
		{
			// noop.
		}
	}

	/**
	 * Test not started chainlet. 
	 */
	public void testIllegalStartedState() throws Exception
	{
		Chainlet chainlet = getChainlet();
		chainlet.attach(getRestlet());
		assertTrue(chainlet.hasTarget());
		assertFalse(chainlet.isStarted());
		assertTrue(chainlet.isStopped());
		Call call = getCall();
		try
		{
			chainlet.handle(call);
			fail("Chainlet handles call without being started");
		}
		catch (Exception ex)
		{
			// noop.
		}
	}

}
