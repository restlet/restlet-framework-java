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
package org.restlet.ext.jaxrs.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.Parameter;
import org.restlet.data.Tag;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.util.FormUtils;
import org.restlet.util.Series;

/**
 * This utility class converts Restlet objects to it correponding JAX-RS objects
 * and vice versa.
 * 
 * @author Stephan Koops
 */
public class Converter {

    /**
     * Paramater in the JAX-RS MediaType for the charset. See MIME
     * specification.
     */
    private static final String CHARSET_PARAM = "charset";

    /**
     * Returns the charset of the MediaType as String
     * 
     * @param mediaType
     *            JaxRs-MediaType
     * @return the charset
     */
    public static String getCharset(MediaType mediaType) {
        final Map<String, String> parameters = mediaType.getParameters();
        if (parameters == null) {
            return null;
        }
        return parameters.get(CHARSET_PARAM);
    }

    /**
     * Creates a MediaType without any parameters.
     * 
     * @param mediaType
     *            A MediaType, perhaps with parameters.
     * @return Creates a MediaType without any parameters.
     */
    public static MediaType getMediaTypeWithoutParams(MediaType mediaType) {
        if (mediaType == null) {
            return null;
        }
        final Map<String, String> parameters = mediaType.getParameters();
        if ((parameters == null) || parameters.isEmpty()) {
            return mediaType;
        }
        return new MediaType(mediaType.getType(), mediaType.getSubtype());
    }

    /**
     * Creates a MediaType without any parameters.
     * 
     * @param mediaType
     *            A MediaType, perhaps with parameters.
     * @return Creates a MediaType without any parameters.
     */
    public static org.restlet.data.MediaType getMediaTypeWithoutParams(
            org.restlet.data.MediaType mediaType) {
        if (mediaType == null) {
            return null;
        }
        final Series<Parameter> parameters = mediaType.getParameters();
        if ((parameters == null) || parameters.isEmpty()) {
            return mediaType;
        }
        return mediaType.getParent();
    }

    /**
     * Returns the Charset of the MediaType as String
     * 
     * @param mediaType
     *            JaxRs-MediaType
     * @return the charset
     */
    public static CharacterSet getRestletCharacterSet(MediaType mediaType) {
        final String charset = getCharset(mediaType);
        if (charset == null) {
            return null;
        }
        return CharacterSet.valueOf(charset);
    }

    /**
     * Copies the data in the {@link MultivaluedMap} to the {@link Form}.
     * 
     * @param mmap
     * @return the converted Http headers or null, if null was given.
     */
    public static Form toForm(MultivaluedMap<String, String> mmap) {
        if (mmap == null) {
            return null;
        }
        final Form form = new Form();
        for (final Map.Entry<String, List<String>> entry : mmap.entrySet()) {
            final String name = entry.getKey();
            for (final String value : entry.getValue()) {
                form.add(name, value);
            }
        }
        return form;
    }

    /**
     * Converts the given query String to a Form, but do not decode the data.
     * 
     * @param queryString
     * @return the encoded form
     */
    public static Form toFormEncoded(String queryString) {
        final Form form = new Form();
        FormUtils.parse(form, queryString, null, false, '&');
        return form;
    }

    /**
     * Converts a Restlet Cookie to a JAX-RS Cookie
     * 
     * @param restletCookie
     *            the Restlet Cookie
     * @return the JAX-RS Cookie
     */
    public static Cookie toJaxRsCookie(org.restlet.data.Cookie restletCookie) {
        if (restletCookie == null) {
            return null;
        }
        return new Cookie(restletCookie.getName(), restletCookie.getValue(),
                restletCookie.getPath(), restletCookie.getDomain(),
                restletCookie.getVersion());
    }

    /**
     * Converts a Restlet-EntityTag to a JAX-RS-EntityTag
     * 
     * @param restletEntityTag
     *            the Restlet-EntityTag to convert.
     * @return The corresponding JAX-RS-Entity-Tag
     */
    public static EntityTag toJaxRsEntityTag(Tag restletEntityTag) {
        if (restletEntityTag == null) {
            return null;
        }
        return new EntityTag(restletEntityTag.getName(), restletEntityTag
                .isWeak());
    }

