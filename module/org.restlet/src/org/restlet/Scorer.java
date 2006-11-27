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

package org.restlet;

import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Filter scoring the affinity of calls with the attached Restlet. The score is
 * used by an associated Router in order to determine the most appropriate
 * Restlet for a given call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Scorer extends Filter {
	/** The parent router. */
	private Router router;

	/**
	 * Constructor.
	 * 
	 * @param router
	 *            The parent router.
	 * @param next
	 *            The next Restlet.
	 */
	public Scorer(Router router, Restlet next) {
		super(router == null ? null : router.getContext(), next);
		this.router = router;
	}

	/**
	 * Returns the parent router.
	 * 
	 * @return The parent router.
	 */
	public Router getRouter() {
		return this.router;
	}

	/**
	 * Returns the score for a given call (between 0.0 and 1.0). The default
	 * value is 1.0 but this method is intended to be overriden.
	 * 
	 * @param request
	 *            The request to score.
	 * @param response
	 *            The response to score.
	 * @return The score for a given call (between 0.0 and 1.0).
	 */
	public float score(Request request, Response response) {
		return 1.0F;
	}
}
