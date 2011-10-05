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

package org.restlet.ext.sip.internal;

import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderWriter;
import org.restlet.ext.sip.Subscription;

/**
 * Subscription state header writer.
 * 
 * @author Thierry Boileau
 */
public class SubscriptionWriter extends HeaderWriter<Subscription> {

    /**
     * Writes a subscription state.
     * 
     * @param subscription
     *            The subscription state.
     * @return The formatted subscription state.
     */
    public static String write(Subscription subscription) {
        return new SubscriptionWriter().append(subscription)
                .toString();
    }

    @Override
    public HeaderWriter<Subscription> append(Subscription value) {
        append(value.getValue());
        
        if (value.getReason() != null) {
            appendParameterSeparator();
            appendExtension("reason", value.getReason());
        }
        
        if (value.getExpires() > 0) {
            appendParameterSeparator();
            appendExtension("expires", Long.toString(value.getExpires()));
        }
        
        if (value.getRetryAfter() > 0) {
            appendParameterSeparator();
            appendExtension("retry-after", Long.toString(value.getRetryAfter()));
        }
        
        for (Parameter param : value.getParameters()) {
            appendParameterSeparator();
            appendExtension(param);
        }

        return this;
    }

}
