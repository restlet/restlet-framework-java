/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.data;

import org.restlet.data.Method;

import junit.framework.TestCase;

/**
 * Test {@link org.restlet.data.Method}.
 * <p>
 * Note: this test purposefully does *not* extends RestletTestCase. The
 * regression previously present in Restlet
 * (desribed in https://github.com/restlet/restlet-framework-java/issues/1130) depends on
 * class initialization order and vanishes when the Restlet/Engine class is
 * initialized before the class Method.
 * 
 * @author Andreas Wundsam
 */
public class MethodTestCase extends TestCase {

	/**
	 * validate that Method caching works, i.e., the value returned by
	 * Method.valueOf("GET") is the cached constant Method.GET
	 */
	public void testCaching() {
		assertTrue("Method.valueOf('GET') should return cached constant Method.GET ",
				Method.GET == Method.valueOf("GET"));
	}

}
