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

package org.restlet.ext.sip.internal;

import java.io.IOException;

import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderReader;
import org.restlet.ext.sip.Subscription;

/**
 * Subscription state header reader.
 * 
 * @author Thierry Boileau
 */
public class SubscriptionReader extends HeaderReader<Subscription> {

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public SubscriptionReader(String header) {
        super(header);
    }

    @Override
    public Subscription readValue() throws IOException {
        Subscription result = null;

        skipSpaces();

        if (peek() != -1) {
            String value = readToken();
            if (value != null) {
                result = new Subscription(value);

                // Read subscription parameters.
                if (skipParameterSeparator()) {
                    Parameter param = readParameter();

                    while (param != null) {
                        if ("reason".equals(param.getName())) {
                            result.setReason(param.getValue());
                        } else if ("expires".equals(param.getName())) {
                            result.setExpires(Long.parseLong(param.getValue()));
                        } else if ("retry-after".equals(param.getName())) {
                            result.setRetryAfter(Long.parseLong(param
                                    .getValue()));
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
            }
        }

        return result;
    }

}
