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

import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Service;

/**
 * Unit test case for the Atom extension.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AtomTest
{
	public static void main(String[] args)
	{
		try
		{
			Service atomService = new Service(
					"http://bitworking.org/projects/pyapp/collection.cgi?introspection=1");
			Feed atomFeed = atomService.getWorkspaces().get(0).getCollections().get(0)
					.getFeed();
			atomFeed.write(System.out);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
