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

import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderWriter;
import org.restlet.ext.sip.Subscription;

/**
 * Subscription state header writer.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public class SubscriptionWriter extends HeaderWriter<Subscription> {

    /**
     * Writes a subscription state.
     * 
     * @param subscription
     *            The subscription state.
     * @return The formatted subscription state.
     */
    public static String write(Subscription subscription) {
        return new SubscriptionWriter().append(subscription).toString();
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
