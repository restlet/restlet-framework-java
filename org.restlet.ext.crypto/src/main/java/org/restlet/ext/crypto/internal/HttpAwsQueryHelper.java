/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.crypto.internal;

import java.util.Date;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.DateUtils;

/**
 * Implements the HTTP authentication for the Amazon Web Services.
 * 
 * @author Jerome Louvel
 */
public class HttpAwsQueryHelper extends AuthenticatorHelper {

    /**
     * Constructor.
     */
    public HttpAwsQueryHelper() {
        super(ChallengeScheme.HTTP_AWS_QUERY, true, false);
    }

    @Override
    public Reference updateReference(Reference resourceRef,
            ChallengeResponse challengeResponse, Request request) {
        Reference result = resourceRef;
        Form query = result.getQueryAsForm();

        if (query.getFirst("Action") != null) {
            query.add("AWSAccessKeyId", request.getChallengeResponse().getIdentifier());
            query.add("SignatureMethod", "HmacSHA256");
            query.add("SignatureVersion", "2");
            query.add("Version", "2009-04-15");
            String df = DateUtils.format(new Date(), DateUtils.FORMAT_ISO_8601.get(0));
            query.add("Timestamp", df);

            // Compute then add the signature parameter
            String signature = AwsUtils.getQuerySignature(request.getMethod(),
                    request.getResourceRef(), query, request
                            .getChallengeResponse().getSecret());
            query.add("Signature", signature);
            result = new Reference(resourceRef);
            result.setQuery(query.getQueryString());
        }

        return result;
    }
}
