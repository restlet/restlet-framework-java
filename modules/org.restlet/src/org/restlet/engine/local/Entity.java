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

package org.restlet.engine.local;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

/**
 * Represents a local entity, for example a regular file or a directory.
 * 
 * @author Thierry Boileau
 * @author Jerome Louvel
 */
public abstract class Entity {

    /**
     * Return the base name that is to say the longest part of a given name
     * without known extensions (beginning from the left).
     * 
     * @param name
     *            The given name.
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The base name of this entity.
     */
    public static String getBaseName(String name,
            MetadataService metadataService) {
        final String[] result = name.split("\\.");
        final StringBuilder baseName = new StringBuilder().append(result[0]);
        boolean extensionFound = false;
        for (int i = 1; (i < result.length) && !extensionFound; i++) {
            extensionFound = metadataService.getMetadata(result[i]) != null;
            if (!extensionFound) {
                baseName.append(".").append(result[i]);
            }
        }
        return baseName.toString();
    }

    /**
     * Returns the list of known extensions taken from a given entity name.
     * 
     * @param name
     *            the given name.
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The list of known extensions taken from the entity name.
     */
    public static Collection<String> getExtensions(String name,
            MetadataService metadataService) {
        final Set<String> result = new TreeSet<String>();
        final String[] tokens = name.split("\\.");
        boolean extensionFound = false;

        int i;
        for (i = 1; (i < tokens.length) && !extensionFound; i++) {
            extensionFound = metadataService.getMetadata(tokens[i]) != null;
        }
        if (extensionFound) {
            for (--i; (i < tokens.length); i++) {
                result.add(tokens[i]);
            }
        }

        return result;
    }

    /**
     * Returns the list of known extensions taken from a given variant.
     * 
     * @param variant
     *            the given variant.
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The list of known extensions taken from the variant.
     */
    public static Collection<String> getExtensions(Variant variant,
            MetadataService metadataService) {
        final Set<String> result = new TreeSet<String>();

        String extension = metadataService.getExtension(variant
                .getCharacterSet());
        if (extension != null) {
            result.add(extension);
        }
        extension = metadataService.getExtension(variant.getMediaType());
        if (extension != null) {
            result.add(extension);
        }
        for (Language language : variant.getLanguages()) {
            extension = metadataService.getExtension(language);
            if (extension != null) {
                result.add(extension);
            }
        }
        for (Encoding encoding : variant.getEncodings()) {
            extension = metadataService.getExtension(encoding);
            if (extension != null) {
                result.add(extension);
            }
        }

        return result;
    }

