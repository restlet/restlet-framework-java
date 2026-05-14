/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
     * @param header The header to read.
     */
    public ChallengeRequestReader(String header) {
        super(header);
    }

    @Override
    public ChallengeRequest readValue() throws IOException {
        ChallengeRequest result = null;

        // The challenge is that this header is a comma separated lst of
        // challenges, and that each challenge is also a comma-separated list,
        // but of parameters.
        skipSpaces();
        if (peek() != -1) {
            String scheme = readToken();
            result = new ChallengeRequest(new ChallengeScheme("HTTP_" + scheme, scheme));
            skipSpaces();

            // Header writer that will reconstruct the raw value of a challenge.
            HeaderWriter<Parameter> w =
                    new HeaderWriter<Parameter>() {
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