    /**
     * Convert a Restlet MediaType to a JAX-RS MediaType.
     * 
     * @param restletMediaType
     *            the MediaType to convert.
     * @param restletCharacterSet
     *            the CharacterSet for the MediaType; may be null.
     * @return the converted MediaType
     */
    public static MediaType toJaxRsMediaType(
            org.restlet.data.MediaType restletMediaType) {
        return toJaxRsMediaType(restletMediaType, null);
    }

    /**
     * Convert a Restlet MediaType to a JAX-RS MediaType.
     * 
     * @param restletMediaType
     *            the MediaType to convert.
     * @param restletCharacterSet
     *            the CharacterSet for the MediaType; may be null.
     * @return the converted MediaType
     */
    public static MediaType toJaxRsMediaType(
            org.restlet.data.MediaType restletMediaType,
            org.restlet.data.CharacterSet restletCharacterSet) {
        if (restletMediaType == null) {
            return null;
        }
        final Map<String, String> parameters = toMap(restletMediaType
                .getParameters());
        if (restletCharacterSet != null) {
            parameters.put(Converter.CHARSET_PARAM, restletCharacterSet
                    .getName());
        }
        return new MediaType(restletMediaType.getMainType(), restletMediaType
                .getSubType(), parameters);
    }

    /**
     * Converts the Restlet CookieSettings to a JAX-RS NewCookie.
     * 
     * @param cookieSetting
     * @return the JAX-RS NewCookie
     * @throws IllegalArgumentException
     */
    public static NewCookie toJaxRsNewCookie(CookieSetting cookieSetting)
            throws IllegalArgumentException {
        if (cookieSetting == null) {
            return null;
        }
        return new NewCookie(cookieSetting.getName(), cookieSetting.getValue(),
                cookieSetting.getPath(), cookieSetting.getDomain(),
                cookieSetting.getVersion(), cookieSetting.getComment(),
                cookieSetting.getMaxAge(), cookieSetting.isSecure());
    }

    /**
     * Converts the given Restlet Variant to a JAX-RS Variant
     * 
     * @param restletVariant
     * @return the JAX-RS Variant
     * @throws IllegalArgumentException
     *             If the given Variant does not contain exactly one language
     *             and one
     */
    public static javax.ws.rs.core.Variant toJaxRsVariant(
            org.restlet.representation.Variant restletVariant)
            throws IllegalArgumentException {
        final MediaType mediaType = Converter.toJaxRsMediaType(restletVariant
                .getMediaType(), restletVariant.getCharacterSet());
        final Locale language = toLocale(Util
                .getOnlyMetadataName(restletVariant.getLanguages()));
        final String encoding = Util.getOnlyMetadataName(restletVariant
                .getEncodings());
        return new javax.ws.rs.core.Variant(mediaType, language, encoding);
    }

    /**
     * 
     * @param locale
     * @return the Restlet Language
     */
    public static Language toLanguage(Locale locale) {
        return new Language(locale.toString());
    }

    /**
     * 
     * @param locale
     * @return the Restlet Language
     */
    public static Language toLanguage(String locale) {
        return new Language(locale);
    }

    /**
     * @param locale
     * @return the language string
     * @see #toLanguage(Locale)
     */
    public static String toLanguageString(Locale locale) {
        return locale.toString();
    }

    /**
     * Converts a {@link Locale} to a Restlet {@link Language}.
     * 
     * @param language
     * @return the Locale
     * @see #toLocale(String)
     * @see Locale
     */
    public static Locale toLocale(Language language) {
        return toLocale(language.getName());
    }

