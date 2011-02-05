/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.util;

import java.util.Date;
import java.util.WeakHashMap;

import org.restlet.engine.Edition;

/**
 * Class acting as an immutable date class based on the {@link Date} class.
 * 
 * Throws {@link UnsupportedOperationException} when mutable methods are
 * invoked.
 * 
 * @author Piyush Purang (ppurang@gmail.com)
 * @see java.util.Date
 * @see <a
 *      href="http://discuss.fogcreek.com/joelonsoftware3/default.asp?cmd=show&ixPost=73959&ixReplies=24"
 *      >Immutable Date</a>
 */
final class ImmutableDate extends Date {
    private static final transient WeakHashMap<Date, ImmutableDate> CACHE = new WeakHashMap<Date, ImmutableDate>();

    private static final long serialVersionUID = -5946186780670229206L;

    /**
     * Returns an ImmutableDate object wrapping the given date.
     * 
     * @param date
     *            object to be made immutable
     * @return an immutable date object
     */
    public static ImmutableDate valueOf(Date date) {
        if (!CACHE.containsKey(date)) {
            CACHE.put(date, new ImmutableDate(date));
        }
        return CACHE.get(date);
    }

    /**
     * Private constructor. A factory method is provided.
     * 
     * @param date
     *            date to be made immutable
     */
    private ImmutableDate(Date date) {
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
        if (Edition.CURRENT == Edition.ANDROID) {
            super.setTime(arg0);
        } else {
            throw new UnsupportedOperationException(
                    "ImmutableDate is immutable");
        }
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