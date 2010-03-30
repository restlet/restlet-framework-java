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

package org.restlet.ext.sip.internal;

import java.util.List;

import org.restlet.data.Parameter;
import org.restlet.engine.http.header.HeaderWriter;
import org.restlet.ext.sip.Address;

/**
 * Address like header writer.
 * 
 * @author Thierry Boileau
 */
public class AddressWriter extends HeaderWriter<Address> {

    /**
     * Writes an address.
     * 
     * @param address
     *            The address.
     * @return The formatted address.
     */
    public static String write(Address address) {
        return new AddressWriter().append(address).toString();
    }

    /**
     * Writes a list of addresses with a comma separator.
     * 
     * @param addresses
     *            The list of addresses.
     * @return The formatted list of addresses.
     */
    public static String write(List<Address> addresses) {
        return new AddressWriter().append(addresses).toString();
    }

    @Override
    public HeaderWriter<Address> append(Address address) {
        if (address != null) {
            if (address.getDisplayName() != null) {
                appendQuotedString(address.getDisplayName());
                append(" <");
                append(address.getReference().toString());
                append("> ");
            }
            if (!address.getParameters().isEmpty()) {
                for (Parameter param : address.getParameters()) {
                    appendParameterSeparator();
                    appendExtension(param);
                }
            }
        }

        return this;
    }

}
