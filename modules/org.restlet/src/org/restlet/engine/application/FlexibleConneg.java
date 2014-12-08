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

import java.util.ArrayList;
import java.util.List;

import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.service.MetadataService;

/**
 * Content negotiation algorithm that flexibly interprets the content
 * negotiation preferences to try to always return a variant even if the client
 * preferences don't exactly match.
 * 
 * @author Jerome Louvel
 */
public class FlexibleConneg extends StrictConneg {

    /** The enriched list of character set preferences. */
    private volatile List<Preference<CharacterSet>> characterSetPrefs;

    /** The enriched list of encoding preferences. */
    private volatile List<Preference<Encoding>> encodingPrefs;

    /** The enriched list of language preferences. */
    private volatile List<Preference<Language>> languagePrefs;

    /** The enriched list of media type preferences. */
    private volatile List<Preference<MediaType>> mediaTypePrefs;

    /**
     * Constructor.
     * 
     * @param request
     *            The request including client preferences.
     * @param metadataService
     *            The metadata service used to get default metadata values.
     */
    public FlexibleConneg(Request request, MetadataService metadataService) {
        super(request, metadataService);
        ClientInfo clientInfo = request.getClientInfo();

        if (clientInfo != null) {
            // Get the enriched user preferences
            this.languagePrefs = getEnrichedPreferences(
                    clientInfo.getAcceptedLanguages(),
                    (metadataService == null) ? null : metadataService
                            .getDefaultLanguage(), Language.ALL);
            this.mediaTypePrefs = getEnrichedPreferences(
                    clientInfo.getAcceptedMediaTypes(),
                    (metadataService == null) ? null : metadataService
                            .getDefaultMediaType(), MediaType.ALL);
            this.characterSetPrefs = getEnrichedPreferences(
                    clientInfo.getAcceptedCharacterSets(),
                    (metadataService == null) ? null : metadataService
                            .getDefaultCharacterSet(), CharacterSet.ALL);
            this.encodingPrefs = getEnrichedPreferences(
                    clientInfo.getAcceptedEncodings(),
                    (metadataService == null) ? null : metadataService
                            .getDefaultEncoding(), Encoding.ALL);
        }
    }

    /**
     * Returns true if the metadata can be added.
     * 
     * @param <T>
     * @param metadata
     *            The metadata to add.
     * @param undesired
     *            The list of prohibited metadata.
     * @return True if the metadata can be added.
     */
    protected <T extends Metadata> boolean canAdd(T metadata, List<T> undesired) {
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

}
