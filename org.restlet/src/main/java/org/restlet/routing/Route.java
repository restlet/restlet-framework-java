/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.routing;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

/**
 * Filter scoring the affinity of calls with the attached Restlet. The score is
 * used by an associated Router in order to determine the most appropriate
 * Restlet for a given call.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see org.restlet.routing.Template
 * @author Jerome Louvel
 */
public abstract class Route extends Filter {

	/** The parent router. */
	private volatile Router router;

	/**
	 * Constructor behaving as a simple extractor filter.
	 * 
	 * @param next The next Restlet.
	 */
	public Route(Restlet next) {
		this(null, next);
	}

	/**
	 * Constructor.
	 * 
	 * @param router The parent router.
	 * @param next   The next Restlet.
	 */
	public Route(Router router, Restlet next) {
		super((router != null) ? router.getContext() : (next != null) ? next.getContext() : null, next);
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
	 * Returns the score for a given call (between 0 and 1.0).
	 * 
	 * @param request  The request to score.
	 * @param response The response to score.
	 * @return The score for a given call (between 0 and 1.0).
	 */
	public abstract float score(Request request, Response response);

	/**
	 * Sets the parent router.
	 * 
	 * @param router The parent router.
	 */
	public void setRouter(Router router) {
		this.router = router;
	}

}
