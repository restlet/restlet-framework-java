/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.engine.local;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/**
 * Represents a local entity, for example a regular file or a directory.
 * 
 * @author Thierry Boileau
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
     * Indicates if the entity does exist.
     * 
     * @return True if the entity does exists.
     */
    public abstract boolean exists();

    /**
     * Return the base name of this entity that is to say the longest part of
     * the name without known extensions (beginning from the left).
     * 
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The base name of this entity.
     */
    public String getBaseName(MetadataService metadataService) {
        return Entity.getBaseName(this.getName(), metadataService);
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
     * @param metadataService
     *            Service that maps extension names to metadata.
     * @return The list of known extensions taken from the entity name.
     */
    public Collection<String> getExtensions(MetadataService metadataService) {
        return Entity.getExtensions(this.getName(), metadataService);
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
     * @param defaultMediaType
     *            The default media type
     * @param timeToLive
     *            the time to live of this representation
     * @return A representation of this entity.
     */
    public abstract Representation getRepresentation(
            MediaType defaultMediaType, int timeToLive);

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
