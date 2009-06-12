/**
 * Copyright 2005-2009 Noelios Technologies.
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
import org.restlet.data.Parameter;
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
    private List<Preference<CharacterSet>> characterSetPrefs;

    /** The client preferences. */
    private ClientInfo clientInfo;

    /** The enriched list of encoding preferences. */
    private List<Preference<Encoding>> encodingPrefs;

    /** The enriched list of language preferences. */
    private List<Preference<Language>> languagePrefs;

    /** The enriched list of media type preferences. */
    private List<Preference<MediaType>> mediaTypePrefs;

    /** The metadata service. */
    private MetadataService metadataService;

    /**
     * Constructor.
     * 
     * @param clientInfo
     * @param metadataService
     */
    public Conneg(ClientInfo clientInfo, MetadataService metadataService) {
        this.clientInfo = clientInfo;
        this.metadataService = metadataService;

        // Get the enriched user preferences
        this.languagePrefs = getEnrichedPreferences(clientInfo
                .getAcceptedLanguages(), metadataService.getDefaultLanguage(),
                Language.ALL);
        this.mediaTypePrefs = getEnrichedPreferences(clientInfo
                .getAcceptedMediaTypes(),
                metadataService.getDefaultMediaType(), MediaType.ALL);
        this.characterSetPrefs = getEnrichedPreferences(clientInfo
                .getAcceptedCharacterSets(), metadataService
                .getDefaultCharacterSet(), CharacterSet.ALL);
        this.encodingPrefs = getEnrichedPreferences(clientInfo
                .getAcceptedEncodings(), metadataService.getDefaultEncoding(),
                Encoding.ALL);
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

        // 1) Add user preferences
        result.addAll(userPreferences);

        // 2) Add user parent preferences
        T parent;
        for (int i = 0; i < result.size(); i++) {
            parent = (T) result.get(i).getMetadata().getParent();

            if (parent != null) {
                result.add(new Preference<T>(parent, 0.005f + (0.001f * result
                        .get(i).getQuality())));
            }
        }

        // 3) Add default preference
        if (defaultValue != null) {
            Preference<T> defaultPref = new Preference<T>(defaultValue, 0.003f);
            result.add(defaultPref);
            T defaultParent = (T) defaultValue.getParent();

            if (defaultParent != null) {
                result.add(new Preference<T>(defaultParent, 0.002f));
            }
        }

        // 5) Add all preference
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
    public Variant getPreferredVariant(List<Variant> variants) {
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
     * Returns a matching score between 2 Languages
     * 
     * @param variantLanguage
     * @param preferenceLanguage
     * @return the positive matching score or -1 if the languages are not
     *         compatible
     */
    protected float score(Language variantLanguage, Language preferenceLanguage) {
        float score = 0.0f;
        boolean compatibleLang = true;

        // 1) Compare the main tag
        if (variantLanguage.getPrimaryTag().equalsIgnoreCase(
                preferenceLanguage.getPrimaryTag())) {
            score += 100;
        } else if (!preferenceLanguage.getPrimaryTag().equals("*")) {
            compatibleLang = false;
        } else if (!preferenceLanguage.getSubTags().isEmpty()) {
            // Only "*" is an acceptable language range
            compatibleLang = false;
        } else {
            // The valid "*" range has the lowest valid score
            score++;
        }

        if (compatibleLang) {
            // 2) Compare the sub tags
            if ((preferenceLanguage.getSubTags().isEmpty())
                    || (variantLanguage.getSubTags().isEmpty())) {
                if (variantLanguage.getSubTags().isEmpty()
                        && preferenceLanguage.getSubTags().isEmpty()) {
                    score += 10;
                } else {
                    // Don't change the score
                }
            } else {
                final int maxSize = Math.min(preferenceLanguage.getSubTags()
                        .size(), variantLanguage.getSubTags().size());
                for (int i = 0; (i < maxSize) && compatibleLang; i++) {
                    if (preferenceLanguage.getSubTags().get(i)
                            .equalsIgnoreCase(
                                    variantLanguage.getSubTags().get(i))) {
                        // Each subtag contribution to the score
                        // is getting less and less important
                        score += Math.pow(10, 1 - i);
                    } else {
                        // SubTags are different
                        compatibleLang = false;
                    }
                }
            }
        }

        return (compatibleLang ? score : -1.0f);
    }

    /**
     * Returns a matching score between 2 Media types
     * 
     * @param variantMediaType
     * @param preferenceMediaType
     * @return the positive matching score or -1 if the media types are not
     *         compatible
     */
    protected float score(MediaType variantMediaType,
            MediaType preferenceMediaType) {
        float score = 0.0F;
        boolean comptabibleMediaType = true;

        // 1) Compare the main types
        if (preferenceMediaType.getMainType().equals(
                variantMediaType.getMainType())) {
            score += 1.0F;
        } else {
            if (variantMediaType.getMainType().equals("*")) {
                // Ranges such as "*/html" are not supported
                // Only "*/*" is acceptable in this case
                comptabibleMediaType = (variantMediaType.getSubType()
                        .equals("*"));
            } else if (preferenceMediaType.getMainType().equals("*")) {
                // Ranges such as "*/html" are not supported
                // Only "*/*" is acceptable in this case
                comptabibleMediaType = (preferenceMediaType.getSubType()
                        .equals("*"));
            } else {
                comptabibleMediaType = false;
            }
        }

        if (comptabibleMediaType) {
            // 2) Compare the sub types
            if (variantMediaType.getSubType().equals(
                    preferenceMediaType.getSubType())) {
                score += 0.1F;
            } else {
                comptabibleMediaType = variantMediaType.getSubType()
                        .equals("*")
                        || preferenceMediaType.getSubType().equals("*");
            }

            if (comptabibleMediaType
                    && (variantMediaType.getParameters() != null)) {
                // 3) Compare the parameters
                // If current media type is compatible with the
                // current media range then the parameters need to
                // be checked too
                for (final Parameter currentParam : variantMediaType
                        .getParameters()) {
                    if (preferenceMediaType.getParameters().contains(
                            currentParam)) {
                        score++;
                    }
                }
            }

        }

        return (comptabibleMediaType ? score : -1.0F);
    }

    /**
     * Scores a character set relatively to enriched client preferences.
     * 
     * @param characterSet
     *            The character set to score.
     * @return The score.
     */
    public float scoreCharacterSet(CharacterSet characterSet) {
        float result = -1.0F;
        float current;

        if (characterSet != null) {
            for (Preference<CharacterSet> pref : getCharacterSetPrefs()) {
                if (characterSet.equals(pref.getMetadata())) {
                    current = 1.0F * pref.getQuality();
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
     * Scores encodings relatively to enriched client preferences.
     * 
     * @param encodings
     *            The encodings to score.
     * @return The score.
     */
    public float scoreEncodings(List<Encoding> encodings) {
        float result = 0.0F;
        return result;
    }

    /**
     * Scores languages relatively to enriched client preferences.
     * 
     * @param languages
     *            The languages to score.
     * @return The score.
     */
    public float scoreLanguages(List<Language> languages) {
        float result = 0.0F;
        return result;
    }

    /**
     * Scores an media type relatively to enriched client preferences.
     * 
     * @param mediaType
     *            The media type to score.
     * @return The score.
     */
    public float scoreMediaType(MediaType mediaType) {
        float result = -1.0F;
        float current;

        if (mediaType != null) {
            for (Preference<MediaType> pref : getMediaTypePrefs()) {
                current = score(mediaType, pref.getMetadata())
                        * pref.getQuality();

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
