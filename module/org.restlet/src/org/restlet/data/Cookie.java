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
 * Cookie provided by a client.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Cookie extends Parameter
{
	/** The version number. */
	private int version;

	/** The validity path. */
	private String path;

	/** The domain name. */
	private String domain;

	/**
	 * Constructor.
	 */
	public Cookie()
	{
		this(0, null, null, null, null);
	}

	/**
	 * Constructor.
	 * @param version The version number.
	 * @param name The name.
	 * @param value The value.
	 */
	public Cookie(int version, String name, String value)
	{
		this(version, name, value, null, null);
	}

	/**
	 * Constructor.
	 * @param version The version number.
	 * @param name The name.
	 * @param value The value.
	 * @param path The validity path.
	 * @param domain The domain name.
	 */
	public Cookie(int version, String name, String value, String path, String domain)
	{
		super(name, value);
		this.version = version;
		this.path = path;
		this.domain = domain;
	}

	/**
	 * Constructor.
	 * @param name The name.
	 * @param value The value.
	 */
	public Cookie(String name, String value)
	{
		this(0, name, value, null, null);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object object)
	{
		return (object instanceof Cookie) && (((Cookie) object).hashCode() == hashCode());
	}

	/**
	 * Returns the domain name.
	 * @return The domain name.
	 */
	public String getDomain()
	{
		return this.domain;
	}

	/**
	 * Returns the validity path.
	 * @return The validity path.
	 */
	public String getPath()
	{
		return this.path;
	}

	/**
	 * Returns the cookie specification version.
	 * @return The cookie specification version.
	 */
	public int getVersion()
	{
		return this.version;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		return Factory.hashCode(super.hashCode(), getVersion(), getPath(), getDomain());
	}

	/**
	 * Sets the domain name.
	 * @param domain The domain name.
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	/**
	 * Sets the validity path.
	 * @param path The validity path.
	 */
	public void setPath(String path)
	{
		this.path = path;
	}

	/**
	 * Sets the cookie specification version.
	 * @param version The cookie specification version.
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}

}
