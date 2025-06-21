/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.crypto.internal;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.util.Series;

/**
 * Implements the HTTP authentication for the Amazon S3 service.
 * 
 * @author Jerome Louvel
 */
public class HttpAwsS3Helper extends AuthenticatorHelper {

    /**
     * Constructor.
     */
    public HttpAwsS3Helper() {
        super(ChallengeScheme.HTTP_AWS_S3, true, true);
    }

    @Override
    public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge,
            Request request, Series<Header> httpHeaders) {
        // Append the AWS credentials
        cw.append(challenge.getIdentifier())
                .append(':')
                .append(AwsUtils.getS3Signature(request, httpHeaders,
                        challenge.getSecret()));
    }

}
