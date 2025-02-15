/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
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
	public static ParameterSeries unmodifiableSeries(final Series<Parameter> series) {
		ParameterSeries result = new ParameterSeries();
		result.addAll(series);
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
