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

package org.restlet.engine.util;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

/**
 * Content negotiation algorithm.
 * 
 * @author Jerome Louvel
 */
public class Conneg {

    /** The enriched list of character set preferences. */
    private volatile List<Preference<CharacterSet>> characterSetPrefs;

    /** The client preferences. */
    private volatile ClientInfo clientInfo;

    /** The enriched list of encoding preferences. */
    private volatile List<Preference<Encoding>> encodingPrefs;

    /** The enriched list of language preferences. */
    private volatile List<Preference<Language>> languagePrefs;

    /** The enriched list of media type preferences. */
    private volatile List<Preference<MediaType>> mediaTypePrefs;

    /** The metadata service. */
    private volatile MetadataService metadataService;

    /**
     * Constructor.
     * 
     * @param clientInfo
     * @param metadataService
     */
    public Conneg(ClientInfo clientInfo, MetadataService metadataService) {
        this.clientInfo = clientInfo;
        this.metadataService = metadataService;

        if (clientInfo != null) {
            // Get the enriched user preferences
            this.languagePrefs = getEnrichedPreferences(clientInfo
                    .getAcceptedLanguages(), (metadataService == null) ? null
                    : metadataService.getDefaultLanguage(), Language.ALL);
            this.mediaTypePrefs = getEnrichedPreferences(clientInfo
                    .getAcceptedMediaTypes(), (metadataService == null) ? null
                    : metadataService.getDefaultMediaType(), MediaType.ALL);
            this.characterSetPrefs = getEnrichedPreferences(clientInfo
                    .getAcceptedCharacterSets(),
                    (metadataService == null) ? null : metadataService
                            .getDefaultCharacterSet(), CharacterSet.ALL);
            this.encodingPrefs = getEnrichedPreferences(clientInfo
                    .getAcceptedEncodings(), (metadataService == null) ? null
                    : metadataService.getDefaultEncoding(), Encoding.ALL);
        }
    }

    /**
     * Returns true if the metadata can be added.
     * 
     * @param <T>
     * @param metadata
     *            The metadata to add.
     * @param undesired
     *            The list of proscribed metadata.
     * @return True if the metadata can be added.
     */
    private <T extends Metadata> boolean canAdd(T metadata, List<T> undesired) {
        boolean add = true;
        if (undesired != null) {
            for (T u : undesired) {
                if (u.equals(metadata)) {
                    add = false;
                    break;
                }
            }
        }

        return add;
    }

    /**
     * Returns the enriched list of character set preferences.
     * 
     * @return The enriched list of character set preferences.
     */
    protected List<Preference<CharacterSet>> getCharacterSetPrefs() {
        return characterSetPrefs;
    }

    /**
     * Returns the client preferences.
     * 
     * @return The client preferences.
     */
    protected ClientInfo getClientInfo() {
        return clientInfo;
    }

    /**
     * Returns the enriched list of encoding preferences.
     * 
     * @return The enriched list of encoding preferences.
     */
    protected List<Preference<Encoding>> getEncodingPrefs() {
        return encodingPrefs;
    }

    /**
     * Returns an enriched list of preferences. Contains the user preferences,
     * implied user parent preferences (quality between 0.005 and 0.006),
     * default preference (quality of 0.003), default parent preference (quality
     * of 0.002), all preference (quality of 0.001).<br>
     * <br>
     * This necessary to compensate the misconfiguration of many browsers which
     * don't expose all the metadata actually understood by end users.
     * 
     * @param <T>
     * @param userPreferences
     *            The user preferences to enrich.
     * @param defaultValue
     *            The default value.
     * @param allValue
     *            The ALL value.
     * @return The enriched user preferences.
     */
    @SuppressWarnings("unchecked")
    protected <T extends Metadata> List<Preference<T>> getEnrichedPreferences(
            List<Preference<T>> userPreferences, T defaultValue, T allValue) {
        List<Preference<T>> result = new ArrayList<Preference<T>>();

        // 0) List all undesired metadata
        List<T> undesired = null;
        for (Preference<T> pref : userPreferences) {
            if (pref.getQuality() == 0) {
                if (undesired == null) {
                    undesired = new ArrayList<T>();
                }
                undesired.add(pref.getMetadata());
            }
        }

        // 1) Add the user preferences
        result.addAll(userPreferences);

        // 2) Add the user parent preferences
        T parent;
        for (int i = 0; i < result.size(); i++) {
            Preference<T> userPref = result.get(i);
            parent = (T) userPref.getMetadata().getParent();

            // Add the parent, if it is not proscribed.
            if ((parent != null)) {
                if (canAdd(parent, undesired)) {
                    result.add(new Preference<T>(parent,
                            0.005f + (0.001f * userPref.getQuality())));
                }
            }
        }

        // 3) Add the default preference
        if (defaultValue != null && canAdd(defaultValue, undesired)) {
            Preference<T> defaultPref = new Preference<T>(defaultValue, 0.003f);
            result.add(defaultPref);
            T defaultParent = (T) defaultValue.getParent();

            if (defaultParent != null && canAdd(defaultParent, undesired)) {
                result.add(new Preference<T>(defaultParent, 0.002f));
            }
        }

        // 5) Add "all" preference
        for (int i = result.size() - 1; i >= 0; i--) {
            // Remove any existing preference
            if (result.get(i).getMetadata().equals(allValue)) {
                result.remove(i);
            }
        }

        result.add(new Preference<T>(allValue, 0.001f));

        // 6) Return the enriched preferences
        return result;
    }

