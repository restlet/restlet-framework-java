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

import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderWriter;
import org.restlet.ext.sip.Availability;

/**
 * Retry-after header writer.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class AvailabilityWriter extends HeaderWriter<Availability> {

    /**
     * Writes an availability instance.
     * 
     * @param availability
     *            The availability instance.
     * @return The formatted contact.
     */
    public static String write(Availability availability) {
        return new AvailabilityWriter().append(availability).toString();
    }

    /**
     * Writes a list of availability instances with a comma separator.
     * 
     * @param availabilities
     *            The list of availabilities.
     * @return The formatted list of availabilities.
     */
    public static String write(List<Availability> availabilities) {
        return new AvailabilityWriter().append(availabilities).toString();
    }

    @Override
    public HeaderWriter<Availability> append(Availability availability) {
        if (availability != null) {
            append(availability.getDelay());

            if (availability.getComment() != null) {
                appendSpace();
                appendComment(availability.getComment());
                appendSpace();
            }

            if (availability.getDuration() > 0) {
                appendParameterSeparator();
                append("duration=");
                append(availability.getDuration());
            }

            for (Parameter param : availability.getParameters()) {
                appendParameterSeparator();
                appendExtension(param);
            }
        }

        return this;
    }

}
