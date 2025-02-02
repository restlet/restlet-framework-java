/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Range;
import org.restlet.representation.Representation;

import java.util.ArrayList;
import java.util.List;

/**
 * Range header reader.
 * 
 * @author Jerome Louvel
 */
public class RangeReader {

	private static final String BYTES_RANGE_PREFIX = "bytes ";

	/**
	 * Parse the Content-Range header value and update the given representation.
	 * 
	 * @param value          Content-range header.
	 * @param representation Representation to update.
	 */
	public static void update(String value, Representation representation) {
		if (value != null && value.startsWith(BYTES_RANGE_PREFIX)) {
			value = value.substring(BYTES_RANGE_PREFIX.length());

			int index = value.indexOf("-");
			int index1 = value.indexOf("/");

			if (index != -1) {
				long startIndex = (index == 0) ? Range.INDEX_LAST : Long.parseLong(value.substring(0, index));
				long endIndex = Long.parseLong(value.substring(index + 1, index1));

				representation.setRange(new Range(startIndex, endIndex - startIndex + 1));
			}

			String strLength = value.substring(index1 + 1, value.length());
			if (!("*".equals(strLength))) {
				representation.setSize(Long.parseLong(strLength));
			}
		}
	}

	/**
	 * Parse the Range header and returns the list of corresponding Range objects.
	 * 
	 * @param rangeHeader The Range header value.
	 * @return The list of corresponding Range objects.
	 */
	public static List<Range> read(String rangeHeader) {
		List<Range> result = new ArrayList<Range>();
		String prefix = "bytes=";
		if (rangeHeader != null && rangeHeader.startsWith(prefix)) {
			rangeHeader = rangeHeader.substring(prefix.length());

			String[] array = rangeHeader.split(",");
			for (int i = 0; i < array.length; i++) {
				String value = array[i].trim();
				long index = 0;
				long length = 0;
				if (value.startsWith("-")) {
					index = Range.INDEX_LAST;
					length = Long.parseLong(value.substring(1));
				} else if (value.endsWith("-")) {
					index = Long.parseLong(value.substring(0, value.length() - 1));
					length = Range.SIZE_MAX;
				} else {
					String[] tab = value.split("-");
					if (tab.length == 2) {
						index = Long.parseLong(tab[0]);
						length = Long.parseLong(tab[1]) - index + 1;
					}
				}
				result.add(new Range(index, length));
			}
		}

		return result;
	}

	/**
	 * Private constructor to ensure that the class acts as a true utility class
	 * i.e., it isn't instantiable and extensible.
	 */
	private RangeReader() {
	}
}