    /**
     * Converts a locale String to a Restlet {@link Language}.
     * 
     * @param language
     * @return the Locale
     * @see #toLocale(Language)
     * @see Locale
     */
    public static Locale toLocale(String language) {
        if ((language == null) || (language.length() == 0)) {
            return null;
        }
        final StringTokenizer stt = new StringTokenizer(language, "_", true);
        final String lang = stt.nextToken();
        if (stt.hasMoreTokens()) {
            stt.nextToken(); // skip "_"
        }
        if (!stt.hasMoreTokens()) {
            return new Locale(lang);
        }
        String country = stt.nextToken();
        if (country.equals("_")) {
            country = "";
        } else if (stt.hasMoreTokens()) {
            stt.nextToken(); // skip "_"
        }
        if (!stt.hasMoreTokens()) {
            return new Locale(lang, country);
        }
        final String variant = stt.nextToken();
        return new Locale(lang, country, variant);
    }

    /**
     * @param parameters
     * @return the Map
     */
    public static Map<String, String> toMap(Series<Parameter> parameters) {
        if (parameters == null) {
            return null;
        }
        final Map<String, String> map = new HashMap<String, String>();
        for (final Parameter parameter : parameters) {
            map.put(parameter.getName(), parameter.getValue());
        }
        return map;
    }

    /**
     * Converts a JAX-RS Cookie to a Restlet Cookie
     * 
     * @param jaxRsCookie
     *            the JAX-RS Cookie
     * @return the Restlet Cookie
     */
    public static org.restlet.data.Cookie toRestletCookie(Cookie jaxRsCookie) {
        if (jaxRsCookie == null) {
            return null;
        }
        return new org.restlet.data.Cookie(jaxRsCookie.getVersion(),
                jaxRsCookie.getName(), jaxRsCookie.getValue(), jaxRsCookie
                        .getPath(), jaxRsCookie.getDomain());
    }

    /**
     * Converts the Restlet JAX-RS NewCookie to a CookieSettings.
     * 
     * @param newCookie
     * @return the converted CookieSetting
     * @throws IllegalArgumentException
     */
    public static CookieSetting toRestletCookieSetting(NewCookie newCookie)
            throws IllegalArgumentException {
        if (newCookie == null) {
            return null;
        }
        return new CookieSetting(newCookie.getVersion(), newCookie.getName(),
                newCookie.getValue(), newCookie.getPath(), newCookie
                        .getDomain(), newCookie.getComment(), newCookie
                        .getMaxAge(), newCookie.isSecure());
    }

    /**
     * Convert a JAX-RS MediaType to a Restlet MediaType.
     * 
     * @param jaxRsMediaType
     * @return the converted MediaType
     */
    public static org.restlet.data.MediaType toRestletMediaType(
            MediaType jaxRsMediaType) {
        if (jaxRsMediaType == null) {
            return null;
        }
        final Series<Parameter> parameters = Converter
                .toRestletSeries(jaxRsMediaType.getParameters());
        final String name = jaxRsMediaType.getType() + "/"
                + jaxRsMediaType.getSubtype();
        return new org.restlet.data.MediaType(name, parameters);
    }

    /**
     * @param parameters
     * @return a form with the given parameters. Will never return null.
     */
    public static Form toRestletSeries(Map<String, String> parameters) {
        final Form form = new Form();
        if (parameters == null) {
            return form;
        }
        for (final Map.Entry<String, String> parameter : parameters.entrySet()) {
            form.add(parameter.getKey(), parameter.getValue());
        }
        return form;
    }

    /**
     * Converts a JAX-RS-EntityTag to a Restlet-EntityTag
     * 
     * @param jaxRsEntityTag
     *            the JAX-RS-EntityTag to convert.
     * @return The corresponding Restlet-Entity-Tag
     */
    public static Tag toRestletTag(EntityTag jaxRsEntityTag) {
        if (jaxRsEntityTag == null) {
            return null;
        }
        return new Tag(jaxRsEntityTag.getValue(), jaxRsEntityTag.isWeak());
    }

