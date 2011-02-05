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

package org.restlet.ext.jaxrs.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

import org.restlet.ext.jaxrs.internal.util.OneElementIterator;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Concrete implementation of abstract class {@link VariantListBuilder}
 * 
 * @author Stephan Koops
 */
public class VariantListBuilderImpl extends VariantListBuilder {

    /**
     * Creates an Iterator over the elements of the list. If the list is null or
     * empty, an Iterator with exact one element is returned: null.
     * 
     * @param <T>
     * @param list
     * @return an {@link Iterator} with at least one element (perhaps null).
     *         never null.
     */
    private static <T> Iterator<T> createIterator(List<T> list) {
        if ((list == null) || list.isEmpty()) {
            return new OneElementIterator<T>(null);
        }
        return list.iterator();
    }

    private List<MediaType> mediaTypes;

    private List<Locale> languages;

    private List<String> encodings;

    private List<Variant> variants;

    /**
     * Creates a new VariantListBuilder
     */
    public VariantListBuilderImpl() {
    }

    /**
     * Add the current combination of metadata to the list of supported
     * variants, after this method is called the current combination of metadata
     * is emptied. If more than one value is supplied for one or more of the
     * variant properties then a variant will be generated for each possible
     * combination. E.g. in the following <code>list</code> would have four
     * members:
     * <p>
     * 
     * <pre>
     * List&lt;Variant&gt; list = VariantListBuilder.newInstance().languages(&quot;en&quot;,&quot;fr&quot;)
     *   .encodings(&quot;zip&quot;, &quot;identity&quot;).add().build()
     * </pre>
     * 
     * 
     * @return the updated builder
     * @see javax.ws.rs.core.Variant.VariantListBuilder#add()
     */
    @Override
    public VariantListBuilder add() {
        buildVariants();
        return this;
    }

    /**
     * Build a list of representation variants from the current state of the
     * builder. After this method is called the builder is reset to an empty
     * state.
     * 
     * @return a list of representation variants
     * @see javax.ws.rs.core.Variant.VariantListBuilder#build()
     */
    @Override
    public List<Variant> build() {
        if (Util.isNotEmpty(this.encodings) || Util.isNotEmpty(this.languages)
                || Util.isNotEmpty(this.mediaTypes)) {
            buildVariants();
        }
        List<Variant> variants = this.variants;
        this.variants = null;
        return variants;
    }

    private void buildVariants() {
        Iterator<MediaType> mediaTypeIter = createIterator(this.mediaTypes);
        if (this.variants == null) {
            this.variants = new ArrayList<Variant>();
        }
        while (mediaTypeIter.hasNext()) {
            MediaType mediaType = mediaTypeIter.next();
            Iterator<Locale> languageIter = createIterator(this.languages);
            while (languageIter.hasNext()) {
                Locale language = languageIter.next();
                Iterator<String> encodingIter = createIterator(this.encodings);
                while (encodingIter.hasNext()) {
                    String encoding = encodingIter.next();
                    Variant variant = new Variant(mediaType, language, encoding);
                    this.variants.add(variant);
                }
            }
        }
        this.encodings.clear();
        this.languages.clear();
        this.mediaTypes.clear();
    }

    /**
     * Set the encoding[s] for this variant.
     * 
     * @param encodings
     *            the available encodings
     * @return the updated builder
     * @see javax.ws.rs.core.Variant.VariantListBuilder#encodings(java.lang.String[])
     */
    @Override
    public VariantListBuilder encodings(String... encodings) {
        if (this.encodings == null) {
            this.encodings = new ArrayList<String>();
        }
        for (String encoding : encodings) {
            this.encodings.add(encoding);
        }
        return this;
    }

    /**
     * Set the language[s] for this variant.
     * 
     * @param languages
     *            the available languages
     * @return the updated builder
     * @see javax.ws.rs.core.Variant.VariantListBuilder#languages(java.lang.String[])
     */
    @Override
    public VariantListBuilder languages(Locale... languages) {
        if (this.languages == null) {
            this.languages = new ArrayList<Locale>();
        }
        for (Locale language : languages) {
            this.languages.add(language);
        }
        return this;
    }

    /**
     * Set the media type[s] for this variant.
     * 
     * @param mediaTypes
     *            the available mediaTypes. If specific charsets are supported
     *            they should be included as parameters of the respective media
     *            type.
     * @return the updated builder
     * @see javax.ws.rs.core.Variant.VariantListBuilder#mediaTypes(javax.ws.rs.core.MediaType[])
     */
    @Override
    public VariantListBuilder mediaTypes(MediaType... mediaTypes) {
        if (this.mediaTypes == null) {
            this.mediaTypes = new ArrayList<MediaType>();
        }
        for (MediaType mediaType : mediaTypes) {
            this.mediaTypes.add(mediaType);
        }
        return this;
    }
}