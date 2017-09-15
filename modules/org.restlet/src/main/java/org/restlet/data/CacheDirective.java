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

import java.util.List;
import java.util.Objects;

import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.util.SystemUtils;
import org.restlet.util.NamedValue;

/**
 * Directive for caching mechanisms along the call chain. This overrides the
 * default behavior of those caches and proxies.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the
 * "Cache-Control" header.
 * 
 * @author Jerome Louvel
 */
public final class CacheDirective implements NamedValue<String> {

    /**
     * Creates a "max-age" directive. Indicates that the client is willing to
     * accept a response whose age is no greater than the specified time in
     * seconds. Unless "max-stale" directive is also included, the client is not
     * willing to accept a stale response.<br>
     * <br>
     * Note that this directive can be used on requests or responses.
     * 
     * @param maxAge
     *            Maximum age in seconds.
     * @return A new "max-age" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3">HTTP 1.1 - Modifications of the
     *      Basic Expiration Mechanism</a>
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4">HTTP 1.1 - Cache Revalidation and
     *      Reload Controls</a>
     */
    public static CacheDirective maxAge(int maxAge) {
        return new CacheDirective(HeaderConstants.CACHE_MAX_AGE,
                Integer.toString(maxAge), true);
    }

    /**
     * Creates a "max-stale" directive. Indicates that the client is willing to
     * accept a response that has exceeded its expiration time by any amount of
     * time.<br>
     * <br>
     * Note that this directive can be used on requests only.
     * 
     * @return A new "max-stale" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3">HTTP 1.1 - Modifications of the
     *      Basic Expiration Mechanism</a>
     */
    public static CacheDirective maxStale() {
        return new CacheDirective(HeaderConstants.CACHE_MAX_STALE);
    }

    /**
     * Creates a "max-stale" directive. Indicates that the client is willing to
     * accept a response that has exceeded its expiration time by a given amount
     * of time.<br>
     * <br>
     * Note that this directive can be used on requests only.
     * 
     * @param maxStale
     *            Maximum stale age in seconds.
     * @return A new "max-stale" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3">HTTP 1.1 - Modifications of the
     *      Basic Expiration Mechanism</a>
     */
    public static CacheDirective maxStale(int maxStale) {
        return new CacheDirective(HeaderConstants.CACHE_MAX_STALE,
                Integer.toString(maxStale), true);
    }

    /**
     * Creates a "min-fresh" directive. Indicates that the client is willing to
     * accept a response whose freshness lifetime is no less than its current
     * age plus the specified time in seconds. That is, the client wants a
     * response that will still be fresh for at least the specified number of
     * seconds.<br>
     * <br>
     * Note that this directive can be used on requests only.
     * 
     * @param minFresh
     *            Minimum freshness lifetime in seconds.
     * @return A new "min-fresh" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3">HTTP 1.1 - Modifications of the
     *      Basic Expiration Mechanism</a>
     */
    public static CacheDirective minFresh(int minFresh) {
        return new CacheDirective(HeaderConstants.CACHE_MIN_FRESH,
                Integer.toString(minFresh), true);
    }

    /**
     * Creates a "must-revalidate" directive. Indicates that the origin server
     * requires revalidation of a cache entry on any subsequent use.<br>
     * <br>
     * Note that this directive can be used on responses only.
     * 
     * @return A new "must-revalidate" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4">HTTP 1.1 - Cache Revalidation and
     *      Reload Controls</a>
     */
    public static CacheDirective mustRevalidate() {
        return new CacheDirective(HeaderConstants.CACHE_MUST_REVALIDATE);
    }

    /**
     * Creates a "no-cache" directive. Indicates that a cache must not use the
     * response to satisfy subsequent requests without successful revalidation
     * with the origin server.<br>
     * <br>
     * Note that this directive can be used on requests or responses.
     * 
     * @return A new "no-cache" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP 1.1 - What is Cacheable</a>
     */
    public static CacheDirective noCache() {
        return new CacheDirective(HeaderConstants.CACHE_NO_CACHE);
    }