    /**
     * Converts the given JAX-RS Variants to Restlet Variants.
     * 
     * @param jaxRsVariants
     * @return the List of Restlet Variants
     */
    public static List<org.restlet.representation.Variant> toRestletVariants(
            Collection<javax.ws.rs.core.Variant> jaxRsVariants) {
        final List<org.restlet.representation.Variant> restletVariants = new ArrayList<org.restlet.representation.Variant>(
                jaxRsVariants.size());
        for (final javax.ws.rs.core.Variant jaxRsVariant : jaxRsVariants) {
            final org.restlet.representation.Variant restletVariant = new org.restlet.representation.Variant();
            restletVariant.setCharacterSet(getRestletCharacterSet(jaxRsVariant
                    .getMediaType()));
            restletVariant.setEncodings(Util.createList(Encoding
                    .valueOf(jaxRsVariant.getEncoding())));
            restletVariant.setLanguages(Util.createList(toLanguage(jaxRsVariant
                    .getLanguage())));
            restletVariant.setMediaType(toRestletMediaType(jaxRsVariant
                    .getMediaType()));
            restletVariants.add(restletVariant);
        }
        return restletVariants;
    }

    /**
     * @param cacheDirectives
     * @return the converted JAX-RS {@link CacheControl}
     */
    public static CacheControl toJaxRsCacheControl(
            List<CacheDirective> cacheDirectives) {
        CacheControl jaxRsCacheControl = new CacheControl();
        for (CacheDirective cacheDirective : cacheDirectives) {
            if (cacheDirective.getName() == HeaderConstants.CACHE_MAX_AGE)
                jaxRsCacheControl.setMaxAge(Integer.valueOf(cacheDirective
                        .getValue()));
            else if (cacheDirective.getName() == HeaderConstants.CACHE_MUST_REVALIDATE)
                jaxRsCacheControl.setMustRevalidate(true);
            else if (cacheDirective.getName() == HeaderConstants.CACHE_NO_CACHE)
                jaxRsCacheControl.setNoCache(true);
            else if (cacheDirective.getName() == HeaderConstants.CACHE_NO_STORE)
                jaxRsCacheControl.setNoStore(true);
            else if (cacheDirective.getName() == HeaderConstants.CACHE_NO_TRANSFORM)
                jaxRsCacheControl.setNoTransform(true);
            else if (cacheDirective.getName() == HeaderConstants.CACHE_PROXY_MUST_REVALIDATE)
                jaxRsCacheControl.setProxyRevalidate(true);
            else if (cacheDirective.getName() == HeaderConstants.CACHE_PUBLIC)
                jaxRsCacheControl.setPrivate(false);
            else
                jaxRsCacheControl.getCacheExtension().put(
                        cacheDirective.getName(), cacheDirective.getValue());
        }
        return jaxRsCacheControl;
    }

    /**
     * @param cacheControl
     * @return the converted {@link CacheDirective}
     */
    public static List<CacheDirective> toRestletCacheDirective(
            CacheControl cacheControl) {
        List<CacheDirective> directives = new ArrayList<CacheDirective>();
        if (cacheControl.getMaxAge() >= 0)
            directives.add(CacheDirective.maxAge(cacheControl.getMaxAge()));
        if (cacheControl.getSMaxAge() >= 0)
            directives.add(CacheDirective.sharedMaxAge(cacheControl
                    .getSMaxAge()));
        if (!cacheControl.getNoCacheFields().isEmpty())
            directives.add(CacheDirective.noCache(cacheControl
                    .getNoCacheFields()));
        if (!cacheControl.getPrivateFields().isEmpty())
            directives.add(CacheDirective.privateInfo(cacheControl
                    .getPrivateFields()));
        if (cacheControl.isMustRevalidate())
            directives.add(CacheDirective.mustRevalidate());
        if (cacheControl.isNoCache())
            directives.add(CacheDirective.noCache());
        if (cacheControl.isNoStore())
            directives.add(CacheDirective.noStore());
        if (cacheControl.isNoTransform())
            directives.add(CacheDirective.noTransform());
        if (cacheControl.isPrivate())
            directives.add(CacheDirective.privateInfo());
        if (cacheControl.isProxyRevalidate())
            directives.add(CacheDirective.proxyMustRevalidate());
        for (Map.Entry<String, String> c : cacheControl.getCacheExtension()
                .entrySet()) {
            directives.add(new CacheDirective(c.getKey(), c.getValue()));
        }
        return directives;
    }

    private Converter() {
        // no instance creation
    }
}