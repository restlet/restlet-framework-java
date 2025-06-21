/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

import java.util.Date;

/**
 * Class acting as an immutable date class based on the {@link Date} class.
 * 
 * Throws {@link UnsupportedOperationException} when mutable methods are
 * invoked.
 * 
 * @author Piyush Purang (ppurang@gmail.com)
 * @see java.util.Date
 */
public final class ImmutableDate extends Date {

	private static final long serialVersionUID = -5946186780670229206L;

	/**
	 * Private constructor. A factory method is provided.
	 * 
	 * @param date date to be made immutable
	 */
	public ImmutableDate(Date date) {
		super(date.getTime());
	}

	/** {@inheritDoc} */
	@Override
	public Object clone() {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**
	 * As an ImmutableDate is immutable, this method throws an
	 * UnsupportedOperationException exception.
	 */
	@Override
	public void setDate(int arg0) {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**
	 * As an ImmutableDate is immutable, this method throws an
	 * UnsupportedOperationException exception.
	 */
	@Override
	public void setHours(int arg0) {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**
	 * As an ImmutableDate is immutable, this method throws an
	 * UnsupportedOperationException exception.
	 */
	@Override
	public void setMinutes(int arg0) {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**
	 * As an ImmutableDate is immutable, this method throws an
	 * UnsupportedOperationException exception.
	 */
	@Override
	public void setMonth(int arg0) {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**
	 * As an ImmutableDate is immutable, this method throws an
	 * UnsupportedOperationException exception.
	 */
	@Override
	public void setSeconds(int arg0) {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**
	 * As an ImmutableDate is immutable, this method throws an
	 * UnsupportedOperationException exception.
	 */
	@Override
	public void setTime(long arg0) {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**
	 * As an ImmutableDate is immutable, this method throws an
	 * UnsupportedOperationException exception.
	 */
	@Override
	public void setYear(int arg0) {
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

}
