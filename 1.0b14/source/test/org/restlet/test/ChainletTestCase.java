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
import org.restlet.DefaultCall;
import org.restlet.Restlet;

/**
 * Test {@link org.restlet.Chainlet}.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public class ChainletTestCase extends AbstractChainletTestCase
{

	public static void main(String[] args)
	{
		junit.awtui.TestRunner.run(ChainletTestCase.class);
	}

	/* (non-Javadoc)
	 * @see org.restlet.AbstractTestChainlet#getChainlet()
	 */
	protected Chainlet getChainlet()
	{
		return new MockChainlet();
	}

	/* (non-Javadoc)
	 * @see org.restlet.AbstractTestChainlet#getCall()
	 */
	protected Call getCall()
	{
		return new DefaultCall();
	}

	/* (non-Javadoc)
	 * @see org.restlet.AbstractTestChainlet#getRestlet()
	 */
	protected Restlet getRestlet()
	{
		return new MockRestlet();
	}

	/* (non-Javadoc)
	 * @see org.restlet.AbstractTestChainlet#getRestletClass()
	 */
	protected Class getRestletClass()
	{
		return MockRestlet.class;
	}

}
