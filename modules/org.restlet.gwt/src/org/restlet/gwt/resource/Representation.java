/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.resource;

import java.util.Date;

import org.restlet.gwt.data.MediaType;
import org.restlet.gwt.data.Tag;

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
 *      >Source dissertation< /a>
 * @author Jerome Louvel (contact@noelios.com)
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
        public String getText() {
            return null;
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
        this.isTransient = false;
        this.size = UNKNOWN_SIZE;
        this.expirationDate = null;
        this.modificationDate = null;
        this.tag = null;
    }

    /**
     * Returns the suggested download file name for this representation. This is
     * mainly used to suggest to the client a local name for a downloaded
     * representation.
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
     * Returns the size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     */
    public long getSize() {
        return this.size;
    }

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
    public abstract String getText();

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
     * Set the suggested download file name for this representation.
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
        this.expirationDate = expirationDate;
    }

    /**
     * Sets the last date when this representation was modified. If this
     * information is not known, pass null.
     * 
     * @param modificationDate
     *            The modification date.
     */
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
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

}
