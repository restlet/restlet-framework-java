/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.header;

import org.restlet.data.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * User agent header reader.
 * 
 * @author Thierry Boileau
 */
public class ProductReader {

	/**
	 * Parses the given user agent String to a list of Product instances.
	 * 
	 * @param userAgent
	 * @return the List of Product objects parsed from the String
	 * @throws IllegalArgumentException Thrown if the String can not be parsed as a
	 *                                  list of Product instances.
	 */
	public static List<Product> read(String userAgent) throws IllegalArgumentException {
		final List<Product> result = new ArrayList<Product>();

		if (userAgent != null) {
			String token = null;
			String version = null;
			String comment = null;
			final char[] tab = userAgent.trim().toCharArray();
			StringBuilder tokenBuilder = new StringBuilder();
			StringBuilder versionBuilder = null;
			StringBuilder commentBuilder = null;
			int index = 0;
			boolean insideToken = true;
			boolean insideVersion = false;
			boolean insideComment = false;

			for (index = 0; index < tab.length; index++) {
				final char c = tab[index];
				if (insideToken) {
					if (HeaderUtils.isTokenChar(c) || (c == ' ')) {
						tokenBuilder.append(c);
					} else {
						token = tokenBuilder.toString().trim();
						insideToken = false;
						if (c == '/') {
							insideVersion = true;
							versionBuilder = new StringBuilder();
						} else if (c == '(') {
							insideComment = true;
							commentBuilder = new StringBuilder();
						}
					}
				} else {
					if (insideVersion) {
						if (c != ' ') {
							versionBuilder.append(c);
						} else {
							insideVersion = false;
							version = versionBuilder.toString();
						}
					} else {
						if (c == '(') {
							insideComment = true;
							commentBuilder = new StringBuilder();
						} else {
							if (insideComment) {
								if (c == ')') {
									insideComment = false;
									comment = commentBuilder.toString();
									result.add(new Product(token, version, comment));
									insideToken = true;
									tokenBuilder = new StringBuilder();
								} else {
									commentBuilder.append(c);
								}
							} else {
								result.add(new Product(token, version, null));
								insideToken = true;
								tokenBuilder = new StringBuilder();
								tokenBuilder.append(c);
							}
						}
					}
				}
			}

			if (insideComment) {
				comment = commentBuilder.toString();
				result.add(new Product(token, version, comment));
			} else {
				if (insideVersion) {
					version = versionBuilder.toString();
					result.add(new Product(token, version, null));
				} else {
					if (insideToken && (!tokenBuilder.isEmpty())) {
						token = tokenBuilder.toString();
						result.add(new Product(token, null, null));
					}
				}
			}
		}

		return result;

	}

	/**
	 * Private constructor to ensure that the class acts as a true utility class
	 * i.e. it isn't instantiable and extensible.
	 */
	private ProductReader() {
	}

}
