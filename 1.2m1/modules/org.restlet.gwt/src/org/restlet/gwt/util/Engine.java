/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.gwt.util;

import java.util.Collection;
import java.util.List;

import org.restlet.gwt.Client;
import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.ClientInfo;
import org.restlet.gwt.data.Cookie;
import org.restlet.gwt.data.CookieSetting;
import org.restlet.gwt.data.Dimension;
import org.restlet.gwt.data.Form;
import org.restlet.gwt.data.Language;
import org.restlet.gwt.data.MediaType;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Product;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.engine.Helper;
import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.resource.Variant;

/**
 * Facade to the engine implementating the Restlet API. Note that this is an SPI
 * class that is not intended for public usage.
 * 
 * @author Jerome Louvel
 */
public abstract class Engine {

    /** The registered engine. */
    private static Engine instance = null;

    /** Major version number. */
    public static final String MAJOR_NUMBER = "@major-number@";

    /** Minor version number. */
    public static final String MINOR_NUMBER = "@minor-number@";

    /** Release number. */
    public static final String RELEASE_NUMBER = "@release-type@@release-number@";

    /** Complete version. */
    public static final String VERSION = MAJOR_NUMBER + '.' + MINOR_NUMBER
            + '.' + RELEASE_NUMBER;

    /**
     * Returns the registered Restlet engine.
     * 
     * @return The registered Restlet engine.
     */
    public static Engine getInstance() {
        if (instance == null) {
            org.restlet.gwt.engine.Engine.register();
        }

        return instance;
    }

    /**
     * Computes the hash code of a set of objects. Follows the algorithm
     * specified in List.hasCode().
     * 
     * @param objects
     *            the objects to compute the hashCode
     * 
     * @return The hash code of a set of objects.
     */
    public static int hashCode(Object... objects) {
        int result = 1;

        if (objects != null) {
            for (final Object obj : objects) {
                result = 31 * result + (obj == null ? 0 : obj.hashCode());
            }
        }

        return result;
    }

    /**
     * Sets the registered Restlet engine.
     * 
     * @param engine
     *            The registered Restlet engine.
     */
    public static void setInstance(Engine engine) {
        instance = engine;
    }

    /**
     * Copies the given header parameters into the given {@link Response}.
     * 
     * @param headers
     *            The headers to copy.
     * @param response
     *            The response to update. Must contain a {@link Representation}
     *            to copy the representation headers in it.
     */
    public abstract void copyResponseHeaders(Iterable<Parameter> headers,
            Response response);

    /**
     * Creates a new helper for a given client connector.
     * 
     * @param client
     *            The client to help.
     * @return The new helper.
     */
    public abstract Helper<Client> createHelper(Client client);

    /**
     * Formats the given Cookie to a String
     * 
     * @param cookie
     * @return the Cookie as String
     * @throws IllegalArgumentException
     *             Thrown if the Cookie contains illegal values
     */
    public abstract String formatCookie(Cookie cookie)
            throws IllegalArgumentException;

    /**
     * Formats the given CookieSetting to a String
     * 
     * @param cookieSetting
     * @return the CookieSetting as String
     * @throws IllegalArgumentException
     *             Thrown if the CookieSetting contains illegal values
     */
    public abstract String formatCookieSetting(CookieSetting cookieSetting)
            throws IllegalArgumentException;

    /**
     * Formats the given Set of Dimensions to a String for the HTTP Vary header.
     * 
     * @param dimensions
     *            the dimensions to format.
     * @return the Vary header or null, if dimensions is null or empty.
     */
    public abstract String formatDimensions(Collection<Dimension> dimensions);

    /**
     * Formats the given List of Products to a String.
     * 
     * @param products
     *            The list of products to format.
     * @return the List of Products as String.
     * @throws IllegalArgumentException
     *             Thrown if the List of Products contains illegal values
     */
    public abstract String formatUserAgent(List<Product> products)
            throws IllegalArgumentException;

    /**
     * Returns the best variant representation for a given resource according
     * the the client preferences.<br>
     * A default language is provided in case the variants don't match the
     * client preferences.
     * 
     * @param client
     *            The client preferences.
     * @param variants
     *            The list of variants to compare.
     * @param defaultLanguage
     *            The default language.
     * @return The preferred variant.
     * @see <a href=
     *      "http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm"
     *      >Apache content negotiation algorithm</a>
     */
    public abstract Variant getPreferredVariant(ClientInfo client,
            List<Variant> variants, Language defaultLanguage);

    /**
     * Parses a representation into a form.
     * 
     * @param form
     *            The target form.
     * @param representation
     *            The representation to parse.
     */
    public abstract void parse(Form form, Representation representation);

    /**
     * Parses a parameters string to parse into a given form.
     * 
     * @param form
     *            The target form.
     * @param parametersString
     *            The parameters string to parse.
     * @param characterSet
     *            The supported character encoding.
     * @param decode
     *            Indicates if the parameters should be decoded using the given
     *            character set.
     * @param separator
     *            The separator character to append between parameters.
     */
    public abstract void parse(Form form, String parametersString,
            CharacterSet characterSet, boolean decode, char separator);

    /**
     * Parses the given Content Type.
     * 
     * @param contentType
     *            the Content Type as String
     * @return the ContentType as MediaType; charset etc. are parameters.
     * @throws IllegalArgumentException
     *             if the String can not be parsed.
     */
    public abstract MediaType parseContentType(String contentType)
            throws IllegalArgumentException;

    /**
     * Parses the given String to a Cookie
     * 
     * @param cookie
     * @return the Cookie parsed from the String
     * @throws IllegalArgumentException
     *             Thrown if the String can not be parsed as Cookie.
     */
    public abstract Cookie parseCookie(String cookie)
            throws IllegalArgumentException;

    /**
     * Parses the given String to a CookieSetting
     * 
     * @param cookieSetting
     * @return the CookieSetting parsed from the String
     * @throws IllegalArgumentException
     *             Thrown if the String can not be parsed as CookieSetting.
     */
    public abstract CookieSetting parseCookieSetting(String cookieSetting)
            throws IllegalArgumentException;

    /**
     * Parses the given user agent String to a list of Product instances.
     * 
     * @param userAgent
     * @return the List of Product objects parsed from the String
     * @throws IllegalArgumentException
     *             Thrown if the String can not be parsed as a list of Product
     *             instances.
     */
    public abstract List<Product> parseUserAgent(String userAgent)
            throws IllegalArgumentException;

}
