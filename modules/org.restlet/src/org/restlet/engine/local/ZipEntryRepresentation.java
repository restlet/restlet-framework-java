/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.restlet.data.MediaType;
import org.restlet.engine.io.ByteUtils;
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

    /** The Zip file. */
    protected final ZipFile zipFile;

    /** The Zip entry. */
    protected final ZipEntry entry;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The entry media type.
     * @param zipFile
     *            The parent Zip archive file.
     * @param entry
     *            The Zip entry.
     */
    public ZipEntryRepresentation(MediaType mediaType, ZipFile zipFile,
            ZipEntry entry) {
        super(mediaType);
        this.zipFile = zipFile;
        this.entry = entry;
        setDownloadName(entry.getName());
        setSize(entry.getSize());
        setModificationDate(new Date(entry.getTime()));
    }

    @Override
    public void release() {
        try {
            zipFile.close();
        } catch (IOException e) {
        }
    }

    @Override
    public InputStream getStream() throws IOException {
        return zipFile.getInputStream(entry);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        ByteUtils.write(getStream(), outputStream);
    }

}
