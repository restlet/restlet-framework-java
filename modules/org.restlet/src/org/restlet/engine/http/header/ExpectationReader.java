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

import java.io.IOException;

import org.restlet.data.ClientInfo;
import org.restlet.data.Expectation;
import org.restlet.data.Parameter;

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
    protected Parameter createParameter(String name, String value) {
        return new Expectation(name, value);
    }

    @Override
    public Expectation readValue() throws IOException {
        Expectation result = (Expectation) readParameter();

        while (skipParameterSeparator()) {
            result.getParameters().add(readParameter());
        }

        return result;
    }

}
