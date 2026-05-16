/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.local;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.StreamRepresentation;

/**
 * An entry in a Zip/JAR file.
 *
 * <p>It is very important {@link #release()} is called to close the underlying Zip file.
 *
 * @author Remi Dewitte remi@gide.net
 */
public class ZipEntryRepresentation extends StreamRepresentation {

    /** The Zip entry. */
    protected final ZipEntry entry;

    /** The Zip file. */
    protected final ZipFile zipFile;

    /**
     * Constructor.
     *
     * @param mediaType The entry media type.
     * @param zipFile The parent Zip archive file.
     * @param entry The Zip entry.
     * @param timeToLive The time to live before it expires (in seconds).
     */
    public ZipEntryRepresentation(
            MediaType mediaType, ZipFile zipFile, ZipEntry entry, int timeToLive) {
        super(mediaType);
        this.zipFile = zipFile;
        this.entry = entry;
        Disposition disposition = new Disposition();
        disposition.setFilename(entry.getName());
        this.setDisposition(disposition);
        setSize(entry.getSize());
        setModificationDate(new Date(entry.getTime()));

        if (timeToLive == 0) {
            setExpirationDate(null);
        } else if (timeToLive > 0) {
            setExpirationDate(new Date(System.currentTimeMillis() + (1000L * timeToLive)));
        }
    }

    @Override
    public InputStream getStream() throws IOException {
        return new FilterInputStream(zipFile.getInputStream(entry)) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    release();
                }
            }
        };
    }

    @Override
    public void release() {
        try {
            zipFile.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try (InputStream inputStream = getStream()) {
            IoUtils.copy(inputStream, outputStream);
        }
    }
}
