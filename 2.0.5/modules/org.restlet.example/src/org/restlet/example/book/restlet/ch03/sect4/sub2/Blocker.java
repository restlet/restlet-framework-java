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

package org.restlet.example.book.restlet.ch03.sect4.sub2;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * Filter that blocks specific IP addresses.
 */
public class Blocker extends org.restlet.routing.Filter {

    /** The set of blocked IP addresses. */
    private final Set<String> blockedAddresses;

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     */
    public Blocker(Context context) {
        super(context);
        this.blockedAddresses = new CopyOnWriteArraySet<String>();
    }

    /**
     * Pre-processing method testing if the client IP address is in the set of
     * blocked addresses.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        int result = STOP;

        if (getBlockedAddresses()
                .contains(request.getClientInfo().getAddress())) {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                    "Your IP address was blocked");
        } else {
            result = CONTINUE;
        }

        return result;
    }

    /**
     * Returns the modifiable set of blocked IP addresses.
     * 
     * @return The modifiable set of blocked IP addresses.
     */
    public Set<String> getBlockedAddresses() {
        return blockedAddresses;
    }

}
