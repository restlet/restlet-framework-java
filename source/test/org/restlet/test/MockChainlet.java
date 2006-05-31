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

import org.restlet.AbstractChainlet;
import org.restlet.Call;

/**
 * Thin layer around an AbstractChainlet.
 * Takes care about being started and having a target when it should handle
 * a call.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public class MockChainlet extends AbstractChainlet
{

	/* (non-Javadoc)
	 * @see org.restlet.AbstractChainlet#handle(org.restlet.Call)
	 */
	@Override
	public void handle(Call call)
	{
		if (!super.isStarted())
		{
			throw new IllegalStateException("Chainlet is not started");
		}
		if (!super.hasTarget())
		{
			throw new IllegalStateException("Target is not set");
		}
		super.handle(call);
	}

}
