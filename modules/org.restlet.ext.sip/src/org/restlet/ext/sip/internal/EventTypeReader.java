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

import static org.restlet.engine.header.HeaderUtils.isComma;
import static org.restlet.engine.header.HeaderUtils.isSpace;

import java.io.IOException;

import org.restlet.engine.header.HeaderReader;
import org.restlet.ext.sip.EventType;

/**
 * Event type like header reader.
 * 
 * @author Thierry Boileau
 */
public class EventTypeReader extends HeaderReader<EventType> {

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public EventTypeReader(String header) {
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
    public EventType readValue() throws IOException {
        EventType result = null;
        skipSpaces();
        if (peek() != -1) {
            String str = readSegment();
            if (str != null) {
                result = new EventType(str);
                while ((str = readSegment()) != null) {
                    result.getEventTemplates().add(str);
                }
            }
        }

        return result;
    }

}
