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

package org.restlet.service;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.Encoder;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

/**
 * Application service automatically encoding or compressing request entities.
 * 
 * @author Jerome Louvel
 */
public class EncoderService extends Service {

    /** Indicates if the encoding should always occur, regardless of the size. */
    public static final int ANY_SIZE = -1;

    /** Indicates if the default minimum size for encoding to occur. */
    public static final int DEFAULT_MINIMUM_SIZE = 1000;

    /**
     * Returns the list of default encoded media types. This can be overridden
     * by subclasses. By default, all media types are encoded (except those
     * explicitly ignored).
     * 
     * @return The list of default encoded media types.
     */
    public static List<MediaType> getDefaultAcceptedMediaTypes() {
        final List<MediaType> result = Arrays.<MediaType> asList(MediaType.ALL);
        return result;
    }

    /**
     * Returns the list of default ignored media types. This can be overridden
     * by subclasses. By default, all archive, audio, image and video media
     * types are ignored.
     * 
     * @return The list of default ignored media types.
     */
    public static List<MediaType> getDefaultIgnoredMediaTypes() {
        final List<MediaType> result = Arrays.<MediaType> asList(
                MediaType.APPLICATION_CAB, MediaType.APPLICATION_GNU_ZIP,
                MediaType.APPLICATION_ZIP, MediaType.APPLICATION_GNU_TAR,
                MediaType.APPLICATION_JAVA_ARCHIVE,
                MediaType.APPLICATION_STUFFIT, MediaType.APPLICATION_TAR,
                MediaType.AUDIO_ALL, MediaType.IMAGE_ALL, MediaType.VIDEO_ALL);
        return result;
    }

    /**
     * The media types that should be encoded.
     */
    private final List<MediaType> acceptedMediaTypes;

    /**
     * The media types that should be ignored.
     */
    private final List<MediaType> ignoredMediaTypes;

    /**
     * The minimal size necessary for encoding.
     */
    private volatile long mininumSize;

    /**
     * Constructor.
     */
    public EncoderService() {
        this(true);
    }

    /**
     * Constructor. The default minimum size
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public EncoderService(boolean enabled) {
        super(enabled);
        this.mininumSize = DEFAULT_MINIMUM_SIZE;
        this.acceptedMediaTypes = new CopyOnWriteArrayList<MediaType>(
                getDefaultAcceptedMediaTypes());
        this.ignoredMediaTypes = new CopyOnWriteArrayList<MediaType>(
                getDefaultIgnoredMediaTypes());
    }

    /**
     * Indicates if a representation can be encoded.
     * 
     * @param representation
     *            The representation to test.
     * @return True if the call can be encoded.
     */
    public boolean canEncode(Representation representation) {
        // Test the existence of the representation and that no existing
        // encoding applies
        boolean result = false;

        if (representation != null) {
            boolean identity = true;

            for (Iterator<Encoding> iter = representation.getEncodings()
                    .iterator(); identity && iter.hasNext();) {
                identity = (iter.next().equals(Encoding.IDENTITY));
            }

            result = identity;
        }

        if (result) {
            // Test the size of the representation
            result = (getMinimumSize() == EncoderService.ANY_SIZE)
                    || (representation.getSize() == Representation.UNKNOWN_SIZE)
                    || (representation.getSize() >= getMinimumSize());
        }

        if (result) {
            // Test the acceptance of the media type
            MediaType mediaType = representation.getMediaType();
            boolean accepted = false;

            for (Iterator<MediaType> iter = getAcceptedMediaTypes().iterator(); !accepted
                    && iter.hasNext();) {
                accepted = iter.next().includes(mediaType);
            }

            result = accepted;
        }

        if (result) {
            // Test the rejection of the media type
            MediaType mediaType = representation.getMediaType();
            boolean rejected = false;

            for (Iterator<MediaType> iter = getIgnoredMediaTypes().iterator(); !rejected
                    && iter.hasNext();) {
                rejected = iter.next().includes(mediaType);
            }

            result = !rejected;
        }

        return result;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new Encoder(context, false, true, this);
    }

    @Override
    public Filter createOutboundFilter(Context context) {
        return new Encoder(context, true, false, this);
    }

    /**
     * Returns the media types that should be encoded.
     * 
     * @return The media types that should be encoded.
     */
    public List<MediaType> getAcceptedMediaTypes() {
        return this.acceptedMediaTypes;
    }

    /**
     * Returns the media types that should be ignored.
     * 
     * @return The media types that should be ignored.
     */
    public List<MediaType> getIgnoredMediaTypes() {
        return this.ignoredMediaTypes;
    }

    /**
     * Returns the minimum size a representation must have before compression is
     * done.
     * 
     * @return The minimum size a representation must have before compression is
     *         done.
     */
    public long getMinimumSize() {
        return this.mininumSize;
    }

    /**
     * Sets the minimum size a representation must have before compression is
     * done.
     * 
     * @param mininumSize
     *            The minimum size a representation must have before compression
     *            is done.
     */
    public void setMinimumSize(long mininumSize) {
        this.mininumSize = mininumSize;
    }

}
