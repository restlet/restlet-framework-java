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

package com.noelios.restlet.test;

import org.restlet.Manager;
import org.restlet.connector.Client;
import org.restlet.data.Protocols;

import com.noelios.restlet.ext.atom.Feed;
import com.noelios.restlet.ext.atom.Service;

/**
 * Unit test case.
 */
public class AtomTest
{
	public static void main(String[] args)
	{
		try
		{
			Client atomClient = Manager.createClient(Protocols.HTTP, "Atom client");
			Service atomService = new Service(atomClient, "http://bitworking.org/projects/pyapp/collection.cgi?introspection=1");
			Feed atomFeed = atomService.getWorkspaces().get(0).getCollections().get(0).getFeed();
			atomFeed.write(System.out);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
