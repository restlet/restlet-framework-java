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
 * It is very important {@link #release()} is called to close the underlying Zip
 * file.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class ZipEntryRepresentation extends StreamRepresentation {

    /** The Zip entry. */
    protected final ZipEntry entry;

    /** The Zip file. */
    protected final ZipFile zipFile;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The entry media type.
     * @param zipFile
     *            The parent Zip archive file.
     * @param entry
     *            The Zip entry.
     * @deprecated Use
     *             {@link #ZipEntryRepresentation(MediaType, ZipFile, ZipEntry, int)}
     *             instead.
     */
    @Deprecated
    public ZipEntryRepresentation(MediaType mediaType, ZipFile zipFile,
            ZipEntry entry) {
        this(mediaType, zipFile, entry, -1);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The entry media type.
     * @param zipFile
     *            The parent Zip archive file.
     * @param entry
     *            The Zip entry.
     * @param timeToLive
     *            The time to live before it expires (in seconds).
     */
    public ZipEntryRepresentation(MediaType mediaType, ZipFile zipFile,
            ZipEntry entry, int timeToLive) {
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
            setExpirationDate(new Date(System.currentTimeMillis()
                    + (1000L * timeToLive)));
        }
    }

    @Override
    public InputStream getStream() throws IOException {
        return zipFile.getInputStream(entry);
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
        IoUtils.copy(getStream(), outputStream);
    }

}
