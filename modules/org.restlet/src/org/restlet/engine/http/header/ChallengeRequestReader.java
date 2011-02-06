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

package org.restlet.engine.http.header;

import static org.restlet.engine.http.header.HeaderUtils.isSpace;

import java.io.IOException;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;

/**
 * Challenge request header reader.
 * 
 * @author Thierry Boileau
 */
public class ChallengeRequestReader extends HeaderReader<ChallengeRequest> {

    public static void main(String[] args) throws Exception {
        String str = "Basic realm=\"Control Panel\"";
        ChallengeRequestReader r = new ChallengeRequestReader(str);
        ChallengeRequest c = r.readValue();
        System.out.println(c.getScheme());
        System.out.println(c.getRawValue());

        str = "Digest realm=\"Control Panel\", domain=\"/controlPanel\", nonce=\"15bb54af506016d4414a025d4c84e34c\", algorithm=MD5, qop=\"auth,auth-int\"";
        r = new ChallengeRequestReader(str);
        c = r.readValue();
        System.out.println(c.getScheme());
        System.out.println(c.getRawValue());

        str = "Negotiate";
        r = new ChallengeRequestReader(str);
        c = r.readValue();
        System.out.println(c.getScheme());
        System.out.println(c.getRawValue());

        str = "Basic realm=\"Control Panel\",Digest realm=\"Control Panel\", domain=\"/controlPanel\", nonce=\"15bb54af506016d4414a025d4c84e34c\", algorithm=MD5, qop=\"auth,auth-int\"";
        r = new ChallengeRequestReader(str);
        System.out.println("list");
        for (ChallengeRequest challengeRequest : r.readValues()) {
            System.out.println(challengeRequest.getScheme());
            System.out.println(challengeRequest.getRawValue());
        }
    }

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public ChallengeRequestReader(String header) {
        super(header);
    }

    @Override
    public ChallengeRequest readValue() throws IOException {
        ChallengeRequest result = null;

        // The challenge is that this header is a comma separated lst of
        // challenges, and that each challenges is also a comma separated list,
        // but of parameters.
        skipSpaces();
        if (peek() != -1) {
            String scheme = readToken();
            result = new ChallengeRequest(new ChallengeScheme("HTTP_" + scheme,
                    scheme));
            skipSpaces();

            // Header writer that will reconstruct the raw value of a challenge.
            HeaderWriter<Parameter> w = new HeaderWriter<Parameter>() {
                @Override
                public HeaderWriter<Parameter> append(Parameter value) {
                    appendExtension(value);
                    return this;
                }
            };

            boolean stop = false;
            while (peek() != -1 && !stop) {
                boolean sepSkipped = skipValueSeparator();
                // Record the start of the segment
                mark();
                // Read a token and the next character.
                readToken();
                int nextChar = read();
                reset();
                if (isSpace(nextChar)) {
                    // A new scheme has been discovered.
                    stop = true;
                } else {
                    // The next segment is considered as a parameter
                    if (sepSkipped) {
                        // Add the skipped value separator.
                        w.appendValueSeparator();
                    }
                    // Append the parameter
                    w.append(readParameter());
                }
            }
            result.setRawValue(w.toString());
        }

        return result;
    }
}
