/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Disposition;
import org.restlet.data.Parameter;

/**
 * Disposition header writer.
 * 
 * @author Thierry Boileau
 */
public class DispositionWriter extends HeaderWriter<Disposition> {

	/**
	 * Formats a disposition.
	 * 
	 * @param disposition The disposition to format.
	 * @return The formatted disposition.
	 */
	public static String write(Disposition disposition) {
		return new DispositionWriter().append(disposition).toString();
	}

	@Override
	public DispositionWriter append(Disposition disposition) {
		if (Disposition.TYPE_NONE.equals(disposition.getType()) || disposition.getType() == null) {
			return this;
		}

		append(disposition.getType());

		for (Parameter parameter : disposition.getParameters()) {
			append("; ");
			append(parameter.getName());
			append("=");

			if (HeaderUtils.isToken(parameter.getValue())) {
				append(parameter.getValue());
			} else {
				appendQuotedString(parameter.getValue());
			}
		}

		return this;
	}

}
