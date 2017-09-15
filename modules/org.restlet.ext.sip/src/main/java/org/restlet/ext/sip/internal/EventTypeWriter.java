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

import java.util.List;

import org.restlet.engine.header.HeaderWriter;
import org.restlet.ext.sip.EventType;

/**
 * Event type like header writer.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class EventTypeWriter extends HeaderWriter<EventType> {

    /**
     * Writes an event type.
     * 
     * @param eventType
     *            The event type.
     * @return The formatted event type.
     */
    public static String write(EventType eventType) {
        return new EventTypeWriter().append(eventType).toString();
    }

    /**
     * Writes a list of event types.
     * 
     * @param eventTypes
     *            The list of event types.
     * @return The formatted list of event types.
     */
    public static String write(List<EventType> eventTypes) {
        return new EventTypeWriter().append(eventTypes).toString();
    }

    @Override
    public HeaderWriter<EventType> append(EventType value) {
        if (value != null && value.getPackage() != null) {
            append(value.getPackage());
            for (String template : value.getEventTemplates()) {
                append(".");
                append(template);
            }
        }

        return this;
    }

}
