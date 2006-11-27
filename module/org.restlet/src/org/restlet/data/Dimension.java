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

package org.restlet.data;

/**
 * Dimension on which the representations of a resource may vary.
 * 
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.44">HTTP
 *      Vary header</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Dimension {
	public static final Dimension CHARACTER_SET = new Dimension();

	public static final Dimension CLIENT_ADDRESS = new Dimension();

	public static final Dimension CLIENT_AGENT = new Dimension();

	public static final Dimension UNSPECIFIED = new Dimension();

	public static final Dimension ENCODING = new Dimension();

	public static final Dimension LANGUAGE = new Dimension();

	public static final Dimension MEDIA_TYPE = new Dimension();

	public static final Dimension TIME = new Dimension();

	/**
	 * Constructor.
	 */
	private Dimension() {
	}
}
