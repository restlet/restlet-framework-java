package com.noelios.restlet.local;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.service.MetadataService;

public abstract class LocalFile {

    /**
     * Return the base name that is to say the longest part of a given name
     * without known extensions (beginning from the left).
     * 
     * @param name
     *            The given name.
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The base name of this local file.
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
     * Returns the list of known extensions taken from a given name
     * 
     * @param name
     *            the given name.
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The list of known extensions taken from the name of the local
     *         file.
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
     * Indicates if the local file exists.
     * 
     * @return True if the local file exists, false otherwise.
     */
    public abstract boolean exists();

    /**
     * Return the base name of this local file that is to say the longest part
     * of the name without known extensions (beginning from the left).
     * 
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The base name of this local file.
     */
    public String getBaseName(MetadataService metadataService) {
        return LocalFile.getBaseName(this.getName(), metadataService);
    }

    /**
     * Returns the list of known extensions taken from the name of the local
     * file.
     * 
     * @param metadataService
     *            Service that holds the known extensions.
     * @return The list of known extensions taken from the name of the local
     *         file.
     */
    public Collection<String> getExtensions(MetadataService metadataService) {
        return LocalFile.getExtensions(this.getName(), metadataService);
    }

    /**
     * Returns the list of files contained by this local file if it is a
     * directory, null otherwise.
     * 
     * @return The list of files contained by this local file if it is a
     *         directory, null otherwise.
     */
    public abstract Collection<LocalFile> getFiles();

    /**
     * Returns the name of the local file.
     * 
     * @return The name of the local file.
     */
    public abstract String getName();

    /**
     * Returns the parent directory (if any) of the local file.
     * 
     * @return The parent of the local file if it exists, null otherwise.
     */
    public abstract LocalFile getParent();

    /**
     * Returns a representation of this local file.
     * 
     * @param defaultMediaType
     *            The default media type
     * @param timeToLive
     *            the time to live of this representation
     * @return A representation of this local file.
     */
    public abstract Representation getRepresentation(
            MediaType defaultMediaType, int timeToLive);

    /**
     * Indicates if the local file is a directory.
     * 
     * @return True if the local file a directory, false otherwise.
     */
    public abstract boolean isDirectory();

    /**
     * Indicates if the local file is a file.
     * 
     * @return True if the local file a file, false otherwise.
     */
    public abstract boolean isFile();
}
