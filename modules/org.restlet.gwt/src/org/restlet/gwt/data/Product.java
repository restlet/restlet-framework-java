package org.restlet.gwt.data;

/**
 * Product tokens are used to allow communicating applications to identify
 * themselves by software name and version.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43">User-Agent</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.8">Product
 *      Tokens</a>
 */
public class Product {

    /** Comment. */
    private String comment;

    /** Product name. */
    private String name;

    /** Version number. */
    private String version;

    /**
     * Constructor.
     * 
     * @param name
     *                The product name.
     * @param version
     *                The product version.
     * @param comment
     *                The product comment.
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
        return comment;
    }

    /**
     * Returns the product name.
     * 
     * @return The product name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the product.
     * 
     * @return The version of the product.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the facultative comment.
     * 
     * @param comment
     *                The facultative comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the product name.
     * 
     * @param name
     *                The product name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the version of the product.
     * 
     * @param version
     *                The version of the product.
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