    /**
     * Returns the enriched list of language preferences.
     * 
     * @return The enriched list of language preferences.
     */
    protected List<Preference<Language>> getLanguagePrefs() {
        return languagePrefs;
    }

    /**
     * Returns the enriched list of media type preferences.
     * 
     * @return The enriched list of media type preferences.
     */
    protected List<Preference<MediaType>> getMediaTypePrefs() {
        return mediaTypePrefs;
    }

    /**
     * Returns the metadata service.
     * 
     * @return The metadata service.
     */
    protected MetadataService getMetadataService() {
        return metadataService;
    }

    /**
     * Returns the best variant representation for a given resource according
     * the the client preferences.<br>
     * A default language is provided in case the variants don't match the
     * client preferences.
     * 
     * @param variants
     *            The list of variants to compare.
     * @return The preferred variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(List<? extends Variant> variants) {
        Variant result = null;

        if ((variants != null) && !variants.isEmpty()) {
            float bestScore = -1.0F;
            float current;

            // Compute the score of each variant
            for (Variant variant : variants) {
                current = scoreVariant(variant);

                if (current > bestScore) {
                    bestScore = current;
                    result = variant;
                }
            }
        }

        return result;
    }

    /**
     * Scores a character set relatively to enriched client preferences.
     * 
     * @param characterSet
     *            The character set to score.
     * @return The score.
     */
    public float scoreCharacterSet(CharacterSet characterSet) {
        return scoreMetadata(characterSet, getCharacterSetPrefs());
    }

    /**
     * Scores encodings relatively to enriched client preferences.
     * 
     * @param encodings
     *            The encodings to score.
     * @return The score.
     */
    public float scoreEncodings(List<Encoding> encodings) {
        return scoreMetadata(encodings, getEncodingPrefs());
    }

    /**
     * Scores languages relatively to enriched client preferences.
     * 
     * @param languages
     *            The languages to score.
     * @return The score.
     */
    public float scoreLanguages(List<Language> languages) {
        return scoreMetadata(languages, getLanguagePrefs());
    }

    /**
     * Scores a media type relatively to enriched client preferences.
     * 
     * @param mediaType
     *            The media type to score.
     * @return The score.
     */
    public float scoreMediaType(MediaType mediaType) {
        return scoreMetadata(mediaType, getMediaTypePrefs());
    }

    /**
     * Scores a list of metadata relatively to enriched client preferences.
     * 
     * @param metadataList
     *            The list of metadata to score.
     * @return The score.
     */
    private <T extends Metadata> float scoreMetadata(List<T> metadataList,
            List<Preference<T>> prefs) {
        float result = -1.0F;
        float current;

        if ((metadataList != null) && !metadataList.isEmpty()) {
            for (Preference<T> pref : prefs) {
                for (T metadata : metadataList) {
                    if (pref.getMetadata().includes(metadata)) {
                        current = pref.getQuality();
                    } else {
                        current = -1.0F;
                    }

                    if (current > result) {
                        result = current;
                    }
                }
            }
        } else {
            result = 0.0F;
        }

        return result;
    }

    /**
     * Scores a metadata relatively to enriched client preferences.
     * 
     * @param metadata
     *            The metadata to score.
     * @return The score.
     */
    private <T extends Metadata> float scoreMetadata(T metadata,
            List<Preference<T>> prefs) {
        float result = -1.0F;
        float current;

        if (metadata != null) {
            for (Preference<? extends Metadata> pref : prefs) {
                if (pref.getMetadata().includes(metadata)) {
                    current = pref.getQuality();
                } else {
                    current = -1.0F;
                }

                if (current > result) {
                    result = current;
                }
            }
        } else {
            result = 0.0F;
        }

        return result;
    }

    /**
     * Scores a variant relatively to enriched client preferences.
     * 
     * @param variant
     *            The variant to score.
     * @return The enriched client preferences.
     */
    public float scoreVariant(Variant variant) {
        float result = -1.0F;
        float languageScore = scoreLanguages(variant.getLanguages());

        if (languageScore != -1.0F) {
            float mediaTypeScore = scoreMediaType(variant.getMediaType());

            if (mediaTypeScore != -1.0F) {
                float characterSetScore = scoreCharacterSet(variant
                        .getCharacterSet());

                if (characterSetScore != -1.0F) {
                    float encodingScore = scoreEncodings(variant.getEncodings());

                    if (encodingScore != -1.0F) {
                        // Return the weighted average score
                        result = ((languageScore * 4.0F)
                                + (mediaTypeScore * 3.0F)
                                + (characterSetScore * 2.0F) + encodingScore) / 9.0F;
                    }
                }
            }
        }

        return result;
    }
}
