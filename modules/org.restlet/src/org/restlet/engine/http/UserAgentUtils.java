/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.Product;

/**
 * Utilities related to the user agent header.
 * 
 * @author Thierry Boileau
 */
public class UserAgentUtils {

    /**
     * Formats the given List of Products to a String.
     * 
     * @param products
     *            The list of products to format.
     * @return the List of Products as String.
     * @throws IllegalArgumentException
     *             Thrown if the List of Products contains illegal values
     */
    public static String format(List<Product> products)
            throws IllegalArgumentException {
        final StringBuilder builder = new StringBuilder();

        for (final Iterator<Product> iterator = products.iterator(); iterator
                .hasNext();) {
            final Product product = iterator.next();
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

    /**
     * Parses the given user agent String to a list of Product instances.
     * 
     * @param userAgent
     * @return the List of Product objects parsed from the String
     * @throws IllegalArgumentException
     *             Thrown if the String can not be parsed as a list of Product
     *             instances.
     */
    public static List<Product> parse(String userAgent)
            throws IllegalArgumentException {
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
                    if (((c >= 'a') && (c <= 'z'))
                            || ((c >= 'A') && (c <= 'Z')) || (c == ' ')) {
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
                                    result.add(new Product(token, version,
                                            comment));
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
                    if (insideToken && (tokenBuilder.length() > 0)) {
                        token = tokenBuilder.toString();
                        result.add(new Product(token, null, null));
                    }
                }
            }
        }

        return result;

    }

}
