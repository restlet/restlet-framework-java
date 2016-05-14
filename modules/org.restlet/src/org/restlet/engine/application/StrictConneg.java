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

package org.restlet.engine.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.engine.resource.VariantInfo;
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
     * @param request
     *            The request including client preferences.
     * @param metadataService
     *            The metadata service used to get default metadata values.
     */
    public StrictConneg(Request request, MetadataService metadataService) {
        super(request, metadataService);
    }

    /**
     * Returns the enriched list of character set preferences.
     * 
     * @return The enriched list of character set preferences.
     */
    protected List<Preference<CharacterSet>> getCharacterSetPrefs() {
        return getRequest().getClientInfo().getAcceptedCharacterSets();
    }

    /**
     * Returns the enriched list of encoding preferences.
     * 
     * @return The enriched list of encoding preferences.
     */
    protected List<Preference<Encoding>> getEncodingPrefs() {
        return getRequest().getClientInfo().getAcceptedEncodings();
    }

    /**
     * Returns the enriched list of language preferences.
     * 
     * @return The enriched list of language preferences.
     */
    protected List<Preference<Language>> getLanguagePrefs() {
        return getRequest().getClientInfo().getAcceptedLanguages();
    }

    /**
     * Returns the enriched list of media type preferences.
     * 
     * @return The enriched list of media type preferences.
     */
    protected List<Preference<MediaType>> getMediaTypePrefs() {
        return getRequest().getClientInfo().getAcceptedMediaTypes();
    }

    /**
     * Scores the annotation descriptor. By default, it assess the quality of
     * the query parameters with the URI query constraint defined in the
     * annotation value if any.
     * 
     * @param annotation
     *            The annotation descriptor to score.
     * @return The annotation descriptor score.
     */
    protected float scoreAnnotation(MethodAnnotationInfo annotation) {
        if (annotation == null) {
            return 0.0F;
        }
        
        float score = doScoreAnnotation(annotation);
        
        if (Context.getCurrentLogger().isLoggable(Level.FINE)) {
            Context.getCurrentLogger()
                    .fine("Score of annotation \"" + annotation + "\"= " + score);
        }
        return score;
    }

    private float doScoreAnnotation(MethodAnnotationInfo annotation) {
        if (annotation.getQuery() == null) {
            if ((getRequest().getResourceRef() == null)
                    || (getRequest().getResourceRef().getQuery() == null)) {
                // No query filter, but no query provided, average fit
                return 0.5F;
            }

            // No query filter, but a query provided, lower fit
            return 0.25F;
        }
        
        if ((getRequest().getResourceRef() == null)
                || (getRequest().getResourceRef().getQuery() == null)) {
            // Query constraint defined, but no query provided, no fit
            return -1.0F;
        }
        
        // Query constraint defined and a query provided, see if fit
        Form constraintParams = new Form(annotation.getQuery());
        Form actualParams = getRequest().getResourceRef().getQueryAsForm();
        Set<Parameter> matchedParams = new HashSet<Parameter>();
        Parameter constraintParam;
        Parameter actualParam;

        boolean allConstraintsMatched = true;
        boolean constraintMatched = false;

        // Verify that each query constraint has been matched
        for (int i = 0; allConstraintsMatched && (i < constraintParams.size()); i++) {
            constraintParam = constraintParams.get(i);
            constraintMatched = false;

            for (int j = 0; !constraintMatched && (j < actualParams.size()); j++) {
                actualParam = actualParams.get(j);

                if (constraintParam.getName().equals(actualParam.getName())) {
                    // Potential match found based on name
                    if ((constraintParam.getValue() == null)
                            || constraintParam.getValue().equals(actualParam.getValue())) {
                        // Actual match found!
                        constraintMatched = true;
                        matchedParams.add(actualParam);
                    }
                }
            }

            allConstraintsMatched = allConstraintsMatched && constraintMatched;
        }

        if (allConstraintsMatched) {
            // Test if all actual query parameters matched a constraint, so increase score
            if (actualParams.size() == matchedParams.size()) {
                // All filter parameters matched, no additional parameter found
                return 1.0F;
            }
            // All filter parameters matched, but additional parameters found
            return 0.75F;
        }

        return -1.0F;
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
        float result = -1.0F;
        float current;

        if (mediaType != null) {
            for (Preference<MediaType> pref : getMediaTypePrefs()) {
                if (pref.getMetadata().includes(mediaType, false)) {
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
                        if (variant instanceof VariantInfo) {
                            float annotationScore = scoreAnnotation(((VariantInfo) variant)
                                    .getAnnotationInfo());

                            // Return the weighted average score
                            result = ((languageScore * 4.0F)
                                    + (mediaTypeScore * 3.0F)
                                    + (characterSetScore * 2.0F)
                                    + (encodingScore * 1.0F) + (annotationScore * 2.0F)) / 12.0F;
                            // Take into account the affinity with the input
                            // entity
                            result = result
                                    * ((VariantInfo) variant).getInputScore();
                        } else {
                            // Return the weighted average score
                            result = ((languageScore * 4.0F)
                                    + (mediaTypeScore * 3.0F)
                                    + (characterSetScore * 2.0F) + (encodingScore * 1.0F)) / 10.0F;
                        }
                    }
                }
            }
        }

        if (Context.getCurrentLogger().isLoggable(Level.FINE)) {
            Context.getCurrentLogger().fine(
                    "Total score of variant \"" + variant + "\"= " + result);
        }

        return result;
    }
}
