/**
 * Copyright 2005-2014 Restlet
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

package org.restlet.ext.raml.internal;

import org.raml.model.Raml;
import org.restlet.ext.apispark.model.Definition;

/**
 * Exception used when translating {@link Definition} objet to {@link Raml} and
 * vice-versa.
 * 
 * @author Cyprien Quilici
 */
@SuppressWarnings("serial")
public class TranslationException extends Exception {
    /** The type of the translation error. */
    private String type;

    /**
     * Constructor.
     * 
     * @param message
     *            The detailed message.
     * @param type
     *            The type of the translation error.
     */
    public TranslationException(String message, String type) {
        super(message);
        this.type = type;
    }

    /**
     * Returns the type of the translation error.
     * 
     * @return The type of the translation error.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the translation error.
     * 
     * @param type
     *            The type of the translation error.
     */
    public void setType(String type) {
        this.type = type;
    }

}
