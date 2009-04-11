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

package org.restlet.gae.engine.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.restlet.gae.data.ClientInfo;
import org.restlet.gae.data.Language;
import org.restlet.gae.data.MediaType;
import org.restlet.gae.data.Parameter;
import org.restlet.gae.data.Preference;
import org.restlet.gae.representation.Variant;

/**
 * Content negotiation utilities.
 * 
 * @author Jerome Louvel
 */
public class ConnegUtils {

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
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public static Variant getPreferredVariant(ClientInfo client,
            List<Variant> variants, Language defaultLanguage) {
        if (variants == null) {
            return null;
        }
        List<Language> variantLanguages = null;
        MediaType variantMediaType = null;

        boolean compatibleLanguage = false;
        boolean compatibleMediaType = false;

        Variant currentVariant = null;
        Variant bestVariant = null;

        Preference<Language> currentLanguagePref = null;
        Preference<Language> bestLanguagePref = null;
        Preference<MediaType> currentMediaTypePref = null;
        Preference<MediaType> bestMediaTypePref = null;

        float bestQuality = 0;
        float bestLanguageScore = 0;
        float bestMediaTypeScore = 0;

        // If no language preference is defined or even none matches, we
        // want to make sure that at least a variant can be returned.
        // Based on experience, it appears that browsers are often
        // misconfigured and don't expose all the languages actually
        // understood by end users.
        // Thus, a few other preferences are added to the user's ones:
        // - primary languages inferred from and sorted according to the
        // user's preferences with quality between 0.005 and 0.006
        // - default language (if any) with quality 0.003
        // - primary language of the default language (if available) with
        // quality 0.002
        // - all languages with quality 0.001
        List<Preference<Language>> languagePrefs = client
                .getAcceptedLanguages();
        final List<Preference<Language>> primaryLanguagePrefs = new ArrayList<Preference<Language>>();
        // A default language preference is defined with a better weight
        // than the "All languages" preference
        final Preference<Language> defaultLanguagePref = ((defaultLanguage == null) ? null
                : new Preference<Language>(defaultLanguage, 0.003f));
        final Preference<Language> allLanguagesPref = new Preference<Language>(
                Language.ALL, 0.001f);

        if (languagePrefs.isEmpty()) {
            // All languages accepted.
            languagePrefs.add(new Preference<Language>(Language.ALL));
        } else {
            // Get the primary language preferences that are not currently
            // accepted by the client
            final List<String> list = new ArrayList<String>();
            for (final Preference<Language> preference : languagePrefs) {
                final Language language = preference.getMetadata();
                if (!language.getSubTags().isEmpty()) {
                    if (!list.contains(language.getPrimaryTag())) {
                        list.add(language.getPrimaryTag());
                        primaryLanguagePrefs.add(new Preference<Language>(
                                new Language(language.getPrimaryTag()),
                                0.005f + (0.001f * preference.getQuality())));
                    }
                }
            }
            // If the default language is a "primary" language but is not
            // present in the list of all primary languages, add it.
            if ((defaultLanguage != null)
                    && !defaultLanguage.getSubTags().isEmpty()) {
                if (!list.contains(defaultLanguage.getPrimaryTag())) {
                    primaryLanguagePrefs.add(new Preference<Language>(
                            new Language(defaultLanguage.getPrimaryTag()),
                            0.002f));
                }
            }

        }

        // Client preferences are altered
        languagePrefs.addAll(primaryLanguagePrefs);
        if (defaultLanguagePref != null) {
            languagePrefs.add(defaultLanguagePref);
            // In this case, if the client adds the "all languages"
            // preference, the latter is removed, in order to support the
            // default preference defined by the server
            final List<Preference<Language>> list = new ArrayList<Preference<Language>>();
            for (final Preference<Language> preference : languagePrefs) {
                final Language language = preference.getMetadata();
                if (!language.equals(Language.ALL)) {
                    list.add(preference);
                }
            }
            languagePrefs = list;
        }
        languagePrefs.add(allLanguagesPref);

        // For each available variant, we will compute the negotiation score
        // which depends on both language and media type scores.
        for (final Iterator<Variant> iter1 = variants.iterator(); iter1
                .hasNext();) {
            currentVariant = iter1.next();
            variantLanguages = currentVariant.getLanguages();
            variantMediaType = currentVariant.getMediaType();

            // All languages of the current variant are scored.
            for (final Language variantLanguage : variantLanguages) {
                // For each language preference defined in the call
                // Calculate the score and remember the best scoring
                // preference
                for (final Iterator<Preference<Language>> iter2 = languagePrefs
                        .iterator(); (variantLanguage != null)
                        && iter2.hasNext();) {
                    currentLanguagePref = iter2.next();
                    final float currentScore = getScore(variantLanguage,
                            currentLanguagePref.getMetadata());
                    final boolean compatiblePref = (currentScore != -1.0f);
                    // 3) Do we have a better preference?
                    // currentScore *= currentPref.getQuality();
                    if (compatiblePref
                            && ((bestLanguagePref == null) || (currentScore > bestLanguageScore))) {
                        bestLanguagePref = currentLanguagePref;
                        bestLanguageScore = currentScore;
                    }
                }
            }

            // Are the preferences compatible with the current variant
            // language?
            compatibleLanguage = (variantLanguages.isEmpty())
                    || (bestLanguagePref != null);

            // If no media type preference is defined, assume that all media
            // types are acceptable
            final List<Preference<MediaType>> mediaTypePrefs = client
                    .getAcceptedMediaTypes();
            if (mediaTypePrefs.size() == 0) {
                mediaTypePrefs.add(new Preference<MediaType>(MediaType.ALL));
            }

            // For each media range preference defined in the call
            // Calculate the score and remember the best scoring preference
            for (final Iterator<Preference<MediaType>> iter2 = mediaTypePrefs
                    .iterator(); compatibleLanguage && iter2.hasNext();) {
                currentMediaTypePref = iter2.next();
                final float currentScore = getScore(variantMediaType,
                        currentMediaTypePref.getMetadata());
                final boolean compatiblePref = (currentScore != -1.0f);
                // 3) Do we have a better preference?
                // currentScore *= currentPref.getQuality();
                if (compatiblePref
                        && ((bestMediaTypePref == null) || (currentScore > bestMediaTypeScore))) {
                    bestMediaTypePref = currentMediaTypePref;
                    bestMediaTypeScore = currentScore;
                }

            }

            // Are the preferences compatible with the current media type?
            compatibleMediaType = (variantMediaType == null)
                    || (bestMediaTypePref != null);

            if (compatibleLanguage && compatibleMediaType) {
                // Do we have a compatible media type?
                float currentQuality = 0;
                if (bestLanguagePref != null) {
                    currentQuality += (bestLanguagePref.getQuality() * 10F);
                } else if (!variantLanguages.isEmpty()) {
                    currentQuality += 0.1F * 10F;
                }

                if (bestMediaTypePref != null) {
                    // So, let's conclude on the current variant, its
                    // quality
                    currentQuality += bestMediaTypePref.getQuality();
                }

                if (bestVariant == null) {
                    bestVariant = currentVariant;
                    bestQuality = currentQuality;
                } else if (currentQuality > bestQuality) {
                    bestVariant = currentVariant;
                    bestQuality = currentQuality;
                }
            }

            // Reset the preference variables
            bestLanguagePref = null;
            bestLanguageScore = 0;
            bestMediaTypePref = null;
            bestMediaTypeScore = 0;
        }

        return bestVariant;

    }

