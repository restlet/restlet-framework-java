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

package org.restlet.ext.sip.internal;

import java.io.IOException;

import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderReader;
import org.restlet.ext.sip.Availability;

/**
 * Retry-after header reader.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class AvailabilityReader extends HeaderReader<Availability> {

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public AvailabilityReader(String header) {
        super(header);
    }

    @Override
    public Availability readValue() throws IOException {
        Availability result = null;

        skipSpaces();
        if (peek() != -1) {
            String delay = readToken();
            result = new Availability(Integer.parseInt(delay));
            skipSpaces();
            if (peek() == '(') {
                result.setComment(readComment());
            }
            skipSpaces();
        }

        // Read availability parameters.
        if (skipParameterSeparator()) {
            Parameter param = readParameter();

            while (param != null) {
                if ("duration".equals(param.getName())) {
                    result.setDuration(Integer.parseInt(param.getValue()));
                } else {
                    result.getParameters().add(param);
                }

                if (skipParameterSeparator()) {
                    param = readParameter();
                } else {
                    param = null;
                }
            }
        }

        return result;
    }

}
