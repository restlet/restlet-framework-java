package org.restlet.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Product tokens are used to allow communicating applications to identify
 * themselves by software name and version. .
 * 
 * @author Thierry Boileau (contact@noelios.com)
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43">User-Agent</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.8">Product
 *      Tokens</a>
 */
public class Product {

    /**
     * Parses a product formatted as defined by the HTTP standard.
     * 
     * @param product
     *                The product string.
     * @return A list of new product instances.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43">User-Agent</a>
     */
    public static List<Product> parseUserAgent(final String product) {
        List<Product> result = new ArrayList<Product>();

        if (product != null) {
            String token = null;
            String version = null;
            String comment = null;
            char[] tab = product.trim().toCharArray();
            StringBuilder tokenBuilder = new StringBuilder();
            StringBuilder versionBuilder = null;
            StringBuilder commentBuilder = null;
            int index = 0;
            boolean insideToken = true;
            boolean insideVersion = false;
            boolean insideComment = false;

            for (index = 0; index < tab.length; index++) {
                char c = tab[index];
                if (insideToken) {
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                            || c == ' ') {
                        tokenBuilder.append(c);
                    } else {
                        token = tokenBuilder.toString().trim();
                        insideToken = false;
                        if (c == '/') {
                            insideVersion = true;
                            versionBuilder = new StringBuilder();
                            version = null;
                        } else if (c == '(' || c == '{') {
                            insideComment = true;
                            commentBuilder = new StringBuilder();
                            comment = null;
                            commentBuilder.append(c);
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
                        if (c == '(' || c == '{') {
                            insideComment = true;
                            commentBuilder = new StringBuilder();
                            comment = null;
                            commentBuilder.append(c);
                        } else {
                            if (insideComment) {
                                if (c == ')' || c == '}') {
                                    insideComment = false;
                                    commentBuilder.append(c);
                                    comment = commentBuilder.toString();
                                    result.add(new Product(token, version,
                                            comment));
                                    comment = null;
                                    insideToken = true;
                                    tokenBuilder = new StringBuilder();
                                    token = null;
                                } else {
                                    commentBuilder.append(c);
                                }
                            } else {
                                result
                                        .add(new Product(token, version,
                                                comment));
                                insideToken = true;
                                tokenBuilder = new StringBuilder();
                                tokenBuilder.append(c);
                                token = null;
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
                    if (insideToken && tokenBuilder.length() > 0) {
                        token = tokenBuilder.toString();
                        result.add(new Product(token, null, null));
                    }
                }
            }
        }

        return result;
    }

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
