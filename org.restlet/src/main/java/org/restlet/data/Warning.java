/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import java.util.Date;

/**
 * Additional information about the status or transformation of a request or
 * response. This is typically used to warn about a possible issues with caching
 * operations or transformations applied to the entity body.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the "Warning"
 * header.
 * 
 * @author Jerome Louvel
 */
public class Warning {

	/** The agent. Typically a caching agent. */
	private volatile String agent;

	/** The warning date. */
	private volatile Date date;

	/** The special status. */
	private volatile Status status;

	/** The warning text. */
	private volatile String text;

	/**
	 * Constructor.
	 */
	public Warning() {
		this.agent = null;
		this.date = null;
		this.status = null;
		this.text = null;
	}

	/**
	 * Returns the agent. Typically a caching agent.
	 * 
	 * @return The agent. Typically a caching agent.
	 */
	public String getAgent() {
		return agent;
	}

	/**
	 * Returns the warning date.
	 * 
	 * @return The warning date.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Returns the special status.
	 * 
	 * @return The special status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Returns the warning text.
	 * 
	 * @return The warning text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the agent. Typically a caching agent.
	 * 
	 * @param agent The agent. Typically a caching agent.
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}

	/**
	 * Sets the warning date.
	 * 
	 * @param date The warning date.
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Sets the special status.
	 * 
	 * @param status The special status.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Sets the warning text.
	 * 
	 * @param text The warning text.
	 */
	public void setText(String text) {
		this.text = text;
	}

}
