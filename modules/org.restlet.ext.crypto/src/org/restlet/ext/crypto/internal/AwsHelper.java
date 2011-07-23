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
public class AwsHelper extends AuthenticatorHelper {

    /**
     * Constructor.
     */
    public AwsHelper() {
        super(ChallengeScheme.HTTP_AWS_S3, true, true);
    }

    @Override
    public void formatRawResponse(ChallengeWriter cw,
            ChallengeResponse challenge, Request request,
            Series<Header> httpHeaders) {
        // Append the AWS credentials
        cw.append(challenge.getIdentifier())
                .append(':')
                .append(AwsUtils.getSignature(request, httpHeaders,
                        challenge.getSecret()));
    }

}
