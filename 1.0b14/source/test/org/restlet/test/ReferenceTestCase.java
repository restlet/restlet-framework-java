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

import org.restlet.data.Reference;

/**
 * Test {@link org.restlet.data.Reference}.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public class ReferenceTestCase extends RestletTestCase
{

	protected static String DEFAULT_SCHEME = "http";

	protected static String DEFAULT_SCHEMEPART = "//";

	public static void main(String[] args)
	{
		junit.awtui.TestRunner.run(ReferenceTestCase.class);
	}

	/**
	 * Returns a reference with uri == http://
	 *
	 * @return Reference instance.
	 */
	protected Reference getReference()
	{
		Reference ref = new Reference();
		ref.setScheme(DEFAULT_SCHEME);
		ref.setSchemeSpecificPart(DEFAULT_SCHEMEPART);
		return ref;
	}

	/**
	 * Returns a reference that is initialized with http://www.restlet.org.
	 *
	 * @return Reference instance.
	 */
	protected Reference getDefaultReference()
	{
		Reference ref = getReference();
		ref.setHostName("www.restlet.org");
		return ref;
	}

	/**
	 * Equality tests. 
	 */
	public void testEquals() throws Exception
	{
		Reference ref1 = getDefaultReference();
		Reference ref2 = getDefaultReference();
		assertTrue(ref1.equals(ref2));
		assertEquals(ref1, ref2);
	}

	/**
	 * Test references that are unequal. 
	 */
	public void testUnEquals() throws Exception
	{
		String uri1 = "http://www.restlet.org/";
		String uri2 = "http://www.restlet.net/";
		Reference ref1 = new Reference(uri1);
		Reference ref2 = new Reference(uri2);
		assertFalse(ref1.equals(ref2));
		assertFalse(ref1.equals(null));
	}

	/**
	 * Test hostname getting/setting.
	 */
	public void testHostName() throws Exception
	{
		Reference ref = getReference();
		String host = "www.restlet.org";
		ref.setHostName(host);
		assertEquals(host, ref.getHostName());
		host = "restlet.org";
		ref.setHostName(host);
		assertEquals(host, ref.getHostName());
	}

	/**
	 * Test port getting/setting. 
	 */
	public void testPort() throws Exception
	{
		Reference ref = getDefaultReference();
		int port = 8080;
		ref.setHostPort(port);
		assertEquals(port, ref.getHostPort().intValue());
		port = 9090;
		ref.setHostPort(port);
		assertEquals(port, ref.getHostPort().intValue());
	}

	/**
	 * Test scheme getting/setting. 
	 */
	public void testScheme() throws Exception
	{
		Reference ref = getDefaultReference();
		assertEquals(DEFAULT_SCHEME, ref.getScheme());
		String scheme = "https";
		ref.setScheme(scheme);
		assertEquals(scheme, ref.getScheme());
		ref.setScheme(DEFAULT_SCHEME);
		assertEquals(DEFAULT_SCHEME, ref.getScheme());
	}

	/**
	 * Test scheme specific part getting/setting. 
	 */
	public void testSchemeSpecificPart() throws Exception
	{
		Reference ref = getDefaultReference();
		String part = "//www.restlet.org";
		assertEquals(part, ref.getSchemeSpecificPart());
		part = "//www.restlet.net";
		ref.setSchemeSpecificPart(part);
		assertEquals(part, ref.getSchemeSpecificPart());
	}
}
