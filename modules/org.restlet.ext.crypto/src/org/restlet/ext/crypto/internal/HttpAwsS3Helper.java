/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.crypto.internal;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.Header;
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
    public void formatResponse(ChallengeWriter cw,
            ChallengeResponse challenge, Request request,
            Series<Header> httpHeaders) {
        // Append the AWS credentials
        cw.append(challenge.getIdentifier())
                .append(':')
                .append(AwsUtils.getS3Signature(request, httpHeaders,
                        challenge.getSecret()));
    }

}
