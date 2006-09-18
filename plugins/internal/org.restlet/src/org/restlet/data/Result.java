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
 * Contains the results information returned by some methods in Resource. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Result
{
	/** The optional output representation. */
	private Representation output;
	
	/** The status. */
	private Status status;

	/** The optional redirection reference. */
	private Reference redirectionRef;

	/**
	 * Constructor. 
	 * @param status The status.
	 */
	public Result(Status status)
	{
		this(status, null, null);
	}

	/**
	 * Constructor. 
	 * @param status The status.
	 * @param output The output representation.
	 */
	public Result(Status status, Representation output)
	{
		this(status, output, null);
	}

	/**
	 * Constructor. 
	 * @param status The status.
	 * @param output The output representation.
	 * @param redirectionRef The redirection reference.
	 */
	public Result(Status status, Representation output, Reference redirectionRef)
	{
		this.output = output;
		this.status = status;
		this.redirectionRef = redirectionRef;
	}
	
	/**
	 * Returns the status.
	 * @return the status.
	 */
	public Status getStatus()
	{
		return this.status;
	}
	
	/**
	 * Sets the status.
	 * @param status The status.
	 */
	public void setStatus(Status status)
	{
		this.status = status;
	}

	/**
	 * Returns the output representation.
	 * @return the output representation or null.
	 */
	public Representation getOutput()
	{
		return this.output;
	}

	/**
	 * Sets the output representation.
	 * @param output The output representation.
	 */
	public void setOutput(Representation output)
	{
		this.output = output;
	}

	/**
	 * Sets the redirection reference.
	 * @param redirectionRef The redirection reference.
	 */
	public void setRedirectionRef(Reference redirectionRef)
	{
		this.redirectionRef = redirectionRef;
	}

	/**
	 * Returns the redirection reference.
	 * @return the redirection reference or null.
	 */
	public Reference getRedirectionRef()
	{
		return this.redirectionRef;
	}
	
}
