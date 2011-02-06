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

package org.restlet.data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.engine.http.header.HeaderConstants;

/**
 * Particular server behavior that is required by a client. Note that when used
 * with HTTP connectors, this class maps to the "Expect" header.
 * 
 * @author Jerome Louvel
 */
public final class Expectation extends Parameter {

    /**
     * Creates a "100-continue" expectation. If a client will wait for a 100
     * (Continue) provisional response before sending the request body, it MUST
     * send this expectation. A client MUST NOT send this expectation if it does
     * not intend to send a request entity.
     * 
     * @return A new "100-continue" expectation.
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.20">HTTP
     *      1.1 - Expect header</a>
     */
    public static Expectation continueResponse() {
        return new Expectation(HeaderConstants.EXPECT_CONTINUE);
    }

    /** The list of parameters. */
    private volatile List<Parameter> parameters;

    /**
     * Constructor for directives with no value.
     * 
     * @param name
     *            The directive name.
     */
    public Expectation(String name) {
        this(name, null);
    }

    /**
     * Constructor for directives with a value.
     * 
     * @param name
     *            The directive name.
     * @param value
     *            The directive value.
     */
    public Expectation(String name, String value) {
        super(name, value);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    /**
     * Returns the list of parameters.
     * 
     * @return The list of parameters.
     */
    public List<Parameter> getParameters() {
        // Lazy initialization with double-check.
        List<Parameter> r = this.parameters;
        if (r == null) {
            synchronized (this) {
                r = this.parameters;
                if (r == null) {
                    this.parameters = r = new CopyOnWriteArrayList<Parameter>();
                }
            }
        }
        return r;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *            The list of parameters.
     */
    public void setParameters(List<Parameter> parameters) {
        synchronized (this) {
            List<Parameter> r = getParameters();
            r.clear();
            r.addAll(parameters);
        }
    }

}