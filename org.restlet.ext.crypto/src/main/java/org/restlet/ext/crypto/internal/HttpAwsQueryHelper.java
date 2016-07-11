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
            query.add("AWSAccessKeyId", new String(request
                    .getChallengeResponse().getIdentifier()));
            query.add("SignatureMethod", "HmacSHA256");
            query.add("SignatureVersion", "2");
            query.add("Version", "2009-04-15");
            String df = DateUtils.format(new Date(),
                    DateUtils.FORMAT_ISO_8601.get(0));
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
