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

package org.restlet.engine.header;

import static org.restlet.engine.header.HeaderUtils.isSpace;

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
            w.close();
        }

        return result;
    }
}
