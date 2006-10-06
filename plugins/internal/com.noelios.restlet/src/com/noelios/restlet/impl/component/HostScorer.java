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

package com.noelios.restlet.impl.component;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.component.VirtualHost;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;

/**
 * Router scorer based on a target VirtualHost. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HostScorer extends Scorer
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(HostScorer.class.getCanonicalName());

	/**
	 * Constructor.
	 * @param router The parent router.
	 * @param target The target virtual host.
	 */
	public HostScorer(Router router, VirtualHost target)
	{
		super(router, target);
	}

	/**
	 * Returns the target virtual host.
	 * @return The target virtual host.
	 */
	public VirtualHost getHost()
	{
		return (VirtualHost) getNext();
	}

	/**
	 * Sets the next virtual host.
	 * @param next The next virtual host.
	 */
	public void setNext(VirtualHost next)
	{
		super.setNext(next);
	}

	/**
	 * Returns the score for a given call (between 0 and 1.0).
    * @param request The request to score.
    * @param response The response to score.
	 * @return The score for a given call (between 0 and 1.0).
	 */
	public float score(Request request, Response response)
	{
		float result = 0F;
		boolean incompatible = false;

		// Add the protocol score
		Protocol protocol = request.getProtocol();
		if (protocol == null)
		{
			// Attempt to guess the protocol to use
			// from the target reference scheme
			protocol = request.getResourceRef().getSchemeProtocol();
		}

		if (protocol == null)
		{
			logger.warning("Unable to determine the protocol to use for this call.");
			incompatible = true;
		}
		else
		{
			if (getHost().getAllowedProtocols().contains(protocol)
					|| getHost().getAllowedProtocols().contains(Protocol.ALL))
			{
				result += 0.25F;
			}
			else
			{
				incompatible = true;
			}
		}

		// Add the port score
		if (!incompatible)
		{
			Integer port = response.getServer().getPort();

			if (getHost().getAllowedPorts().contains(VirtualHost.ALL_PORTS)
					|| ((port != null) && getHost().getAllowedPorts().contains(port)))
			{
				result += 0.25F;
			}
			else
			{
				incompatible = true;
			}
		}

		// Add the address score
		if (!incompatible)
		{
			String address = response.getServer().getAddress();

			if (getHost().getAllowedAddresses().contains(VirtualHost.ALL_ADDRESSES)
					|| ((address != null) && getHost().getAllowedAddresses().contains(address)))
			{
				result += 0.25F;
			}
			else
			{
				incompatible = true;
			}
		}

		// Add the name score
		if (!incompatible)
		{
			String name = response.getServer().getName();

			if (getHost().getAllowedNames().contains(VirtualHost.ALL_NAMES)
					|| ((name != null) && getHost().getAllowedNames().contains(name)))
			{
				result += 0.25F;
			}
			else
			{
				incompatible = true;
			}
		}

		if (incompatible)
		{
			result = 0F;
		}

		if (logger.isLoggable(Level.FINER))
		{
			logger.finer("Call score for the \"" + getHost().getName() + "\" host: "
					+ result);
		}

		return result;
	}

	/**
	 * Handles a call.
    * @param request The request to handle.
    * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		request.setBaseRef(new Reference(request.getProtocol().getSchemeName(), response.getServer()
				.getName(), response.getServer().getPort(), null, null, null));

		if (logger.isLoggable(Level.FINE))
		{
			logger.fine("New base URI: " + request.getBaseRef());
			logger.fine("New relative part: " + request.getRelativePart());
		}

		if (logger.isLoggable(Level.FINE))
		{
			logger.fine("Delegating the call to the target Restlet");
		}

		// Invoke the call restlet
		super.handle(request, response);
	}
}