    /**
     * Creates a "no-cache" directive. Indicates that a cache must not use the
     * response to satisfy subsequent requests without successful revalidation
     * with the origin server.<br>
     * <br>
     * Note that this directive can be used on requests or responses.
     * 
     * @param fieldNames
     *            Field names, typically a HTTP header name, that must not be
     *            sent by caches.
     * @return A new "no-cache" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP 1.1 - What is Cacheable</a>
     */
    public static CacheDirective noCache(List<String> fieldNames) {
        StringBuilder sb = new StringBuilder();

        if (fieldNames != null) {
            for (int i = 0; i < fieldNames.size(); i++) {
                sb.append("\"" + fieldNames.get(i) + "\"");

                if (i < fieldNames.size() - 1) {
                    sb.append(',');
                }
            }
        }

        return new CacheDirective(HeaderConstants.CACHE_NO_CACHE, sb.toString());
    }

    /**
     * Creates a "no-cache" directive. Indicates that a cache must not use the
     * response to satisfy subsequent requests without successful revalidation
     * with the origin server.<br>
     * <br>
     * Note that this directive can be used on requests or responses.
     * 
     * @param fieldName
     *            A field name, typically a HTTP header name, that must not be
     *            sent by caches.
     * @return A new "no-cache" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP 1.1 - What is Cacheable</a>
     */
    public static CacheDirective noCache(String fieldName) {
        return new CacheDirective(HeaderConstants.CACHE_NO_CACHE, "\""
                + fieldName + "\"");
    }

    /**
     * Creates a "no-store" directive. Indicates that a cache must not release
     * or retain any information about the call. This applies to both private
     * and shared caches.<br>
     * <br>
     * Note that this directive can be used on requests or responses.
     * 
     * @return A new "no-store" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.2">HTTP 1.1 - What May be Stored by
     *      Caches</a>
     */
    public static CacheDirective noStore() {
        return new CacheDirective(HeaderConstants.CACHE_NO_STORE);
    }

    /**
     * Creates a "no-transform" directive. Indicates that a cache or
     * intermediary proxy must not transform the response entity.<br>
     * <br>
     * Note that this directive can be used on requests or responses.
     * 
     * @return A new "no-transform" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.5">HTTP 1.1 - No-Transform
     *      Directive</a>
     */
    public static CacheDirective noTransform() {
        return new CacheDirective(HeaderConstants.CACHE_NO_TRANSFORM);
    }

    /**
     * Creates a "onlyIfCached" directive. Indicates that only cached responses
     * should be returned to the client.<br>
     * <br>
     * Note that this directive can be used on requests only.
     * 
     * @return A new "only-if-cached" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4">HTTP 1.1 - Cache Revalidation and
     *      Reload Controls</a>
     */
    public static CacheDirective onlyIfCached() {
        return new CacheDirective(HeaderConstants.CACHE_ONLY_IF_CACHED);
    }

    /**
     * Creates a "private" directive. Indicates that all or part of the response
     * message is intended for a single user and must not be cached by a shared
     * cache.<br>
     * <br>
     * Note that this directive can be used on responses only.
     * 
     * @return A new "private" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP 1.1 - What is Cacheable</a>
     */
    public static CacheDirective privateInfo() {
        return new CacheDirective(HeaderConstants.CACHE_PRIVATE);
    }

    /**
     * Creates a "private" directive. Indicates that all or part of the response
     * message is intended for a single user and must not be cached by a shared
     * cache.<br>
     * <br>
     * Note that this directive can be used on responses only.
     * 
     * @param fieldNames
     *            Field names, typically a HTTP header name, that must be
     *            private.
     * @return A new "private" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP 1.1 - What is Cacheable</a>
     */
    public static CacheDirective privateInfo(List<String> fieldNames) {
        StringBuilder sb = new StringBuilder();

        if (fieldNames != null) {
            for (int i = 0; i < fieldNames.size(); i++) {
                sb.append("\"" + fieldNames.get(i) + "\"");

                if (i < fieldNames.size() - 1) {
                    sb.append(',');
                }
            }
        }

        return new CacheDirective(HeaderConstants.CACHE_PRIVATE, sb.toString());
    }

