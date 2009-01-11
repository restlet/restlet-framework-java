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

package org.restlet.util;

import java.util.Collection;
import java.util.List;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Dimension;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.Parameter;
import org.restlet.data.Product;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.Helper;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Facade to the engine implementating the Restlet API. Note that this is an SPI
 * class that is not intended for public usage.
 * 
 * @author Jerome Louvel
 */
public abstract class Engine {

    /** The registered engine. */
    private static volatile Engine instance = null;

    /** Major version number. */
    public static final String MAJOR_NUMBER = "@major-number@";

    /** Minor version number. */
    public static final String MINOR_NUMBER = "@minor-number@";

    /** Release number. */
    public static final String RELEASE_NUMBER = "@release-type@@release-number@";

    /** User class loader to use for dynamic class loading. */
    private static volatile ClassLoader userClassLoader;

    /** Complete version. */
    public static final String VERSION = MAJOR_NUMBER + '.' + MINOR_NUMBER
            + '.' + RELEASE_NUMBER;

    /**
     * Returns the best class loader, first the engine class loader if available
     * using {@link #getUserClassLoader()}, otherwise the current thread context
     * class loader, or finally the classloader of the current class.
     * 
     * @return The best class loader.
     */
    public static ClassLoader getClassLoader() {
        ClassLoader result = getUserClassLoader();

        if (result == null) {
            result = Thread.currentThread().getContextClassLoader();
        }

        if (result == null) {
            result = Class.class.getClassLoader();
        }

        if (result == null) {
            result = ClassLoader.getSystemClassLoader();
        }

        return result;
    }

    /**
     * Returns the registered Restlet engine.
     * 
     * @return The registered Restlet engine.
     */
    public static Engine getInstance() {
        Engine result = instance;

        if (result == null) {
            result = new org.restlet.engine.Engine();
        }

        return result;
    }

    /**
     * Returns the class loader specified by the user and that should be used in
     * priority.
     * 
     * @return The user class loader
     */
    private static ClassLoader getUserClassLoader() {
        return userClassLoader;
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
     * Returns the class object for the given name using the given class loader.
     * 
     * @param classLoader
     *            The class loader to use.
     * @param className
     *            The class name to lookup.
     * @return The class object or null.
     */
    private static Class<?> loadClass(ClassLoader classLoader, String className) {
        Class<?> result = null;

        if (classLoader != null) {
            try {
                result = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                // Do nothing
            }
        }

        return result;
    }

    /**
     * Returns the class object for the given name using the engine class loader
     * fist, then the current thread context class loader, or the classloader of
     * the current class.
     * 
     * @param className
     *            The class name to lookup.
     * @return The class object or null if the class was not found.
     */
    public static Class<?> loadClass(String className)
            throws ClassNotFoundException {
        Class<?> result = null;

        // First, try using the engine class loader
        result = loadClass(getUserClassLoader(), className);

        // Then, try using the current thread context class loader
        if (result == null) {
            result = loadClass(Thread.currentThread().getContextClassLoader(),
                    className);
        }

        // Then, try using the current class's class loader
        if (result == null) {
            result = loadClass(Class.class.getClassLoader(), className);
        }

        // Then, try using the caller's class loader
        if (result == null) {
            result = Class.forName(className);
        }

        // Finally try using the system class loader
        if (result == null) {
            result = loadClass(ClassLoader.getSystemClassLoader(), className);
        }

        if (result == null) {
            throw new ClassNotFoundException(className);
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
     * Sets the user class loader that should used in priority.
     * 
     * @param newClassLoader
     *            The new user class loader to use.
     */
    public static void setUserClassLoader(ClassLoader newClassLoader) {
        userClassLoader = newClassLoader;
    }

    /**
     * Indicates if the call is properly authenticated. By default, this
     * delegates credential checking to checkSecret().
     * 
     * @param request
     *            The request to authenticate.
     * @param guard
     *            The associated guard to callback.
     * @return -1 if the given credentials were invalid, 0 if no credentials
     *         were found and 1 otherwise.
     * @see Guard#checkSecret(Request, String, char[])
     */
    public abstract int authenticate(Request request, Guard guard);

    /**
     * Challenges the client by adding a challenge request to the response and
     * by setting the status to CLIENT_ERROR_UNAUTHORIZED.
     * 
     * @param response
     *            The response to update.
     * @param stale
     *            Indicates if the new challenge is due to a stale response.
     * @param guard
     *            The associated guard to callback.
     */
    public abstract void challenge(Response response, boolean stale, Guard guard);

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
     * Copies the headers of the given {@link Response} into the given
     * {@link Series}.
     * 
     * @param response
     *            The response to update. Should contain a
     *            {@link Representation} to copy the representation headers from
     *            it.
     * @param headers
     *            The Series to copy the headers in.
     */
    public abstract void copyResponseHeaders(Response response,
            Series<Parameter> headers);

    /**
     * Creates a new helper for a given component.
     * 
     * @param application
     *            The application to help.
     * @return The new helper.
     */
    public abstract Helper<Application> createHelper(Application application);

    /**
     * Creates a new helper for a given client connector.
     * 
     * @param client
     *            The client to help.
     * @param helperClass
     *            Optional helper class name.
     * @return The new helper.
     */
    public abstract Helper<Client> createHelper(Client client,
            String helperClass);

    /**
     * Creates a new helper for a given component.
     * 
     * @param component
     *            The component to help.
     * @return The new helper.
     */
    public abstract Helper<Component> createHelper(Component component);

    /**
     * Creates a new helper for a given server connector.
     * 
     * @param server
     *            The server to help.
     * @param helperClass
     *            Optional helper class name.
     * @return The new helper.
     */
    public abstract Helper<Server> createHelper(Server server,
            String helperClass);

    /**
     * Indicates that a Restlet's context has changed.
     * 
     * @param restlet
     *            The Restlet with a changed context.
     * @param context
     *            The new context.
     */
    public abstract void fireContextChanged(Restlet restlet, Context context);

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
     * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
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

    /**
     * Converts the given bytes array into a Base64 String.
     * 
     * @param target
     *            The bytes array to encode.
     * @return The Base64 String.
     */
    public abstract String toBase64(byte[] target);

    /**
     * Returns the MD5 digest of the target string. Target is decoded to bytes
     * using the US-ASCII charset. The returned hexidecimal String always
     * contains 32 lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *            The string to encode.
     * @return The MD5 digest of the target string.
     */
    public abstract String toMd5(String target);

}
