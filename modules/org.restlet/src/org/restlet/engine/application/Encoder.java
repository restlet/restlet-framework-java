/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.engine.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

// [excludes gwt]
/**
 * Filter compressing entities. The best encoding is automatically selected
 * based on the preferences of the client and on the encoding supported by NRE:
 * GZip, Zip and Deflate.
 * <p>
 * If the {@link org.restlet.representation.Representation} has an unknown size,
 * it will always be a candidate for encoding. Candidate representations need to
 * respect media type criteria by the lists of accepted and ignored media types.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel
 */
public class Encoder extends Filter {
    /**
     * Indicates if the encoding should always occur, regardless of the size.
     */
    public static final int ENCODE_ALL_SIZES = -1;

    /**
     * Returns the list of default encoded media types. This can be overridden
     * by subclasses. By default, all media types are encoded (except those
     * explicitly ignored).
     * 
     * @return The list of default encoded media types.
     */
    public static List<MediaType> getDefaultAcceptedMediaTypes() {
        final List<MediaType> result = new ArrayList<MediaType>();
        result.add(MediaType.ALL);
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
    private volatile List<MediaType> acceptedMediaTypes;

    /**
     * Indicates if the request entity should be encoded.
     */
    private volatile boolean encodingRequest;

    /**
     * Indicates if the response entity should be encoded.
     */
    private volatile boolean encodingResponse;

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
     *            The context.
     */
    public Encoder(Context context) {
        this(context, false, true, ENCODE_ALL_SIZES,
                getDefaultAcceptedMediaTypes(), getDefaultIgnoredMediaTypes());
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param encodingInput
     *            Indicates if the request entities should be encoded.
     * @param encodingOutput
     *            Indicates if the response entities should be encoded.
     * @param minimumSize
     *            The minimal size of the representation where compression
     *            should be used.
     * @param acceptedMediaTypes
     *            The media types that should be encoded.
     * @param ignoredMediaTypes
     *            The media types that should be ignored.
     */
    public Encoder(Context context, boolean encodingInput,
            boolean encodingOutput, long minimumSize,
            List<MediaType> acceptedMediaTypes,
            List<MediaType> ignoredMediaTypes) {
        super(context);
        this.encodingRequest = encodingInput;
        this.encodingResponse = encodingOutput;
        this.mininumSize = minimumSize;
        this.acceptedMediaTypes = acceptedMediaTypes;
        this.ignoredMediaTypes = ignoredMediaTypes;
    }

    /**
     * Allows filtering after its handling by the target Restlet. Does nothing
     * by default.
     * 
     * @param request
     *            The request to filter.
     * @param response
     *            The response to filter.
     */
    @Override
    public void afterHandle(Request request, Response response) {
        // Check if encoding of the response entity is needed
        if (isEncodingResponse() && canEncode(response.getEntity())) {
            response.setEntity(encode(request.getClientInfo(),
                    response.getEntity()));
        }
    }

    /**
     * Allows filtering before its handling by the target Restlet. Does nothing
     * by default.
     * 
     * @param request
     *            The request to filter.
     * @param response
     *            The response to filter.
     * @return The continuation status.
     */
    @Override
    public int beforeHandle(Request request, Response response) {
        // Check if encoding of the request entity is needed
        if (isEncodingRequest() && canEncode(request.getEntity())) {
            request.setEntity(encode(request.getClientInfo(),
                    request.getEntity()));
        }

        return CONTINUE;
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
            for (final Iterator<Encoding> iter = representation.getEncodings()
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
            final MediaType mediaType = representation.getMediaType();
            boolean accepted = false;
            for (final Iterator<MediaType> iter = getAcceptedMediaTypes()
                    .iterator(); !accepted && iter.hasNext();) {
                accepted = iter.next().includes(mediaType);
            }

            result = accepted;
        }

        if (result) {
            // Test the rejection of the media type
            final MediaType mediaType = representation.getMediaType();
            boolean rejected = false;
            for (final Iterator<MediaType> iter = getIgnoredMediaTypes()
                    .iterator(); !rejected && iter.hasNext();) {
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
     *            The client preferences to use.
     * @param representation
     *            The representation to encode.
     * @return The encoded representation or the original one if no encoding
     *         supported by the client.
     */
    public Representation encode(ClientInfo client,
            Representation representation) {
        Representation result = representation;
        final Encoding bestEncoding = getBestEncoding(client);

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
     *            The client preferences to use.
     * @return The best supported encoding for the given call.
     */
    public Encoding getBestEncoding(ClientInfo client) {
        Encoding bestEncoding = null;
        Encoding currentEncoding = null;
        Preference<Encoding> currentPref = null;
        float bestScore = 0F;

        for (Iterator<Encoding> iter = getSupportedEncodings().iterator(); iter
                .hasNext();) {
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
        return this.mininumSize;
    }

    /**
     * Returns the list of supported encodings. By default it calls
     * {@link EncodeRepresentation#getSupportedEncodings()} static method.
     * 
     * @return The list of supported encodings.
     */
    public List<Encoding> getSupportedEncodings() {
        return EncodeRepresentation.getSupportedEncodings();
    }

    /**
     * Indicates if the request entity should be encoded.
     * 
     * @return True if the request entity should be encoded.
     * @deprecated Use {@link #isEncodingRequest()} instead.
     */
    @Deprecated
    public boolean isEncodeRequest() {
        return this.encodingRequest;
    }

    /**
     * Indicates if the response entity should be encoded.
     * 
     * @return True if the response entity should be encoded.
     * @deprecated Use {@link #isEncodingResponse()} instead.
     */
    @Deprecated
    public boolean isEncodeResponse() {
        return this.encodingResponse;
    }

    /**
     * Indicates if the request entity should be encoded.
     * 
     * @return True if the request entity should be encoded.
     */
    public boolean isEncodingRequest() {
        return isEncodeRequest();
    }

    /**
     * Indicates if the response entity should be encoded.
     * 
     * @return True if the response entity should be encoded.
     */
    public boolean isEncodingResponse() {
        return isEncodeResponse();
    }

    /**
     * Indicates if the request entity should be encoded.
     * 
     * @param encodingRequest
     *            True if the request entity should be encoded.
     * @deprecated Use {@link #setEncodingRequest(boolean)} instead.
     */
    @Deprecated
    public void setEncodeRequest(boolean encodingRequest) {
        this.encodingRequest = encodingRequest;
    }

    /**
     * Indicates if the response entity should be encoded.
     * 
     * @param encodingResponse
     *            True if the response entity should be encoded.
     * @deprecated Use {@link #setEncodingResponse(boolean)} instead.
     */
    @Deprecated
    public void setEncodeResponse(boolean encodingResponse) {
        this.encodingResponse = encodingResponse;
    }

    /**
     * Indicates if the request entity should be encoded.
     * 
     * @param encodingRequest
     *            True if the request entity should be encoded.
     */
    public void setEncodingRequest(boolean encodingRequest) {
        setEncodeRequest(encodingRequest);
    }

    /**
     * Indicates if the response entity should be encoded.
     * 
     * @param encodingResponse
     *            True if the response entity should be encoded.
     */
    public void setEncodingResponse(boolean encodingResponse) {
        setEncodeResponse(encodingResponse);
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