    /**
     * Creates a "private" directive. Indicates that all or part of the response
     * message is intended for a single user and must not be cached by a shared
     * cache.<br>
     * <br>
     * Note that this directive can be used on responses only.
     * 
     * @param fieldName
     *            A field name, typically a HTTP header name, that is private.
     * @return A new "private" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP 1.1 - What is Cacheable</a>
     */
    public static CacheDirective privateInfo(String fieldName) {
        return new CacheDirective(HeaderConstants.CACHE_PRIVATE, "\""
                + fieldName + "\"");
    }

    /**
     * Creates a "proxy-revalidate" directive. Indicates that the origin server
     * requires revalidation of a cache entry on any subsequent use, except that
     * it does not apply to non-shared user agent caches<br>
     * <br>
     * Note that this directive can be used on responses only.
     * 
     * @return A new "proxy-revalidate" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4">HTTP 1.1 - Cache Revalidation and
     *      Reload Controls</a>
     */
    public static CacheDirective proxyMustRevalidate() {
        return new CacheDirective(HeaderConstants.CACHE_PROXY_MUST_REVALIDATE);
    }

    /**
     * Creates a "public" directive. Indicates that the response may be cached
     * by any cache, even if it would normally be non-cacheable or cacheable
     * only within a non-shared cache.<br>
     * <br>
     * Note that this directive can be used on responses only.
     * 
     * @return A new "public" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.1">HTTP 1.1 - What is Cacheable</a>
     */
    public static CacheDirective publicInfo() {
        return new CacheDirective(HeaderConstants.CACHE_PUBLIC);
    }

    /**
     * Creates a "s-maxage" directive. Indicates that the client is willing to
     * accept a response from a shared cache (but not a private cache) whose age
     * is no greater than the specified time in seconds.<br>
     * <br>
     * Note that this directive can be used on responses only.
     * 
     * @param sharedMaxAge
     *            Maximum age in seconds.
     * @return A new "s-maxage" directive.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.3">HTTP 1.1 - Modifications of the
     *      Basic Expiration Mechanism</a>
     */
    public static CacheDirective sharedMaxAge(int sharedMaxAge) {
        return new CacheDirective(HeaderConstants.CACHE_SHARED_MAX_AGE,
                Integer.toString(sharedMaxAge), true);
    }

    /** Indicates if the directive is a digit value. */
    private boolean digit;

    /** The name. */
    private volatile String name;

    /** The value. */
    private volatile String value;

    /**
     * Constructor for directives with no value.
     * 
     * @param name
     *            The directive name.
     */
    public CacheDirective(String name) {
        this(name, null);
    }

    /**
     * Constructor for directives with a value.
     * 
     * @param name
     *            The directive name.
     * @param value
     *            The directive value.
     */
    public CacheDirective(String name, String value) {
        this(name, value, false);
    }

    /**
     * Constructor for directives with a value.
     * 
     * @param name
     *            The directive name.
     * @param value
     *            The directive value.
     * @param digit
     *            The kind of value (true for a digit value, false otherwise).
     */
    public CacheDirective(String name, String value, boolean digit) {
        this.name = name;
        this.value = value;
        this.digit = digit;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CacheDirective)) {
            return false;
        }

        CacheDirective that = (CacheDirective) obj;
        
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getValue(), that.getValue())
                && (this.digit == that.digit);
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public String getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName(), getValue(), isDigit());
    }

    /**
     * Returns true if the directive contains a digit value.
     * 
     * @return True if the directive contains a digit value.
     */
    public boolean isDigit() {
        return digit;
    }

    /**
     * Indicates if the directive is a digit value.
     * 
     * @param digit
     *            True if the directive contains a digit value.
     */
    public void setDigit(boolean digit) {
        this.digit = digit;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CacheDirective [digit=" + digit + ", name=" + name + ", value="
                + value + "]";
    }
}
