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
 * Method to execute when handling a call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Method extends Metadata
{
	private static final String BASE_HTTP = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html";

	private static final String BASE_WEBDAV = "http://www.webdav.org/specs/rfc2518.html";

	public static final Method CONNECT = new Method("CONNECT",
			"Used with a proxy that can dynamically switch to being a tunnel", BASE_HTTP
					+ "#sec9.9");

	public static final Method COPY = new Method(
			"COPY",
			"Create a duplicate of the source resource, identified by the Request-URI, in the destination resource, identified by the URI in the Destination header",
			BASE_WEBDAV + "#METHOD_COPY");

	public static final Method DELETE = new Method(
			"DELETE",
			"Request that the origin server delete the resource identified by the request URI",
			BASE_HTTP + "#sec9.7");

	public static final Method GET = new Method(
			"GET",
			"Retrieve whatever information (in the form of an entity) is identified by the request URI",
			BASE_HTTP + "#sec9.3");

	public static final Method HEAD = new Method(
			"HEAD",
			"Identical to GET except that the server must not return a message body in the response",
			BASE_HTTP + "#sec9.4");

	public static final Method LOCK = new Method("LOCK",
			"Used to take out a lock of any access type (WebDAV)", BASE_WEBDAV
					+ "#METHOD_LOCK");

	public static final Method MKCOL = new Method("MKCOL",
			"Used to create a new collection (WebDAV)", BASE_WEBDAV + "#METHOD_MKCOL");

	public static final Method MOVE = new Method(
			"MOVE",
			"Logical equivalent of a copy, followed by consistency maintenance processing, followed by a delete of the source (WebDAV)",
			BASE_WEBDAV + "#METHOD_MOVE");

	public static final Method OPTIONS = new Method(
			"OPTIONS",
			"Request for information about the communication options available on the request/response chain identified by the URI",
			BASE_HTTP + "#sec9.2");

	public static final Method POST = new Method(
			"POST",
			"Request that the origin server accept the entity enclosed in the request as a new subordinate of the resource identified by the request URI",
			BASE_HTTP + "#sec9.5");

	public static final Method PROPFIND = new Method("PROPFIND",
			"Retrieve properties defined on the resource identified by the request URI",
			BASE_WEBDAV + "#METHOD_PROPFIND");

	public static final Method PROPPATCH = new Method(
			"PROPPATCH",
			"Process instructions specified in the request body to set and/or remove properties defined on the resource identified by the request URI",
			BASE_WEBDAV + "#METHOD_PROPPATCH");

	public static final Method PUT = new Method("PUT",
			"Request that the enclosed entity be stored under the supplied request URI",
			BASE_HTTP + "#sec9.6");

	public static final Method TRACE = new Method("TRACE",
			"Used to invoke a remote, application-layer loop-back of the request message",
			BASE_HTTP + "#sec9.8");

	public static final Method UNLOCK = new Method(
			"UNLOCK",
			"Remove the lock identified by the lock token from the request URI, and all other resources included in the lock",
			BASE_WEBDAV + "#METHOD_UNLOCK");

	/** The URI of the specification describing the method. */
	private String uri;

	/**
	 * Constructor.
	 * @param name The technical name of the method.
	 * @see org.restlet.data.Method#valueOf(String)
	 */
	public Method(String name)
	{
		this(name, null, null);
	}

	/**
	 * Constructor.
	 * @param name The technical name of the method.
	 * @param description The description.
	 * @see org.restlet.data.Method#valueOf(String)
	 */
	public Method(String name, String description)
	{
		this(name, description, null);
	}

	/**
	 * Constructor.
	 * @param name The technical name.
	 * @param description The description.
	 * @param uri The URI of the specification describing the method.
	 * @see org.restlet.data.Method#valueOf(String)
	 */
	public Method(String name, String description, String uri)
	{
		super(name, description);
		this.uri = uri;
	}

	/**
	 * Returns the URI of the specification describing the method.
	 * @return The URI of the specification describing the method.
	 */
	public String getUri()
	{
		return this.uri;
	}

	/**
	 * Indicates if two metadata are equal.
	 * @param object The object to compare to.
	 * @return True if both metadata are equal.
	 */
	@Override
	public boolean equals(Object object)
	{
		return (object instanceof Method) && ((Method) object).getName().equals(getName());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}

	/**
	 * Returns the method associated to a given method name. If an existing constant exists then it is returned,
	 * otherwise a new instance is created.
	 * @param methodName The method name.
	 * @return The associated method.
	 */
	public static Method valueOf(String methodName)
	{
		Method result = null;

		if (methodName != null)
		{
			if (methodName.equalsIgnoreCase(GET.getName()))
				result = GET;
			else if (methodName.equalsIgnoreCase(POST.getName()))
				result = POST;
			else if (methodName.equalsIgnoreCase(HEAD.getName()))
				result = HEAD;
			else if (methodName.equalsIgnoreCase(OPTIONS.getName()))
				result = OPTIONS;
			else if (methodName.equalsIgnoreCase(PUT.getName()))
				result = PUT;
			else if (methodName.equalsIgnoreCase(DELETE.getName()))
				result = DELETE;
			else if (methodName.equalsIgnoreCase(CONNECT.getName()))
				result = CONNECT;
			else if (methodName.equalsIgnoreCase(COPY.getName()))
				result = COPY;
			else if (methodName.equalsIgnoreCase(LOCK.getName()))
				result = LOCK;
			else if (methodName.equalsIgnoreCase(MKCOL.getName()))
				result = MKCOL;
			else if (methodName.equalsIgnoreCase(MOVE.getName()))
				result = MOVE;
			else if (methodName.equalsIgnoreCase(PROPFIND.getName()))
				result = PROPFIND;
			else if (methodName.equalsIgnoreCase(PROPPATCH.getName()))
				result = PROPPATCH;
			else if (methodName.equalsIgnoreCase(TRACE.getName()))
				result = TRACE;
			else if (methodName.equalsIgnoreCase(UNLOCK.getName()))
				result = UNLOCK;
			else
				result = new Method(methodName);
		}

		return result;
	}
}
