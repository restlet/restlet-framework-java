/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test.jaxrs.services;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

/**
 * 
 * @author Stephan Koops
 * 
 */
@Path("/train")
public class SimpleTrain {

	public SimpleTrain() {
		throw new RuntimeException(
				"This Constructor is not allowed, because another constructors has more elements");
	}

	public SimpleTrain(Integer x) {
		"".equals(x);
		throw new RuntimeException(
				"This Constructor is not allowed, because the paramters are not correct annotated");
	}

	public SimpleTrain(String x, @HeaderParam("p")
	String p) {
		"".equals(p);
		"".equals(x);
		throw new RuntimeException(
				"This Constructor is not allowed, because one of the paramters are not correct annotated");
	}

	public SimpleTrain(@HeaderParam("p")
	String p) {
		"".equals(p);
		// this is a valid constructor
	}

	/** Text der ausgegebenen Plain-Text-Repräsentation. */
	public static final String RERP_PLAIN_TEXT = "This is a simple text train";

	/** Text der ausgegebenen HTML-Text-Repräsentation. */
	public static final String RERP_HTML_TEXT = "<html><body>This is a simple html train</body></html>";

	/**
	 * 
	 * @return
	 */
	@GET
	@ProduceMime("text/plain")
	public String getPlainText() {
		return RERP_PLAIN_TEXT;
	}

	/**
	 * 
	 * @return
	 */
	@GET
	@ProduceMime("text/html")
	public String getHtmlText() {
		return RERP_HTML_TEXT;
	}
}