    /**
     * Updates some variant metadata based on a given entry name with
     * extensions.
     * 
     * @param entryName
     *            The entry name with extensions.
     * @param variant
     *            The variant to update.
     * @param applyDefault
     *            Indicate if default metadata must be applied.
     * @param metadataService
     *            The parent metadata service.
     */
    public static void updateMetadata(String entryName, Variant variant,
            boolean applyDefault, MetadataService metadataService) {
        if (variant != null) {
            String[] tokens = entryName.split("\\.");
            Metadata current;

            // We found a potential variant
            for (int j = 1; j < tokens.length; j++) {
                current = metadataService.getMetadata(tokens[j]);

                if (current != null) {
                    // Metadata extension detected
                    if (current instanceof MediaType) {
                        variant.setMediaType((MediaType) current);
                    } else if (current instanceof CharacterSet) {
                        variant.setCharacterSet((CharacterSet) current);
                    } else if (current instanceof Encoding) {
                        // Do we need to add this metadata?
                        boolean found = false;
                        for (int i = 0; !found
                                && i < variant.getEncodings().size(); i++) {
                            found = current.includes(variant.getEncodings()
                                    .get(i));
                        }
                        if (!found) {
                            variant.getEncodings().add((Encoding) current);
                        }
                    } else if (current instanceof Language) {
                        // Do we need to add this metadata?
                        boolean found = false;
                        for (int i = 0; !found
                                && i < variant.getLanguages().size(); i++) {
                            found = current.includes(variant.getLanguages()
                                    .get(i));
                        }
                        if (!found) {
                            variant.getLanguages().add((Language) current);
                        }
                    }
                }

                final int dashIndex = tokens[j].indexOf('-');
                if (dashIndex != -1) {
                    // We found a language extension with a region area
                    // specified.
                    // Try to find a language matching the primary part of the
                    // extension.
                    final String primaryPart = tokens[j]
                            .substring(0, dashIndex);
                    current = metadataService.getMetadata(primaryPart);
                    if (current instanceof Language) {
                        variant.getLanguages().add((Language) current);
                    }
                }
            }

            if (applyDefault) {
                // If no language is defined, take the default one
                if (variant.getLanguages().isEmpty()) {
                    final Language defaultLanguage = metadataService
                            .getDefaultLanguage();

                    if ((defaultLanguage != null)
                            && !defaultLanguage.equals(Language.ALL)) {
                        variant.getLanguages().add(defaultLanguage);
                    }
                }

                // If no media type is defined, take the default one
                if (variant.getMediaType() == null) {
                    final MediaType defaultMediaType = metadataService
                            .getDefaultMediaType();

                    if ((defaultMediaType != null)
                            && !defaultMediaType.equals(MediaType.ALL)) {
                        variant.setMediaType(defaultMediaType);
                    }
                }

                // If no encoding is defined, take the default one
                if (variant.getEncodings().isEmpty()) {
                    final Encoding defaultEncoding = metadataService
                            .getDefaultEncoding();

                    if ((defaultEncoding != null)
                            && !defaultEncoding.equals(Encoding.ALL)
                            && !defaultEncoding.equals(Encoding.IDENTITY)) {
                        variant.getEncodings().add(defaultEncoding);
                    }
                }

                // If no character set is defined, take the default one
                if (variant.getCharacterSet() == null) {
                    final CharacterSet defaultCharacterSet = metadataService
                            .getDefaultCharacterSet();

                    if ((defaultCharacterSet != null)
                            && !defaultCharacterSet.equals(CharacterSet.ALL)) {
                        variant.setCharacterSet(defaultCharacterSet);
                    }
                }
            }
        }
    }

    /** The metadata service to use. */
    private volatile MetadataService metadataService;

    /**
     * Constructor.
     * 
     * @param metadataService
     *            The metadata service to use.
     */
    public Entity(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Indicates if the entity does exist.
     * 
     * @return True if the entity does exists.
     */
    public abstract boolean exists();

    /**
     * Return the base name of this entity that is to say the longest part of
     * the name without known extensions (beginning from the left).
     * 
     * @return The base name of this entity.
     */
    public String getBaseName() {
        return getBaseName(getName(), getMetadataService());
    }

    /**
     * Returns the list of contained entities if the current entity is a
     * directory, null otherwise.
     * 
     * @return The list of contained entities.
     */
    public abstract List<Entity> getChildren();

    /**
     * Returns the list of known extensions.
     * 
     * @return The list of known extensions taken from the entity name.
     */
    public Collection<String> getExtensions() {
        return getExtensions(getName(), getMetadataService());
    }

    /**
     * Returns the metadata service to use.
     * 
     * @return The metadata service to use.
     */
    public MetadataService getMetadataService() {
        return metadataService;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public abstract String getName();

    /**
     * Returns the parent directory (if any).
     * 
     * @return The parent directory, null otherwise.
     */
    public abstract Entity getParent();

    /**
     * Returns a representation of this local entity.
     * 
     * @return A representation of this entity.
     */
    public abstract Representation getRepresentation(
            MediaType defaultMediaType, int timeToLive);

    /**
     * Returns a variant corresponding to the extensions of this entity.
     * 
     * @return A variant corresponding to the extensions of this entity.
     */
    public Variant getVariant() {
        Variant result = new Variant();
        updateMetadata(getName(), result, true, getMetadataService());
        return result;
    }

    /**
     * Indicates if the entity is a directory.
     * 
     * @return True if the entity is a directory.
     */
    public abstract boolean isDirectory();

    /**
     * Indicates if the entity is a normal entity, especially if it is not a
     * directory.
     * 
     * @return True if the entity is a normal entity.
     * @see File#isFile()
     * @see File#isDirectory()
     */
    public abstract boolean isNormal();

}
