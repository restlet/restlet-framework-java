/**
 * Copyright 2005-2020 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.util;

import java.util.Iterator;
import java.util.List;

import org.restlet.client.data.Parameter;
import org.restlet.client.util.Series;

/**
 * Parameter series.
 * 
 * @author Thierry Boileau
 */
public class ParameterSeries extends Series<Parameter> {

	/**
	 * Returns an unmodifiable view of the specified series. Attempts to call a
	 * modification method will throw an UnsupportedOperationException.
	 * 
	 * @param series
	 *            The series for which an unmodifiable view should be returned.
	 * @return The unmodifiable view of the specified series.
	 */
	public static ParameterSeries unmodifiableSeries(
			final Series<Parameter> series) {
		ParameterSeries result = new ParameterSeries();
		for (Iterator<Parameter> iterator = series.iterator(); iterator
				.hasNext();) {
			result.add(iterator.next());
		}

		return result;
	}

	/**
	 * Constructor.
	 */
	public ParameterSeries() {
		super(Parameter.class);
	}

	/**
	 * Constructor.
	 * 
	 * @param delegate
	 *            The delegate list.
	 */
	public ParameterSeries(List<Parameter> delegate) {
		super(Parameter.class, delegate);
	}

	@Override
	public Parameter createEntry(String name, String value) {
		return new Parameter(name, value);
	}
	
	@Override
	public Series<Parameter> createSeries(List<Parameter> delegate) {
		return new ParameterSeries(delegate);
	}

}
