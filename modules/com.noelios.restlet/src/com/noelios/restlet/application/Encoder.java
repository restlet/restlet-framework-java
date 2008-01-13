/*
 * Copyright 2005-2008 Noelios Consulting.
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
 * 
 * Portions Copyright 2006 Lars Heuer (heuer[at]semagia.com)
 */

package com.noelios.restlet.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Filter compressing entities. The best encoding is automatically selected
 * based on the preferences of the client and on the encoding supported by NRE:
 * GZip, Zip and Deflate.<br/> If the
 * {@link org.restlet.resource.Representation} has an unknown size, it will
 * always be a candidate for encoding. Candidate representations need to respect
 * media type criteria by the lists of accepted and ignored media types.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact@noelios.com) <a
 *         href="http://www.noelios.com">Noelios Consulting</a>
 */
public class Encoder extends Filter {
    /**
     * Indicates if the encoding should always occur, regardless of the size.
     */
    public static final int ENCODE_ALL_SIZES = -1;

    /**
     * Returns the list of default encoded media types. This can be overriden by
     * subclasses. By default, all media types are encoded (except those
     * explicitely ignored).
     * 
     * @return The list of default encoded media types.
     */
    public static List<MediaType> getDefaultAcceptedMediaTypes() {
        List<MediaType> result = new ArrayList<MediaType>();
        result.add(MediaType.ALL);
        return result;
    }

    /**
     * Returns the list of default ignored media types. This can be overriden by
     * subclasses. By default, all archive, audio, image and video media types
     * are ignored.
     * 
     * @return The list of default ignored media types.
     */
    public static List<MediaType> getDefaultIgnoredMediaTypes() {
        List<MediaType> result = Arrays.<MediaType> asList(
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
    private volatile List<MediaType> acceptedMediaTypes;

    /**
     * Indicates if the request entity should be encoded.
     */
    private volatile boolean encodeRequest;

    /**
     * Indicates if the response entity should be encoded.
     */
    private volatile boolean encodeResponse;

    /**
     * The media types that should be ignored.
     */
    private volatile List<MediaType> ignoredMediaTypes;

    /**
     * The minimal size necessary for encoding.
     */
    private volatile long mininumSize;

    /**
     * Constructor using the default media types and with
     * {@link #ENCODE_ALL_SIZES} setting. This constructor will only encode
     * response entities after call handling.
     * 
     * @param context
     *                The context.
     */
    public Encoder(Context context) {
        this(context, false, true, ENCODE_ALL_SIZES,
                getDefaultAcceptedMediaTypes(), getDefaultIgnoredMediaTypes());
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param encodeInput
     *                Indicates if the request entities should be encoded.
     * @param encodeOutput
     *                Indicates if the response entities should be encoded.
     * @param minimumSize
     *                The minimal size of the representation where compression
     *                should be used.
     * @param acceptedMediaTypes
     *                The media types that should be encoded.
     * @param ignoredMediaTypes
     *                The media types that should be ignored.
     */
    public Encoder(Context context, boolean encodeInput, boolean encodeOutput,
            long minimumSize, List<MediaType> acceptedMediaTypes,
            List<MediaType> ignoredMediaTypes) {
        super(context);
        this.encodeRequest = encodeInput;
        this.encodeResponse = encodeOutput;
        this.mininumSize = minimumSize;
        this.acceptedMediaTypes = acceptedMediaTypes;
        this.ignoredMediaTypes = ignoredMediaTypes;
    }

    /**
     * Allows filtering after its handling by the target Restlet. Does nothing
     * by default.
     * 
     * @param request
     *                The request to filter.
     * @param response
     *                The response to filter.
     */
    public void afterHandle(Request request, Response response) {
        // Check if encoding of the response entity is needed
        if (isEncodeResponse() && canEncode(response.getEntity())) {
            response.setEntity(encode(request.getClientInfo(), response
                    .getEntity()));
        }
    }

    /**
     * Allows filtering before its handling by the target Restlet. Does nothing
     * by default.
     * 
     * @param request
     *                The request to filter.
     * @param response
     *                The response to filter.
     * @return The continuation status.
     */
    public int beforeHandle(Request request, Response response) {
        // Check if encoding of the request entity is needed
        if (isEncodeRequest() && canEncode(request.getEntity())) {
            request.setEntity(encode(request.getClientInfo(), request
                    .getEntity()));
        }

        return CONTINUE;
    }

    /**
     * Indicates if a representation can be encoded.
     * 
     * @param representation
     *                The representation to test.
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
            result = (getMinimumSize() == ENCODE_ALL_SIZES)
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

    /**
     * Encodes a given representation if an encoding is supported by the client.
     * 
     * @param client
     *                The client preferences to use.
     * @param representation
     *                The representation to encode.
     * @return The encoded representation or the original one if no encoding
     *         supported by the client.
     */
    public Representation encode(ClientInfo client,
            Representation representation) {
        Representation result = representation;
        Encoding bestEncoding = getBestEncoding(client);

        if (bestEncoding != null) {
            result = new EncodeRepresentation(bestEncoding, representation);
        }

        return result;
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
     * Returns the best supported encoding for a given client.
     * 
     * @param client
     *                The client preferences to use.
     * @return The best supported encoding for the given call.
     */
    public Encoding getBestEncoding(ClientInfo client) {
        Encoding bestEncoding = null;
        Encoding currentEncoding = null;
        Preference<Encoding> currentPref = null;
        float bestScore = 0F;

        for (Iterator<Encoding> iter = EncodeRepresentation
                .getSupportedEncodings().iterator(); iter.hasNext();) {
            currentEncoding = iter.next();

            for (Iterator<Preference<Encoding>> iter2 = client
                    .getAcceptedEncodings().iterator(); iter2.hasNext();) {
                currentPref = iter2.next();

                if (currentPref.getMetadata().equals(Encoding.ALL)
                        || currentPref.getMetadata().equals(currentEncoding)) {
                    // A match was found, compute its score
                    if (currentPref.getQuality() > bestScore) {
                        bestScore = currentPref.getQuality();
                        bestEncoding = currentEncoding;
                    }
                }
            }
        }

        return bestEncoding;
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
        return mininumSize;
    }

    /**
     * Indicates if the request entity should be encoded.
     * 
     * @return True if the request entity should be encoded.
     */
    public boolean isEncodeRequest() {
        return this.encodeRequest;
    }

    /**
     * Indicates if the response entity should be encoded.
     * 
     * @return True if the response entity should be encoded.
     */
    public boolean isEncodeResponse() {
        return this.encodeResponse;
    }

    /**
     * Indicates if the request entity should be encoded.
     * 
     * @param encodeRequest
     *                True if the request entity should be encoded.
     */
    public void setEncodeRequest(boolean encodeRequest) {
        this.encodeRequest = encodeRequest;
    }

    /**
     * Indicates if the response entity should be encoded.
     * 
     * @param encodeResponse
     *                True if the response entity should be encoded.
     */
    public void setEncodeResponse(boolean encodeResponse) {
        this.encodeResponse = encodeResponse;
    }

    /**
     * Sets the minimum size a representation must have before compression is
     * done.
     * 
     * @param mininumSize
     *                The minimum size a representation must have before
     *                compression is done.
     */
    public void setMinimumSize(long mininumSize) {
        this.mininumSize = mininumSize;
    }

}
