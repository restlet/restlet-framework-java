/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.Parameter;
import org.restlet.data.Tag;
import org.restlet.util.Engine;
import org.restlet.util.Series;

/**
 * @author Stephan
 * 
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
     *                JaxRs-MediaType
     * @return the charset
     */
    public static String getCharset(MediaType mediaType) {
        return mediaType.getParameters().get(CHARSET_PARAM);
    }

    /**
     * Creates a MediaType without any parameters.
     * 
     * @param mediaType
     *                A MediaType, perhaps with parameters.
     * @return Creates a MediaType without any parameters.
     */
    public static MediaType getMediaTypeWithoutParams(MediaType mediaType) {
        if (mediaType == null)
            return null;
        Map<String, String> parameters = mediaType.getParameters();
        if (parameters == null || parameters.isEmpty())
            return mediaType;
        return new MediaType(mediaType.getType(), mediaType.getSubtype());
    }

    /**
     * Creates a MediaType without any parameters.
     * 
     * @param mediaType
     *                A MediaType, perhaps with parameters.
     * @return Creates a MediaType without any parameters.
     */
    public static org.restlet.data.MediaType getMediaTypeWithoutParams(
            org.restlet.data.MediaType mediaType) {
        if (mediaType == null)
            return null;
        Series<Parameter> parameters = mediaType.getParameters();
        if (parameters == null || parameters.isEmpty())
            return mediaType;
        return new org.restlet.data.MediaType(mediaType.getName());
    }

    /**
     * Returns the Charset of the MediaType as String
     * 
     * @param mediaType
     *                JaxRs-MediaType
     * @return the charset
     */
    public static CharacterSet getRestletCharacterSet(MediaType mediaType) {
        String charset = getCharset(mediaType);
        if (charset == null)
            return null;
        return CharacterSet.valueOf(charset);
    }

    /**
     * Converts a Restlet Cookie to a JAX-RS Cookie
     * 
     * @param restletCookie
     *                the Restlet Cookie
     * @return the JAX-RS Cookie
     */
    public static Cookie toJaxRsCookie(org.restlet.data.Cookie restletCookie) {
        if (restletCookie == null)
            return null;
        return new Cookie(restletCookie.getName(), restletCookie.getValue(),
                restletCookie.getPath(), restletCookie.getDomain(),
                restletCookie.getVersion());
    }

    /**
     * Converts a Restlet-EntityTag to a JAX-RS-EntityTag
     * 
     * @param restletEntityTag
     *                the Restlet-EntityTag to convert.
     * @return The corresponding JAX-RS-Entity-Tag
     */
    public static EntityTag toJaxRsEntityTag(Tag restletEntityTag) {
        if (restletEntityTag == null)
            return null;
        return new EntityTag(restletEntityTag.getName(), restletEntityTag
                .isWeak());
    }

    /**
     * Convert a Restlet MediaType to a JAX-RS MediaType.
     * 
     * @param restletMediaType
     *                the MediaType to convert.
     * @param restletCharacterSet
     *                the CharacterSet for the MediaType; may be null.
     * @return the converted MediaType
     */
    public static MediaType toJaxRsMediaType(
            org.restlet.data.MediaType restletMediaType,
            org.restlet.data.CharacterSet restletCharacterSet) {
        if (restletMediaType == null)
            return null;
        Map<String, String> parameters = toMap(restletMediaType.getParameters());
        if (restletCharacterSet != null)
            parameters.put(Converter.CHARSET_PARAM, restletCharacterSet
                    .getName());
        return new MediaType(restletMediaType.getMainType(), restletMediaType
                .getSubType(), parameters);
    }

    /**
     * Converts the Restlet CookieSettings to a JAX-RS NewCookie.
     * 
     * @param cookieSetting
     * @return
     * @throws IllegalArgumentException
     */
    public static NewCookie toJaxRsNewCookie(CookieSetting cookieSetting)
            throws IllegalArgumentException {
        if (cookieSetting == null)
            return null;
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
     *                 If the given Variant does not contain exactly one
     *                 language and one
     */
    public static javax.ws.rs.core.Variant toJaxRsVariant(
            org.restlet.resource.Variant restletVariant)
            throws IllegalArgumentException {
        MediaType mediaType = Converter.toJaxRsMediaType(restletVariant
                .getMediaType(), restletVariant.getCharacterSet());
        String language = Util.getOnlyMetadataName(restletVariant
                .getLanguages());
        String encoding = Util.getOnlyMetadataName(restletVariant
                .getEncodings());
        return new javax.ws.rs.core.Variant(mediaType, language, encoding);
    }

    /**
     * @param parameters
     * @return
     */
    public static Map<String, String> toMap(Series<Parameter> parameters) {
        if (parameters == null)
            return null;
        Map<String, String> map = new HashMap<String, String>();
        for (Parameter parameter : parameters) {
            map.put(parameter.getName(), parameter.getValue());
        }
        return map;
    }

    /**
     * Converts a JAX-RS Cookie to a Restlet Cookie
     * 
     * @param jaxRsCookie
     *                the JAX-RS Cookie
     * @return the Restlet Cookie
     */
    public static org.restlet.data.Cookie toRestletCookie(Cookie jaxRsCookie) {
        if (jaxRsCookie == null)
            return null;
        return new org.restlet.data.Cookie(jaxRsCookie.getVersion(),
                jaxRsCookie.getName(), jaxRsCookie.getValue(), jaxRsCookie
                        .getPath(), jaxRsCookie.getDomain());
    }

    /**
     * Converts the Restlet JAX-RS NewCookie to a CookieSettings.
     * 
     * @param newCookie
     * @return
     * @throws IllegalArgumentException
     */
    public static CookieSetting toRestletCookieSetting(NewCookie newCookie)
            throws IllegalArgumentException {
        if (newCookie == null)
            return null;
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
        if (jaxRsMediaType == null)
            return null;
        Series<Parameter> parameters = Converter.toRestletSeries(jaxRsMediaType
                .getParameters());
        String name = jaxRsMediaType.getType() + "/"
                + jaxRsMediaType.getSubtype();
        return new org.restlet.data.MediaType(name, parameters);
    }

    /**
     * @param parameters
     * @return a form with the given parameters. Will never return null.
     */
    public static Form toRestletSeries(Map<String, String> parameters) {
        Form form = new Form();
        if (parameters == null)
            return form;
        for (Map.Entry<String, String> parameter : parameters.entrySet())
            form.add(parameter.getKey(), parameter.getValue());
        return form;
    }

    /**
     * Converts a JAX-RS-EntityTag to a Restlet-EntityTag
     * 
     * @param jaxRsEntityTag
     *                the JAX-RS-EntityTag to convert.
     * @return The corresponding Restlet-Entity-Tag
     */
    public static Tag toRestletTag(EntityTag jaxRsEntityTag) {
        if (jaxRsEntityTag == null)
            return null;
        return new Tag(jaxRsEntityTag.getValue(), jaxRsEntityTag.isWeak());
    }

    /**
     * Converts the given JAX-RS Variants to Restlet Variants.
     * 
     * @param jaxRsVariants
     * @return
     */
    public static List<org.restlet.resource.Variant> toRestletVariants(
            Collection<javax.ws.rs.core.Variant> jaxRsVariants) {
        List<org.restlet.resource.Variant> restletVariants = new ArrayList<org.restlet.resource.Variant>(
                jaxRsVariants.size());
        for (javax.ws.rs.core.Variant jaxRsVariant : jaxRsVariants) {
            org.restlet.resource.Variant restletVariant = new org.restlet.resource.Variant();
            restletVariant.setCharacterSet(getRestletCharacterSet(jaxRsVariant
                    .getMediaType()));
            restletVariant.setEncodings(Util.createList(Encoding
                    .valueOf(jaxRsVariant.getEncoding())));
            restletVariant.setLanguages(Util.createList(Language
                    .valueOf(jaxRsVariant.getLanguage())));
            restletVariant.setMediaType(toRestletMediaType(jaxRsVariant
                    .getMediaType()));
            restletVariants.add(restletVariant);
        }
        return restletVariants;
    }

    private Converter() {
        // no instance creation
    }

    /**
     * Copies the data in the {@link MultivaluedMap} to the {@link Form}.
     * 
     * @param mmap
     * @return the converted Http headers or null, if null was given.
     */
    public static Form toForm(MultivaluedMap<String, String> mmap) {
        if (mmap == null)
            return null;
        Form form = new Form();
        for (Map.Entry<String, List<String>> entry : mmap.entrySet()) {
            String name = entry.getKey();
            for (String value : entry.getValue())
                form.add(name, value);
        }
        return form;
    }

    /**
     * Converts the given query String to a Form, but do not decode the data.
     * 
     * @param queryString
     * @param logger
     * @return
     */
    public static Form toFormEncoded(String queryString, Logger logger) {
        Form form = new Form();
        Engine.getInstance().parse(logger, form, queryString, null, false);
        return form;
    }
}