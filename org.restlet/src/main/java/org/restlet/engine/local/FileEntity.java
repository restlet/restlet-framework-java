/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.MetadataService;

/** Local entity based on a regular {@link File}. */
public class FileEntity extends Entity {

    /** The underlying regular file. */
    private final File file;

    /**
     * Constructor.
     *
     * @param file The underlying file.
     * @param metadataService The metadata service to use.
     */
    public FileEntity(File file, MetadataService metadataService) {
        super(metadataService);
        this.file = file;
    }

    @Override
    public boolean exists() {
        return getFile().exists();
    }

    @Override
    public List<Entity> getChildren() {
        List<Entity> result = null;

        if (isDirectory()) {
            result = new ArrayList<>();

            for (File f : Objects.requireNonNull(getFile().listFiles())) {
                result.add(new FileEntity(f, getMetadataService()));
            }
        }

        return result;
    }

    /**
     * Returns the underlying regular file.
     *
     * @return The underlying regular file.
     */
    public File getFile() {
        return file;
    }

    @Override
    public String getName() {
        return getFile().getName();
    }

    @Override
    public Entity getParent() {
        File parentFile = getFile().getParentFile();
        return (parentFile == null) ? null : new FileEntity(parentFile, getMetadataService());
    }

    @Override
    public Representation getRepresentation(MediaType defaultMediaType, int timeToLive) {
        return new FileRepresentation(getFile(), defaultMediaType, timeToLive);
    }

    @Override
    public boolean isDirectory() {
        return getFile().isDirectory();
    }

    @Override
    public boolean isNormal() {
        return getFile().isFile();
    }
}
