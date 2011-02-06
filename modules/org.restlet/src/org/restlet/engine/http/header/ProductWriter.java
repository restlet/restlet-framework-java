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

package org.restlet.engine.http.header;

import java.util.Iterator;
import java.util.List;

import org.restlet.data.Product;

/**
 * User agent header writer.
 * 
 * @author Thierry Boileau
 */
public class ProductWriter {

    /**
     * Formats the given List of Products to a String.
     * 
     * @param products
     *            The list of products to format.
     * @return the List of Products as String.
     */
    public static String write(List<Product> products) {
        StringBuilder builder = new StringBuilder();

        for (Iterator<Product> iterator = products.iterator(); iterator
                .hasNext();) {
            Product product = iterator.next();

            if ((product.getName() == null)
                    || (product.getName().length() == 0)) {
                throw new IllegalArgumentException(
                        "Product name cannot be null.");
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
