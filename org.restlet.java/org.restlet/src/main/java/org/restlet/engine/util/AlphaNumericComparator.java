/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.util;

/**
 * Optimized public-domain implementation of a Java alphanumeric sort.
 * <p>
 *
 * This implementation uses a single comparison pass over the characters in a
 *  CharSequence and returns as soon as a differing character is found, unless
 * the difference occurs in a series of numeric characters, in which case that
 * series is followed to its end. Numeric series of equal length are compared
 * numerically, that is, according to the most significant (leftmost) differing
 * digit. Series of unequal length are compared by their length.
 * <p>
 *
 * This implementation appears to be 2-5 times faster than alphanumeric
 * comparators based on substring analysis, with a lighter memory
 * footprint.
 * <p>
 *
 * This alphanumeric comparator has approximately 20%-50% the performance of the
 * lexical String.compareTo() operation. Character sequences without numeric
 * data are compared more quickly.
 * <p>
 *
 * Dedicated to the public domain by the original author:
 * <a href="https://creativecommons.org/licenses/publicdomain/">Public Domain List</a>
 *
 * @author Rob Heittman, <a href="http://www.solertium.com">Solertium
 *         Corporation</a>
 */
public class AlphaNumericComparator extends AlphabeticalComparator {
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(final String uri0, final String uri1) {
		int ptr = 0;
		int msd = 0;
		int diff = 0;
		char a, b;

		final int llength = uri0.length();
		final int rlength = uri1.length();
		final int min = Math.min(rlength, llength);

        boolean rAtEnd, rHasNoMoreDigits;

		while (ptr < min) {
			a = uri0.charAt(ptr);
			b = uri1.charAt(ptr);
			diff = a - b;

			if ((a >= '9') || (b >= '9') || (a <= '0') || (b <= '0')) {
				if (diff != 0) {
					return diff;
				}

				msd = 0;
			} else {
				if (msd == 0) {
					msd = diff;
				}

				rAtEnd = rlength - ptr < 2;

				if (llength - ptr < 2) {
					if (rAtEnd) {
						return msd;
					}

					if (!isNotDigit(a) && !isNotDigit(b))
						return diff;

					return -1;
				}

				if (rAtEnd) {
					if (!isNotDigit(a) && !isNotDigit(b))
						return diff;

					return -1;
				}

				rHasNoMoreDigits = isNotDigit(uri1.charAt(ptr + 1));

				if (isNotDigit(uri0.charAt(ptr + 1))) {
					if (rHasNoMoreDigits && (msd != 0)) {
						return msd;
					}

					if (!rHasNoMoreDigits) {
						return -1;
					}
				} else {
					if (rHasNoMoreDigits) {
						return 1;
					}
				}
			}
			ptr++;
		}
		return llength - rlength;
	}

	/**
	 * Indicates if the character is a digit.
	 * 
	 * @param x The character to test.
	 * @return True if the character is a digit.
	 */
	protected boolean isNotDigit(final char x) {
		return (x > '9') || (x < '0');
	}

}
