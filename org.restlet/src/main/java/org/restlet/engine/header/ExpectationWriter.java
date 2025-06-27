/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Expectation;
import org.restlet.data.Parameter;

import java.util.List;

/**
 * Expectation header writer.
 * 
 * @author Jerome Louvel
 */
public class ExpectationWriter extends HeaderWriter<Expectation> {

	/**
	 * Writes a list of expectations with a comma separator.
	 * 
	 * @param expectations The list of expectations.
	 * @return The formatted list of expectations.
	 */
	public static String write(List<Expectation> expectations) {
		return new ExpectationWriter().append(expectations).toString();
	}

	@Override
	public ExpectationWriter append(Expectation expectation) {
		if ((expectation.getName() != null) && (!expectation.getName().isEmpty())) {
			appendExtension(expectation);

			if (!expectation.getParameters().isEmpty()) {
				for (Parameter param : expectation.getParameters()) {
					appendParameterSeparator();
					appendExtension(param);
				}
			}
		}

		return this;
	}

}
