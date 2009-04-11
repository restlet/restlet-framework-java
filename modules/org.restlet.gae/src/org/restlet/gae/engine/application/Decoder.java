/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.gae.engine.application;

import java.util.Iterator;

import org.restlet.gae.Context;
import org.restlet.gae.data.Encoding;
import org.restlet.gae.data.Request;
import org.restlet.gae.data.Response;
import org.restlet.gae.representation.Representation;
import org.restlet.gae.routing.Filter;

/**
 * Filter decompressing entities.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class Decoder extends Filter {
    /**
     * Indicates if the request entity should be decoded.
     */
    private volatile boolean decodeRequest;

    /**
     * Indicates if the response entity should be decoded.
     */
    private volatile boolean decodeResponse;

    /**
     * Constructor to only decode request entities before handling.
     * 
     * @param context
     *            The context.
     */
    public Decoder(Context context) {
        this(context, true, false);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param decodeRequest
     *            Indicates if the request entity should be decoded.
     * @param decodeResponse
     *            Indicates if the response entity should be decoded.
     */
    public Decoder(Context context, boolean decodeRequest,
            boolean decodeResponse) {
        super(context);
        this.decodeRequest = decodeRequest;
        this.decodeResponse = decodeResponse;
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
        // Check if decoding of the response entity is needed
        if (isDecodeResponse() && canDecode(response.getEntity())) {
            response.setEntity(decode(response.getEntity()));
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
        // Check if decoding of the request entity is needed
        if (isDecodeRequest() && canDecode(request.getEntity())) {
            request.setEntity(decode(request.getEntity()));
        }

        return CONTINUE;
    }

    /**
     * Indicates if a representation can be decoded.
     * 
     * @param representation
     *            The representation to test.
     * @return True if the call can be decoded.
     */
    public boolean canDecode(Representation representation) {
        // Test the existence of the representation and that at least an
        // encoding applies.
        boolean result = (representation != null)
                && (!representation.getEncodings().isEmpty());

        if (result) {
            boolean found = false;
            for (final Iterator<Encoding> iter = representation.getEncodings()
                    .iterator(); !found && iter.hasNext();) {
                found = (!iter.next().equals(Encoding.IDENTITY));
            }
            result = found;
        }
        return result;
    }

    /**
     * Decodes a given representation if its encodings are supported by NRE.
     * 
     * @param representation
     *            The representation to encode.
     * @return The decoded representation or the original one if the encoding
     *         isn't supported by NRE.
     */
    public Representation decode(Representation representation) {
        Representation result = representation;

        // Check if all encodings of the representation are supported in order
        // to avoid the creation of a useless decodeRepresentation object.
        // False if an encoding is not supported
        boolean supported = true;
        // True if all representation's encodings are IDENTITY
        boolean identityEncodings = true;
        for (final Iterator<Encoding> iter = representation.getEncodings()
                .iterator(); supported && iter.hasNext();) {
            final Encoding encoding = iter.next();
            supported = DecodeRepresentation.getSupportedEncodings().contains(
                    encoding);
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
    public boolean isDecodeRequest() {
        return this.decodeRequest;
    }

    /**
     * Indicates if the response entity should be decoded.
     * 
     * @return True if the response entity should be decoded.
     */
    public boolean isDecodeResponse() {
        return this.decodeResponse;
    }

    /**
     * Indicates if the request entity should be decoded.
     * 
     * @param decodeRequest
     *            True if the request entity should be decoded.
     */
    public void setDecodeRequest(boolean decodeRequest) {
        this.decodeRequest = decodeRequest;
    }

    /**
     * Indicates if the response entity should be decoded.
     * 
     * @param decodeResponse
     *            True if the response entity should be decoded.
     */
    public void setDecodeResponse(boolean decodeResponse) {
        this.decodeResponse = decodeResponse;
    }

}
