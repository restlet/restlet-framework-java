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

package com.noelios.restlet.container;

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.VirtualHost;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Router scorer based on a target VirtualHost.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HostScorer extends Scorer {
	/**
	 * Constructor.
	 * 
	 * @param router
	 *            The parent router.
	 * @param target
	 *            The target virtual host.
	 */
	public HostScorer(Router router, VirtualHost target) {
		super(router, target);
	}

	/**
	 * Returns the target virtual host.
	 * 
	 * @return The target virtual host.
	 */
	public VirtualHost getHost() {
		return (VirtualHost) getNext();
	}

	/**
	 * Sets the next virtual host.
	 * 
	 * @param next
	 *            The next virtual host.
	 */
	public void setNext(VirtualHost next) {
		super.setNext(next);
	}

	/**
	 * Returns the score for a given call (between 0 and 1.0).
	 * 
	 * @param request
	 *            The request to score.
	 * @param response
	 *            The response to score.
	 * @return The score for a given call (between 0 and 1.0).
	 */
	public float score(Request request, Response response) {
		float result = 0F;

		String baseDomain = "";
		String basePort = "";
		String baseScheme = "";

		if (request.getBaseRef() != null) {
			baseDomain = request.getBaseRef().getHostDomain();
			basePort = request.getBaseRef().getHostPort().toString();
			baseScheme = request.getBaseRef().getScheme();
		}

		String resourceDomain = request.getResourceRef().getHostDomain();
		String resourcePort = request.getResourceRef().getHostPort().toString();
		String resourceScheme = request.getResourceRef().getScheme();

		String serverAddress = response.getServerInfo().getAddress();
		String serverPort = response.getServerInfo().getPort().toString();

		if (Pattern.matches(getHost().getBaseDomain(), baseDomain)
				&& Pattern.matches(getHost().getBasePort(), basePort)
				&& Pattern.matches(getHost().getBaseScheme(), baseScheme)
				&& Pattern.matches(getHost().getResourceDomain(),
						resourceDomain)
				&& Pattern.matches(getHost().getResourcePort(), resourcePort)
				&& Pattern.matches(getHost().getResourceScheme(),
						resourceScheme)
				&& Pattern.matches(getHost().getServerAddress(), serverAddress)
				&& Pattern.matches(getHost().getServerPort(), serverPort)) {
			result = 1F;
		}

		if (getLogger().isLoggable(Level.FINER)) {
			getLogger().finer(
					"Call score for the \"" + getHost().getName() + "\" host: "
							+ result);
		}

		return result;
	}

	/**
	 * Allows filtering before processing by the next Restlet. Set the base
	 * reference.
	 * 
	 * @param request
	 *            The request to handle.
	 * @param response
	 *            The response to update.
	 */
	protected void beforeHandle(Request request, Response response) {
		if (getLogger().isLoggable(Level.FINE)) {
			getLogger().fine("New base URI: " + request.getBaseRef());
			getLogger().fine("New relative part: " + request.getRelativePart());
		}
	}
}
