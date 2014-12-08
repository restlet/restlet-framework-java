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

import java.io.IOException;

import org.restlet.data.ClientInfo;
import org.restlet.data.Expectation;

/**
 * Expectation header reader.
 * 
 * @author Jerome Louvel
 */
public class ExpectationReader extends HeaderReader<Expectation> {
    /**
     * Adds values to the given collection.
     * 
     * @param header
     *            The header to read.
     * @param clientInfo
     *            The client info to update.
     */
    public static void addValues(String header, ClientInfo clientInfo) {
        if (header != null) {
            new ExpectationReader(header).addValues(clientInfo
                    .getExpectations());
        }
    }

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public ExpectationReader(String header) {
        super(header);
    }

    @Override
    public Expectation readValue() throws IOException {
        Expectation result = readNamedValue(Expectation.class);

        while (skipParameterSeparator()) {
            result.getParameters().add(readParameter());
        }

        return result;
    }

}
