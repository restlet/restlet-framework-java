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

import org.restlet.util.Factory;

/**
 * Authentication challenge sent by an origin server to a client.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ChallengeRequest
{
	/** The challenge scheme. */
	private ChallengeScheme scheme;

	/** The authentication realm. */
	private String realm;

	/** The scheme parameters. */
	private ParameterList parameters;

	/**
	 * Constructor.
	 * @param scheme The challenge scheme.
	 * @param realm The authentication realm.
	 */
	public ChallengeRequest(ChallengeScheme scheme, String realm)
	{
		this.scheme = scheme;
		this.realm = realm;
		this.parameters = null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object object)
	{
		return (object instanceof ChallengeRequest)
				&& (((ChallengeRequest) object).hashCode() == hashCode());
	}

	/**
	 * Returns the scheme parameters.
	 * @return The scheme parameters.
	 */
	public ParameterList getParameters()
	{
		if (this.parameters == null) this.parameters = new ParameterList();
		return this.parameters;
	}

	/**
	 * Returns the realm name.
	 * @return The realm name.
	 */
	public String getRealm()
	{
		return this.realm;
	}

	/**
	 * Returns the scheme used.
	 * @return The scheme used.
	 */
	public ChallengeScheme getScheme()
	{
		return this.scheme;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		return Factory.hashCode(getScheme(), getRealm(), getParameters());
	}

	/**
	 * Sets the realm name.
	 * @param realm The realm name.
	 */
	public void setRealm(String realm)
	{
		this.realm = realm;
	}

	/**
	 * Sets the scheme used.
	 * @param scheme The scheme used.
	 */
	public void setScheme(ChallengeScheme scheme)
	{
		this.scheme = scheme;
	}

}
