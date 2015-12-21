/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.data;

/**
 * Product tokens are used to allow communicating applications to identify
 * themselves by software name and version.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43">User-Agent</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.8">Product
 *      Tokens</a>
 */
public class Product {

    /** Comment. */
    private volatile String comment;

    /** Product name. */
    private volatile String name;

    /** Version number. */
    private volatile String version;

    /**
     * Constructor.
     * 
     * @param name
     *            The product name.
     * @param version
     *            The product version.
     * @param comment
     *            The product comment.
     */
    public Product(String name, String version, String comment) {
        super();
        this.name = name;
        this.version = version;
        this.comment = comment;
    }

    /**
     * Returns the facultative comment.
     * 
     * @return The facultative comment.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Returns the product name.
     * 
     * @return The product name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the version of the product.
     * 
     * @return The version of the product.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the facultative comment.
     * 
     * @param comment
     *            The facultative comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the product name.
     * 
     * @param name
     *            The product name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the version of the product.
     * 
     * @param version
     *            The version of the product.
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
