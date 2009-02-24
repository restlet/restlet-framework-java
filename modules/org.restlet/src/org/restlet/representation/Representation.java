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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.representation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Digest;
import org.restlet.data.MediaType;
import org.restlet.data.Range;
import org.restlet.data.Tag;
import org.restlet.engine.io.ByteUtils;
import org.restlet.engine.util.DateUtils;

/**
 * Current or intended state of a resource. The content of a representation can
 * be retrieved several times if there is a stable and accessible source, like a
 * local file or a string. When the representation is obtained via a temporary
 * source like a network socket, its content can only be retrieved once. The
 * "transient" and "available" properties are available to help you figure out
 * those aspects at runtime.<br>
 * <br>
 * For performance purpose, it is essential that a minimal overhead occurs upon
 * initialization. The main overhead must only occur during invocation of
 * content processing methods (write, getStream, getChannel and toString).<br>
 * <br>
 * "REST components perform actions on a resource by using a representation to
 * capture the current or intended state of that resource and transferring that
 * representation between components. A representation is a sequence of bytes,
 * plus representation metadata to describe those bytes. Other commonly used but
 * less precise names for a representation include: document, file, and HTTP
 * message entity, instance, or variant." Roy T. Fielding
 * 
 * @see <a href=
 *      "http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2"
 *      >Source dissertation</a>
 * @author Jerome Louvel
 */
public abstract class Representation extends Variant {
    /**
     * Empty representation with no content.
     */
    private static class EmptyRepresentation extends Representation {

        /**
         * Constructor.
         */
        public EmptyRepresentation() {
            setAvailable(false);
            setTransient(true);
            setSize(0);
        }

        @Override
        public ReadableByteChannel getChannel() throws IOException {
            return null;
        }

        @Override
        public Reader getReader() throws IOException {
            return null;
        }

        @Override
        public InputStream getStream() throws IOException {
            return null;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            // Do nothing
        }

        @Override
        public void write(WritableByteChannel writableChannel)
                throws IOException {
            // Do nothing
        }

        @Override
        public void write(Writer writer) throws IOException {
            // Do nothing
        }
    }

    /**
     * Indicates that the size of the representation can't be known in advance.
     */
    public static final long UNKNOWN_SIZE = -1L;

    /**
     * Returns a new empty representation with no content.
     * 
     * @return A new empty representation.
     */
    public static Representation createEmpty() {
        return new EmptyRepresentation();
    }

    /** Indicates if the representation's content is available. */
    private volatile boolean available;

    /**
     * The representation digest if any.
     */
    private volatile Digest digest;

    /** Indicates if the representation is downloadable. */
    private volatile boolean downloadable;

    /**
     * Indicates the suggested download file name for the representation's
     * content.
     */
    private volatile String downloadName;

    /** The expiration date. */
    private volatile Date expirationDate;

    /** Indicates if the representation's content is transient. */
    private volatile boolean isTransient;

    /** The modification date. */
    private volatile Date modificationDate;

    /**
     * The expected size. Dynamic representations can have any size, but
     * sometimes we can know in advance the expected size. If this expected size
     * is specified by the user, it has a higher priority than any size that can
     * be guessed by the representation (like a file size).
     */
    private volatile long size;

    /** The tag. */
    private volatile Tag tag;

    /**
     * Indicates where in the full content the partial content available should
     * be applied.
     */
    private volatile Range range;

    /**
     * Default constructor.
     */
    public Representation() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The media type.
     */
    public Representation(MediaType mediaType) {
        super(mediaType);
        this.available = true;
        this.digest = null;
        this.downloadable = false;
        this.downloadName = null;
        this.isTransient = false;
        this.range = null;
        this.size = UNKNOWN_SIZE;
        this.expirationDate = null;
        this.modificationDate = null;
        this.tag = null;
    }

    /**
     * Check that the digest computed from the representation content and the
     * digest declared by the representation are the same.<br>
     * Since this method relies on the {@link #computeDigest(String)} method,
     * and since this method reads entirely the representation's stream, user
     * must take care of the content of the representation in case the latter is
     * transient.
     * 
     * {@link #isTransient}
     * 
     * @return True if both digests are not null and equals.
     */
    public boolean checkDigest() {
        return (getDigest() != null && checkDigest(getDigest().getAlgorithm()));
    }

