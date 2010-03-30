/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.sip;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Used by the "Event" header.
 * 
 * @author Thierry Boileau
 * 
 */
public class Event {

    /** The event identifier. */
    private String id;

    /** The event parameters. */
    private Series<Parameter> parameters;

    /** The type of the event. */
    private EventType type;

    /**
     * Constructor.
     */
    public Event() {
        super();
    }

    /**
     * Returns the event identifier.
     * 
     * @return The event identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the event parameters.
     * 
     * @return The event parameters.
     */
    public Series<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new Form();
        }
        return parameters;
    }

    /**
     * Returns the type of the event.
     * 
     * @return The type of the event.
     */
    public EventType getType() {
        return type;
    }

    /**
     * Sets the event identifier.
     * 
     * @param id
     *            The event identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the event parameters.
     * 
     * @param parameters
     *            The event parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the type of the event.
     * 
     * @param type
     *            The type of the event.
     */
    public void setType(EventType type) {
        this.type = type;
    }

}
