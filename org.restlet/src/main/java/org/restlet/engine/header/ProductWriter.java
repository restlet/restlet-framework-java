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

import java.util.Iterator;
import java.util.List;

/**
 * User agent header writer.
 * 
 * @author Thierry Boileau
 */
public class ProductWriter {

	/**
	 * Formats the given List of Products to a String.
	 * 
	 * @param products The list of products to format.
	 * @return the List of Products as String.
	 */
	public static String write(List<Product> products) {
		StringBuilder builder = new StringBuilder();

		for (Iterator<Product> iterator = products.iterator(); iterator.hasNext();) {
			Product product = iterator.next();

			if ((product.getName() == null) || (product.getName().isEmpty())) {
				throw new IllegalArgumentException("Product name cannot be null.");
			}

			builder.append(product.getName());

			if (product.getVersion() != null) {
				builder.append("/").append(product.getVersion());
			}

			if (product.getComment() != null) {
				builder.append(" (").append(product.getComment()).append(")");
			}

			if (iterator.hasNext()) {
				builder.append(" ");
			}
		}

		return builder.toString();
	}

}
