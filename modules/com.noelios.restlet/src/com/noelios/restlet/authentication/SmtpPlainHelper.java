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
 */

package com.noelios.restlet.authentication;

import java.io.UnsupportedEncodingException;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.util.Series;

import com.noelios.restlet.util.Base64;

/**
 * Implements the SMTP PLAIN authentication.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SmtpPlainHelper extends AuthenticationHelper {

    /**
     * Constructor.
     */
    public SmtpPlainHelper() {
        super(ChallengeScheme.SMTP_PLAIN, true, false);
    }

    @Override
    public void formatCredentials(StringBuilder sb,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
        try {
            final String credentials = "^@" + challenge.getIdentifier() + "^@"
                    + new String(challenge.getSecret());
            sb.append(Base64.encode(credentials.getBytes("US-ASCII"), false));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unsupported encoding, unable to encode credentials");
        }
    }

}
