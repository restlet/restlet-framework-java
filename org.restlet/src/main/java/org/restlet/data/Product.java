/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

/**
 * Product tokens are used to allow communicating applications to identify
 * themselves by software name and version.
 * 
 * @author Thierry Boileau
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43">User-Agent</a>
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.8">Product
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
	 * @param name    The product name.
	 * @param version The product version.
	 * @param comment The product comment.
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
	 * @param comment The facultative comment.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Sets the product name.
	 * 
	 * @param name The product name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the version of the product.
	 * 
	 * @param version The version of the product.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
}
