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

import static org.restlet.engine.header.HeaderUtils.isComma;
import static org.restlet.engine.header.HeaderUtils.isSpace;

import java.io.IOException;

import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderReader;
import org.restlet.ext.sip.Event;
import org.restlet.ext.sip.EventType;

/**
 * Event header reader.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class EventReader extends HeaderReader<Event> {

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public EventReader(String header) {
        super(header);
    }

    /**
     * Returns the value of the next segment.
     * 
     * @return The value of the next segment
     */
    public String readSegment() {
        // Read value until end or space or point
        StringBuilder sb = null;
        skipSpaces();
        int next = read();

        while (next != -1) {
            if (isSpace(next) || isComma(next)) {
                // Unread the separator
                unread();
                next = -1;
            } else if ((next == '.')) {
                // ready for the next segment
                next = -1;
            } else {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append((char) next);
                next = read();
            }
        }

        return (sb == null) ? null : sb.toString();
    }

    @Override
    public Event readValue() throws IOException {
        Event result = null;

        skipSpaces();
        if (peek() != -1) {
            EventType eventType = null;
            String str = readSegment();

            if (str != null) {
                eventType = new EventType(str);

                while ((str = readSegment()) != null) {
                    eventType.getEventTemplates().add(str);
                }

                result = new Event();
                result.setType(eventType);

                // Read event parameters.
                if (skipParameterSeparator()) {
                    Parameter param = readParameter();

                    while (param != null) {
                        if ("id".equals(param.getName())) {
                            result.setId(param.getValue());
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
