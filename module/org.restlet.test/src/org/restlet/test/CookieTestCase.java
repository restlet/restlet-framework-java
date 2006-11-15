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

import org.restlet.data.Cookie;

/**
 * Test {@link org.restlet.data.Cookie}.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class CookieTestCase extends RestletTestCase
{

	/**
	 * Equality tests. 
	 */
	public void testEquals() throws Exception
	{
		Cookie c1 = new Cookie(1, "name1", "value1", "path1", "domain1");
		Cookie c2 = new Cookie(1, "name1", "value1", "path1", "domain1");
		assertTrue(c1.equals(c2));
		assertEquals(c1, c2);
	}

	/**
	 * Test references that are unequal. 
	 */
	public void testUnEquals() throws Exception
	{
		Cookie c1 = new Cookie(1, "name1", "value1", "path1", "domain1");
		Cookie c2 = new Cookie(2, "name2", "value2", "path2", "domain2");
		assertFalse(c1.equals(c2));
	}

}
