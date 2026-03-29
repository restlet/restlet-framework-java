/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import java.util.Iterator;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

/**
 * Filter uncompressing entities.
 *
 * <p>Concurrency note: instances of this class or its subclasses can be invoked by several threads
 * at the same time and therefore must be thread-safe. You should be especially careful when storing
 * state in member variables.
 *
 * @author Jerome Louvel
 */
public class Decoder extends Filter {

    /** Indicates if the request entity should be decoded. */
    private final boolean decodingRequest;

    /** Indicates if the response entity should be decoded. */
    private final boolean decodingResponse;

    /**
     * Constructor to only decode request entities before handling.
     *
     * @param context The context.
     */
    public Decoder(Context context) {
        this(context, true, false);
    }

    /**
     * Constructor.
     *
     * @param context The context.
     * @param decodingRequest Indicates if the request entity should be decoded.
     * @param decodingResponse Indicates if the response entity should be decoded.
     */
    public Decoder(Context context, boolean decodingRequest, boolean decodingResponse) {
        super(context);
        this.decodingRequest = decodingRequest;
        this.decodingResponse = decodingResponse;
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
        // Check if decoding of the response entity is needed
        if (isDecodingResponse() && canDecode(response.getEntity())) {
            response.setEntity(decode(response.getEntity()));
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
        // Check if decoding of the request entity is needed
        if (isDecodingRequest() && canDecode(request.getEntity())) {
            request.setEntity(decode(request.getEntity()));
        }

        return CONTINUE;
    }

    /**
     * Indicates if a representation can be decoded.
     *
     * @param representation The representation to test.
     * @return True if the call can be decoded.
     */
    public boolean canDecode(Representation representation) {
        // Test the existence of the representation and that at least an
        // encoding applies.
        boolean result = (representation != null) && (!representation.getEncodings().isEmpty());

        if (result) {
            boolean found = false;

            for (final Iterator<Encoding> iter = representation.getEncodings().iterator();
                    !found && iter.hasNext(); ) {
                found = (!iter.next().equals(Encoding.IDENTITY));
            }

            result = found;
        }
        return result;
    }

    /**
     * Decodes a given representation if its encodings are supported by NRE.
     *
     * @param representation The representation to encode.
     * @return The decoded representation or the original one if the encoding isn't supported by
     *     NRE.
     */
    public Representation decode(Representation representation) {
        Representation result = representation;

        // Check if all encodings of the representation are supported in order
        // to avoid the creation of a useless decodeRepresentation object.
        // False if an encoding is not supported
        boolean supported = true;
        // True if all representation's encodings are IDENTITY
        boolean identityEncodings = true;
        for (final Iterator<Encoding> iter = representation.getEncodings().iterator();
                supported && iter.hasNext(); ) {
            final Encoding encoding = iter.next();
            supported = DecodeRepresentation.getSupportedEncodings().contains(encoding);
            identityEncodings &= encoding.equals(Encoding.IDENTITY);
        }

        if (supported && !identityEncodings) {
            result = new DecodeRepresentation(representation);
        }

        return result;
    }

    /**
     * Indicates if the request entity should be decoded.
     *
     * @return True if the request entity should be decoded.
     */
    public boolean isDecodingRequest() {
        return this.decodingRequest;
    }

    /**
     * Indicates if the response entity should be decoded.
     *
     * @return True if the response entity should be decoded.
     */
    public boolean isDecodingResponse() {
        return this.decodingResponse;
    }
}
