/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.Parameter;

/**
 * Authentication challenge header writer.
 * 
 * @author Jerome Louvel
 */
public class ChallengeWriter extends HeaderWriter<ChallengeRequest> {

	/** Indicates if the first challenge parameter is written. */
	private volatile boolean firstChallengeParameter;

	/**
	 * Constructor.
	 */
	public ChallengeWriter() {
		this.firstChallengeParameter = true;
	}

	@Override
	public HeaderWriter<ChallengeRequest> append(ChallengeRequest value) {
		return this;
	}

	/**
	 * Appends a new challenge parameter, prefixed with a comma. The value is
	 * separated from the name by an '=' character.
	 * 
	 * @param parameter The parameter.
	 * @return This writer.
	 */
	public ChallengeWriter appendChallengeParameter(Parameter parameter) {
		return appendChallengeParameter(parameter.getName(), parameter.getValue());
	}

	/**
	 * Appends a new parameter, prefixed with a comma.
	 * 
	 * @param name The parameter name.
	 * @return The current builder.
	 */
	public ChallengeWriter appendChallengeParameter(String name) {
		appendChallengeParameterSeparator();
		appendToken(name);
		return this;
	}

	/**
	 * Appends a new parameter, prefixed with a comma. The value is separated from
	 * the name by an '=' character.
	 * 
	 * @param name  The parameter name.
	 * @param value The parameter value.
	 * @return This writer.
	 */
	public ChallengeWriter appendChallengeParameter(String name, String value) {
		appendChallengeParameterSeparator();

		if (name != null) {
			appendToken(name);
		}

		if (value != null) {
			append('=');
			appendToken(value);
		}

		return this;
	}

	/**
	 * Appends a comma as a separator if the first parameter has already been
	 * written.
	 * 
	 * @return This writer.
	 */
	public ChallengeWriter appendChallengeParameterSeparator() {
		if (isFirstChallengeParameter()) {
			setFirstChallengeParameter(false);
		} else {
			append(", ");
		}

		return this;
	}

	/**
	 * Appends a new parameter, prefixed with a comma. The value is separated from
	 * the name by an '=' character.
	 * 
	 * @param parameter The parameter.
	 * @return This writer.
	 */
	public ChallengeWriter appendQuotedChallengeParameter(Parameter parameter) {
		return appendQuotedChallengeParameter(parameter.getName(), parameter.getValue());
	}

	/**
	 * Appends a new parameter, prefixed with a comma. The value is quoted and
	 * separated from the name by an '=' character.
	 * 
	 * @param name  The parameter name.
	 * @param value The parameter value to quote.
	 * @return This writer.
	 */
	public ChallengeWriter appendQuotedChallengeParameter(String name, String value) {
		appendChallengeParameterSeparator();

		if (name != null) {
			appendToken(name);
		}

		if (value != null) {
			append('=');
			appendQuotedString(value);
		}

		return this;
	}

	/**
	 * Indicates if the first comma-separated value is written.
	 * 
	 * @return True if the first comma-separated value is written.
	 */
	public boolean isFirstChallengeParameter() {
		return firstChallengeParameter;
	}

	/**
	 * Indicates if the first comma-separated value is written.
	 * 
	 * @param firstValue True if the first comma-separated value is written.
	 */
	public void setFirstChallengeParameter(boolean firstValue) {
		this.firstChallengeParameter = firstValue;
	}
}
