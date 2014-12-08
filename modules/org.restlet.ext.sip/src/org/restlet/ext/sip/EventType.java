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

package org.restlet.ext.sip;

import java.util.ArrayList;
import java.util.List;

/**
 * Described an event type. Used by the SIP "Event" and "Allow-Events" headers.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class EventType {

    /** The package name. */
    private String _package;

    /** The list of event templates. */
    private List<String> eventTemplates;

    /**
     * Constructor.
     * 
     * @param _package
     *            The package name.
     */
    public EventType(String _package) {
        super();
        this._package = _package;
    }

    /**
     * Returns the list of event templates.
     * 
     * @return The list of event templates.
     */
    public List<String> getEventTemplates() {
        if (eventTemplates == null) {
            eventTemplates = new ArrayList<String>();
        }
        return eventTemplates;
    }

    /**
     * Returns the package name.
     * 
     * @return The package name.
     */
    public String getPackage() {
        return _package;
    }

    /**
     * Sets the list of event templates.
     * 
     * @param eventTemplates
     *            The list of event templates.
     */
    public void setEventTemplates(List<String> eventTemplates) {
        this.eventTemplates = eventTemplates;
    }

    /**
     * Sets the package name.
     * 
     * @param _package
     *            The package name.
     */
    public void setPackage(String _package) {
        this._package = _package;
    }

}
