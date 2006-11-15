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

package org.restlet.util;

import java.util.Date;
import java.util.WeakHashMap;

/**
 * Class acting as an immutable date class based on the {@link java.util.Date} class.
 * 
 * Throws {@link UnsupportedOperationException} when muttable methopds are invoked.
 * @author Piyush Purang
 * @see java.util.Date
 * @see <a href="http://discuss.fogcreek.com/joelonsoftware3/default.asp?cmd=show&ixPost=73959&ixReplies=24">Immutable Date</a>
 */
public final class ImmutableDate extends Date
{
	// TODO Are we serializable?
	private static final long serialVersionUID = -5946186780670229206L;

	/** Delegate being wrapped */
	private final Date delegate;

	private static final transient WeakHashMap<Date, ImmutableDate> CACHE = new WeakHashMap<Date, ImmutableDate>();

	/**
	 * Private constructor. A factory method is provided.
	 * @param date date to be made immutable
	 */
	private ImmutableDate(Date date)
	{
		this.delegate = (Date) date.clone();
	}

	/**
	 * Returns an ImmutableDate object wrapping the given date.
	 * @param date object to be made immutable
	 * @return an immutable date object
	 */
	public static ImmutableDate valueOf(Date date)
	{
		if (!CACHE.containsKey(date))
		{
			CACHE.put(date, new ImmutableDate(date));
		}
		return CACHE.get(date);
	}

	/**{@inheritDoc}*/
	@Override
	public boolean after(Date when)
	{
		return delegate.after(when);
	}

	/**{@inheritDoc}*/
	@Override
	public boolean before(Date when)
	{
		return delegate.before(when);
	}

	/**{@inheritDoc}*/
	@Override
	public Object clone()
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	public int compareTo(Date anotherDate)
	{
		return delegate.compareTo(anotherDate);
	}

	/**{@inheritDoc}*/
	@Override
	public boolean equals(Object obj)
	{
		return delegate.equals(obj);
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getDate()
	{
		return delegate.getDate();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getDay()
	{
		return delegate.getDay();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getHours()
	{
		return delegate.getHours();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getMinutes()
	{
		return delegate.getMinutes();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getMonth()
	{
		return delegate.getMonth();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getSeconds()
	{

		return delegate.getSeconds();
	}

	/**{@inheritDoc}*/
	@Override
	public long getTime()
	{
		return delegate.getTime();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getTimezoneOffset()
	{
		return delegate.getTimezoneOffset();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public int getYear()
	{
		return delegate.getYear();
	}

	/**{@inheritDoc}*/
	@Override
	public int hashCode()
	{
		return delegate.hashCode();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public void setDate(int date)
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public void setHours(int hours)
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public void setMinutes(int minutes)
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public void setMonth(int month)
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public void setSeconds(int seconds)
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public void setTime(long time)
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public void setYear(int year)
	{
		throw new UnsupportedOperationException("ImmutableDate is immutable");
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public String toGMTString()
	{
		return delegate.toGMTString();
	}

	/**{@inheritDoc}*/
	@Override
	@Deprecated
	public String toLocaleString()
	{
		return delegate.toLocaleString();
	}

	/**{@inheritDoc}*/
	@Override
	public String toString()
	{
		return delegate.toString();
	}
}