    /**
     * Check that the digest computed from the representation content and the
     * digest declared by the representation are the same. It also first checks
     * that the algorithms are the same.<br>
     * Since this method relies on the {@link #computeDigest(String)} method,
     * and since this method reads entirely the representation's stream, user
     * must take care of the content of the representation in case the latter is
     * transient.
     * 
     * {@link #isTransient}
     * 
     * @param algorithm
     *            The algorithm used to compute the digest to compare with. See
     *            constant values in {@link Digest}.
     * @return True if both digests are not null and equals.
     */
    public boolean checkDigest(String algorithm) {
        Digest digest = getDigest();
        if (digest != null) {
            if (algorithm.equals(digest.getAlgorithm())) {
                return digest.equals(computeDigest(algorithm));
            }
        }
        return false;
    }

    /**
     * Compute the representation digest according to the given algorithm.<br>
     * Since this method reads entirely the representation's stream, user must
     * take care of the content of the representation in case the latter is
     * transient.
     * 
     * {@link #isTransient}
     * 
     * @param algorithm
     *            The algorithm used to compute the digest. See constant values
     *            in {@link Digest}.
     * @return The computed digest or null if the digest cannot be computed.
     */
    public Digest computeDigest(String algorithm) {
        Digest result = null;

        if (isAvailable()) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithm);
                DigestInputStream dis = new DigestInputStream(getStream(), md);
                ByteUtils.exhaust(dis);
                result = new Digest(algorithm, md.digest());
            } catch (NoSuchAlgorithmException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to check the digest of the representation.", e);
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to check the digest of the representation.", e);
            }
        }

        return result;
    }

    /**
     * Exhauts the content of the representation by reading it and silently
     * discarding anything read.
     * 
     * @return The number of bytes consumed or -1 if unknown.
     */
    public long exhaust() throws IOException {
        long result = -1L;

        if (isAvailable()) {
            result = ByteUtils.exhaust(getStream());
        }

        return result;
    }

    /**
     * Returns the size effectively available. This returns the same value as
     * {@link #getSize()} if no range is defined, otherwise it returns the size
     * of the range using {@link Range#getSize()}.
     * 
     * @return The available size.
     */
    public long getAvailableSize() {
        if (getRange() == null) {
            return getSize();
        } else if (getRange().getSize() != Range.SIZE_MAX) {
            return getRange().getSize();
        } else if (getSize() != Representation.UNKNOWN_SIZE) {
            if (getRange().getIndex() != Range.INDEX_LAST) {
                return getSize() - getRange().getIndex();
            } else {
                return getSize();
            }
        }

        return Representation.UNKNOWN_SIZE;
    }

    /**
     * Returns a channel with the representation's content.<br>
     * If it is supported by a file, a read-only instance of FileChannel is
     * returned.<br>
     * This method is ensured to return a fresh channel for each invocation
     * unless it is a transient representation, in which case null is returned.
     * 
     * @return A channel with the representation's content.
     * @throws IOException
     */
    public abstract ReadableByteChannel getChannel() throws IOException;

    /**
     * Returns the representation digest if any.
     * 
     * @return The representation digest or null.
     */
    public Digest getDigest() {
        return this.digest;
    }

    /**
     * Returns the suggested download file name for this representation. This is
     * mainly used to suggest to the client a local name for a downloaded
     * representation. Note that in order for this property to be sent from
     * servers to clients, you also need to call
     * {@link #setDownloadable(boolean)} with a 'true' value.
     * 
     * @return The suggested file name for this representation.
     */
    public String getDownloadName() {
        return this.downloadName;
    }

    /**
     * Returns the future date when this representation expire. If this
     * information is not known, returns null.
     * 
     * @return The expiration date.
     */
    public Date getExpirationDate() {
        return this.expirationDate;
    }

    /**
     * Returns the last date when this representation was modified. If this
     * information is not known, returns null.
     * 
     * @return The modification date.
     */
    public Date getModificationDate() {
        return this.modificationDate;
    }

    /**
     * Returns the range where in the full content the partial content available
     * should be applied.
     * 
     * @return The content range or null if the full content is available.
     */
    public Range getRange() {
        return this.range;
    }

    /**
     * Returns a characters reader with the representation's content. This
     * method is ensured to return a fresh reader for each invocation unless it
     * is a transient representation, in which case null is returned. If the
     * representation has no character set defined, the system's default one
     * will be used.
     * 
     * @return A reader with the representation's content.
     * @throws IOException
     */
    public abstract Reader getReader() throws IOException;

    /**
     * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     */
    public long getSize() {
        return this.size;
    }

    /**
     * Returns a stream with the representation's content. This method is
     * ensured to return a fresh stream for each invocation unless it is a
     * transient representation, in which case null is returned.
     * 
     * @return A stream with the representation's content.
     * @throws IOException
     */
    public abstract InputStream getStream() throws IOException;

    /**
     * Returns the tag.
     * 
     * @return The tag.
     */
    public Tag getTag() {
        return this.tag;
    }

    /**
     * Converts the representation to a string value. Be careful when using this
     * method as the conversion of large content to a string fully stored in
     * memory can result in OutOfMemoryErrors being thrown.
     * 
     * @return The representation as a string value.
     */
    public String getText() throws IOException {
        String result = null;

        if (isAvailable()) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            write(baos);

            if (getCharacterSet() != null) {
                result = baos.toString(getCharacterSet().getName());
            } else {
                result = baos.toString();
            }
        }

        return result;
    }

    /**
     * Indicates if some fresh content is available, without having to actually
     * call one of the content manipulation method like getStream() that would
     * actually consume it. This is especially useful for transient
     * representation whose content can only be accessed once and also when the
     * size of the representation is not known in advance.
     * 
     * @return True if some fresh content is available.
     */
    public boolean isAvailable() {
        return (getSize() != 0) && this.available;
    }

    /**
     * Indicates if the representation is downloadable which means that it can
     * be obtained via a download dialog box.
     * 
     * @return True if the representation's content is downloadable.
     */
    public boolean isDownloadable() {
        return this.downloadable;
    }

    /**
     * Indicates if the representation's content is transient, which means that
     * it can be obtained only once. This is often the case with representations
     * transmitted via network sockets for example. In such case, if you need to
     * read the content several times, you need to cache it first, for example
     * into memory or into a file.
     * 
     * @return True if the representation's content is transient.
     */
    public boolean isTransient() {
        return this.isTransient;
    }

    /**
     * Releases the representation's content and all associated objects like
     * sockets, channels or files. If the representation is transient and hasn't
     * been read yet, all the remaining content will be discarded, any open
     * socket, channel, file or similar source of content will be immediately
     * closed. The representation is also no more available.
     */
    public void release() {
        this.available = false;
    }

    /**
     * Indicates if some fresh content is available.
     * 
     * @param available
     *            True if some fresh content is available.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Sets the representation digest.
     * 
     * @param digest
     *            The representation digest.
     */
    public void setDigest(Digest digest) {
        this.digest = digest;
    }

    /**
     * Indicates if the representation is downloadable which means that it can
     * be obtained via a download dialog box.
     * 
     * @param downloadable
     *            True if the representation's content is downloadable.
     */
    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }

    /**
     * Set the suggested download file name for this representation. Note that
     * in order for this property to be sent from servers to clients, you also
     * need to call {@link #setDownloadable(boolean)} with a 'true' value.
     * 
     * @param fileName
     *            The suggested file name.
     */
    public void setDownloadName(String fileName) {
        this.downloadName = fileName;
    }

    /**
     * Sets the future date when this representation expire. If this information
     * is not known, pass null.
     * 
     * @param expirationDate
     *            The expiration date.
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = DateUtils.unmodifiable(expirationDate);
    }

    /**
     * Sets the last date when this representation was modified. If this
     * information is not known, pass null.
     * 
     * @param modificationDate
     *            The modification date.
     */
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = DateUtils.unmodifiable(modificationDate);
    }

    /**
     * Sets the range where in the full content the partial content available
     * should be applied.
     * 
     * @param range
     *            The content range.
     */
    public void setRange(Range range) {
        this.range = range;
    }

    /**
     * Sets the expected size in bytes if known, -1 otherwise.
     * 
     * @param expectedSize
     *            The expected size in bytes if known, -1 otherwise.
     */
    public void setSize(long expectedSize) {
        this.size = expectedSize;
    }

    /**
     * Sets the tag.
     * 
     * @param tag
     *            The tag.
     */
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    /**
     * Indicates if the representation's content is transient.
     * 
     * @param isTransient
     *            True if the representation's content is transient.
     */
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    /**
     * Writes the representation to a byte stream. This method is ensured to
     * write the full content for each invocation unless it is a transient
     * representation, in which case an exception is thrown.
     * 
     * @param outputStream
     *            The output stream.
     * @throws IOException
     */
    public abstract void write(OutputStream outputStream) throws IOException;

    /**
     * Writes the representation to a byte channel. This method is ensured to
     * write the full content for each invocation unless it is a transient
     * representation, in which case an exception is thrown.
     * 
     * @param writableChannel
     *            A writable byte channel.
     * @throws IOException
     */
    public abstract void write(WritableByteChannel writableChannel)
            throws IOException;

    /**
     * Writes the representation to a characters writer. This method is ensured
     * to write the full content for each invocation unless it is a transient
     * representation, in which case an exception is thrown.
     * 
     * @param writer
     *            The characters writer.
     * @throws IOException
     */
    public abstract void write(Writer writer) throws IOException;

}
