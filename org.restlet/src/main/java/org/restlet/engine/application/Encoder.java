/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import java.util.List;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Preference;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;
import org.restlet.service.EncoderService;

/**
 * Filter compressing entities. The best encoding is automatically selected based on the preferences
 * of the client and on the encoding supported by NRE: GZip, Zip, and Deflate.<br>
 * If the {@link org.restlet.representation.Representation} has an unknown size, it will always be a
 * candidate for encoding. Candidate representations need to respect media type criteria by the
 * lists of accepted and ignored media types.
 *
 * <p>Concurrency note: instances of this class or its subclasses can be invoked by several threads
 * at the same time and therefore must be thread-safe. You should be especially careful when storing
 * state in member variables.
 *
 * @author Lars Heuer
 * @author Jerome Louvel
 */
public class Encoder extends Filter {

    /** Indicates if the request entity should be encoded. */
    private final boolean encodingRequest;

    /** Indicates if the response entity should be encoded. */
    private final boolean encodingResponse;

    /** The parent encoder service. */
    private final EncoderService encoderService;

    /**
     * Constructor.
     *
     * @param context The context.
     * @param encodingRequest Indicates if the request entities should be encoded.
     * @param encodingResponse Indicates if the response entities should be encoded.
     * @param encoderService The parent encoder service.
     */
    public Encoder(
            Context context,
            boolean encodingRequest,
            boolean encodingResponse,
            EncoderService encoderService) {
        super(context);
        this.encodingRequest = encodingRequest;
        this.encodingResponse = encodingResponse;
        this.encoderService = encoderService;
    }

    /**
     * Allows filtering of a request and a response after the target Restlet handled the request.
     * Does nothing by default.
     *
     * @param request The request to filter.
     * @param response The response to filter.
     */
    @Override
    public void afterHandle(Request request, Response response) {
        // Check if encoding of the response entity is needed
        if (isEncodingResponse() && getEncoderService().canEncode(response.getEntity())) {
            response.setEntity(encode(request.getClientInfo(), response.getEntity()));
        }
    }

    /**
     * Allows filtering before its handling by the target Restlet. Does nothing by default.
     *
     * @param request The request to filter.
     * @param response The response to filter.
     * @return The continuation status.
     */
    @Override
    public int beforeHandle(Request request, Response response) {
        // Check if encoding of the request entity is needed
        if (isEncodingRequest() && getEncoderService().canEncode(request.getEntity())) {
            request.setEntity(encode(request.getClientInfo(), request.getEntity()));
        }

        return CONTINUE;
    }

    /**
     * Encodes a given representation if the client supports an encoding.
     *
     * @param client The client preferences to use.
     * @param representation The representation to encode.
     * @return The encoded representation or the original one if no encoding supported by the
     *     client.
     */
    public Representation encode(ClientInfo client, Representation representation) {
        Representation result = representation;
        Encoding bestEncoding = getBestEncoding(client);

        if (bestEncoding != null) {
            result = new EncodeRepresentation(bestEncoding, representation);
        }

        return result;
    }

    /**
     * Returns the best supported encoding for a given client.
     *
     * @param client The client preferences to use.
     * @return The best supported encoding for the given call.
     */
    public Encoding getBestEncoding(ClientInfo client) {
        Encoding bestEncoding = null;
        Encoding currentEncoding = null;
        Preference<Encoding> currentPref = null;
        float bestScore = 0F;

        for (Encoding encoding : getSupportedEncodings()) {
            currentEncoding = encoding;

            for (Preference<Encoding> encodingPreference : client.getAcceptedEncodings()) {
                currentPref = encodingPreference;

                if ((currentPref.getMetadata().equals(Encoding.ALL)
                                || currentPref.getMetadata().equals(currentEncoding))
                        && currentPref.getQuality() > bestScore) {
                    bestScore = currentPref.getQuality();
                    bestEncoding = currentEncoding;
                }
            }
        }

        return bestEncoding;
    }

    /**
     * Returns the parent encoder service.
     *
     * @return The parent encoder service.
     */
    public EncoderService getEncoderService() {
        return encoderService;
    }

    /**
     * Returns the list of supported encodings. By default, it calls {@link
     * EncodeRepresentation#getSupportedEncodings()} static method.
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
     */
    public boolean isEncodingRequest() {
        return this.encodingRequest;
    }

    /**
     * Indicates if the response entity should be encoded.
     *
     * @return True if the response entity should be encoded.
     */
    public boolean isEncodingResponse() {
        return this.encodingResponse;
    }
}