    /**
     * Returns a matching score between 2 Languages
     * 
     * @param variantLanguage
     * @param preferenceLanguage
     * @return the positive matching score or -1 if the languages are not
     *         compatible
     */
    public static float getScore(Language variantLanguage,
            Language preferenceLanguage) {
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
    public static float getScore(MediaType variantMediaType,
            MediaType preferenceMediaType) {
        float score = 0.0f;
        boolean comptabibleMediaType = true;

        // 1) Compare the main types
        if (preferenceMediaType.getMainType().equals(
                variantMediaType.getMainType())) {
            score += 1000;
        } else if (!preferenceMediaType.getMainType().equals("*")) {
            comptabibleMediaType = false;
        } else if (!preferenceMediaType.getSubType().equals("*")) {
            // Ranges such as "*/html" are not supported
            // Only "*/*" is acceptable in this case
            comptabibleMediaType = false;
        }

        if (comptabibleMediaType) {
            // 2) Compare the sub types
            if (variantMediaType.getSubType().equals(
                    preferenceMediaType.getSubType())) {
                score += 100;
            } else if (!preferenceMediaType.getSubType().equals("*")) {
                // Sub-type are different
                comptabibleMediaType = false;
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

        return (comptabibleMediaType ? score : -1.0f);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ConnegUtils() {
    }
}
