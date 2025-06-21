/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Dimension;

import java.util.Collection;

/**
 * Dimension header writer.
 * 
 * @author Thierry Boileau
 */
public class DimensionWriter extends HeaderWriter<Dimension> {

	/**
	 * Creates a vary header from the given dimensions.
	 * 
	 * @param dimensions The dimensions to copy to the response.
	 * @return Returns the Vary header or null, if dimensions is null or empty.
	 */
	public static String write(Collection<Dimension> dimensions) {
		return new DimensionWriter().append(dimensions).toString();
	}

	/**
	 * Appends a collection of dimensions as a header.
	 * 
	 * @param dimensions The dimensions to format.
	 * @return This writer.
	 */
	public DimensionWriter append(Collection<Dimension> dimensions) {
		if ((dimensions != null) && !dimensions.isEmpty()) {
			if (dimensions.contains(Dimension.CLIENT_ADDRESS) || dimensions.contains(Dimension.TIME)
					|| dimensions.contains(Dimension.UNSPECIFIED)) {
				// From an HTTP point of view the representations can
				// vary in unspecified ways
				append("*");
			} else {
				boolean first = true;

				for (Dimension dimension : dimensions) {
					if (first) {
						first = false;
					} else {
						append(", ");
					}

					append(dimension);
				}
			}
		}

		return this;
	}

	@Override
	public HeaderWriter<Dimension> append(Dimension dimension) {
		if (dimension == Dimension.CHARACTER_SET) {
			append(HeaderConstants.HEADER_ACCEPT_CHARSET);
		} else if (dimension == Dimension.CLIENT_AGENT) {
			append(HeaderConstants.HEADER_USER_AGENT);
		} else if (dimension == Dimension.ENCODING) {
			append(HeaderConstants.HEADER_ACCEPT_ENCODING);
		} else if (dimension == Dimension.LANGUAGE) {
			append(HeaderConstants.HEADER_ACCEPT_LANGUAGE);
		} else if (dimension == Dimension.MEDIA_TYPE) {
			append(HeaderConstants.HEADER_ACCEPT);
		} else if (dimension == Dimension.AUTHORIZATION) {
			append(HeaderConstants.HEADER_AUTHORIZATION);
		} else if (dimension == Dimension.ORIGIN) {
			append(HeaderConstants.HEADER_ORIGIN);
		}

		return this;
	}

}
