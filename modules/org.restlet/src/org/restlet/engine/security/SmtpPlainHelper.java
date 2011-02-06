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

package org.restlet.engine.security;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.ChallengeWriter;
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
    public void formatRawResponse(ChallengeWriter cw,
            ChallengeResponse challenge, Request request,
            Series<Parameter> httpHeaders) {
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
