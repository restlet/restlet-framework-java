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
    /** Product token. */
    private String token;

    /** Version number. */
    private String version;

    /** Comment. */
    private String comment;

    /**
     * Constructor.
     * 
     * @param token
     *                The product token.
     * @param version
     *                The version of the product.
     * @param comment
     *                Facultative comment.
     */
    public Product(String token, String version, String comment) {
        super();
        this.token = token;
        this.version = version;
        this.comment = comment;
    }

    /**
     * Returns the product token.
     * 
     * @return The product token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the product token.
     * 
     * @param token
     *                The product token.
     */
    public void setToken(String token) {
        this.token = token;
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
     * Sets the version of the product.
     * 
     * @param version
     *                The version of the product.
     */
    public void setVersion(String version) {
        this.version = version;
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
     * Sets the facultative comment.
     * 
     * @param comment
     *                The facultative comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

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

    /**
     * Parses a product formatted as defined by the HTTP standard.
     * 
     * @param product
     *                The product string.
     * @return A new product instance.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.8">Product
     *      Tokens</a>
     */
    public static Product parse(final String product) {
        Product result = null;

        String token = null;
        String version = null;
        String comment = null;
        char[] tab = product.trim().toCharArray();
        StringBuilder builder = new StringBuilder();
        int index = 0;
        // Find the token
        for (index = 0; index < tab.length; index++) {
            char c = tab[index];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == ' ') {
                builder.append(c);
            } else {
                break;
            }
        }
        token = builder.toString().trim();
        if (index < tab.length && tab[index] == '/') {
            // Found a version
            index++;
            int spaceIndex = product.indexOf(" ", index);
            if (spaceIndex > -1) {
                // And a comment
                version = product.substring(index, spaceIndex);
                comment = product.substring(spaceIndex + 1);
            } else {
                version = product.substring(index);
            }
        } else {
            if (index < tab.length) {
                // Found a comment
                comment = product.substring(index);
            }
        }

        result = new Product(token, version, comment);
        return result;
    }
}
