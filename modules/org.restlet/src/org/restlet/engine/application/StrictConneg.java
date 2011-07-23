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

package org.restlet.engine.application;

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
 * Content negotiation algorithm that strictly interprets the content
 * negotiation preferences.
 * 
 * @author Jerome Louvel
 */
public class StrictConneg extends Conneg {

    /**
     * Constructor.
     * 
     * @param clientInfo
     *            The client info containing preferences.
     * @param metadataService
     *            The metadata service used to get default metadata values.
     */
    public StrictConneg(ClientInfo clientInfo, MetadataService metadataService) {
        super(clientInfo, metadataService);
    }

    /**
     * Returns the enriched list of character set preferences.
     * 
     * @return The enriched list of character set preferences.
     */
    protected List<Preference<CharacterSet>> getCharacterSetPrefs() {
        return getClientInfo().getAcceptedCharacterSets();
    }

    /**
     * Returns the enriched list of encoding preferences.
     * 
     * @return The enriched list of encoding preferences.
     */
    protected List<Preference<Encoding>> getEncodingPrefs() {
        return getClientInfo().getAcceptedEncodings();
    }

    /**
     * Returns the enriched list of language preferences.
     * 
     * @return The enriched list of language preferences.
     */
    protected List<Preference<Language>> getLanguagePrefs() {
        return getClientInfo().getAcceptedLanguages();
    }

    /**
     * Returns the enriched list of media type preferences.
     * 
     * @return The enriched list of media type preferences.
     */
    protected List<Preference<MediaType>> getMediaTypePrefs() {
        return getClientInfo().getAcceptedMediaTypes();
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
    protected <T extends Metadata> float scoreMetadata(List<T> metadataList,
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
    protected <T extends Metadata> float scoreMetadata(T metadata,
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
     * Scores a variant relatively to enriched client preferences. The language
     * has a weight of 4, the media type 3, the character set 2 and the encoding
     * 1.
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
                                + (characterSetScore * 2.0F) + (encodingScore * 1.0F)) / 10.0F;
                    }
                }
            }
        }

        return result;
    }

}
