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

package org.restlet.engine.security;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.util.Base64;
import org.restlet.util.Series;

/**
 * Implements the SMTP PLAIN authentication.
 * 
 * @author Jerome Louvel
 */
public class SmtpPlainHelper extends AuthenticatorHelper {

    /**
     * Constructor.
     */
    public SmtpPlainHelper() {
        super(ChallengeScheme.SMTP_PLAIN, true, false);
    }

    @Override
    public void formatResponse(ChallengeWriter cw, ChallengeResponse challenge,
            Request request, Series<Header> httpHeaders) {
        try {
            final CharArrayWriter credentials = new CharArrayWriter();
            credentials.write("^@");
            credentials.write(challenge.getIdentifier());
            credentials.write("^@");
            credentials.write(challenge.getSecret());
            cw.append(Base64.encode(credentials.toCharArray(), "US-ASCII",
                    false));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unsupported encoding, unable to encode credentials");
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected exception, unable to encode credentials", e);
        }
    }

}
