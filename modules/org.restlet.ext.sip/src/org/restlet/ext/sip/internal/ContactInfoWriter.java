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

import java.util.List;

import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderWriter;
import org.restlet.engine.header.PreferenceWriter;
import org.restlet.ext.sip.ContactInfo;

/**
 * Contact header writer.
 * 
 * @author Thierry Boileau
 */
public class ContactInfoWriter extends HeaderWriter<ContactInfo> {

    /**
     * Writes a contact.
     * 
     * @param contact
     *            The contact.
     * @return The formatted contact.
     */
    public static String write(ContactInfo contact) {
        return new ContactInfoWriter().append(contact).toString();
    }

    /**
     * Writes a list of contacts with a comma separator.
     * 
     * @param contacts
     *            The list of contacts.
     * @return The formatted list of contacts.
     */
    public static String write(List<ContactInfo> contacts) {
        return new ContactInfoWriter().append(contacts).toString();
    }

    @Override
    public HeaderWriter<ContactInfo> append(ContactInfo contact) {
        if (contact != null) {
            if (contact.getDisplayName() != null) {
                appendQuotedString(contact.getDisplayName());
                append(" <");
                append(contact.getReference().toString());
                append("> ");
            }
            
            if (!contact.getParameters().isEmpty()) {
                if (contact.getQuality() < 1F) {
                    appendParameterSeparator();
                    append("q=");
                    appendQuality(contact.getQuality());
                }
                
                if (contact.getExpires() != null) {
                    appendParameterSeparator();
                    append("expires=");
                    append(contact.getExpires());
                }
                
                for (Parameter param : contact.getParameters()) {
                    appendParameterSeparator();
                    appendExtension(param);
                }
            }
        }

        return this;
    }

    /**
     * Formats a quality value.<br>
     * If the quality is invalid, an IllegalArgumentException is thrown.
     * 
     * @param quality
     *            The quality value as a float.
     * @return This writer.
     */
    public ContactInfoWriter appendQuality(float quality) {
        if (!PreferenceWriter.isValidQuality(quality)) {
            throw new IllegalArgumentException(
                    "Invalid quality value detected. Value must be between 0 and 1.");
        }

        // [ifndef gwt]
        java.text.NumberFormat formatter = java.text.NumberFormat
                .getNumberInstance(java.util.Locale.US);
        formatter.setMaximumFractionDigits(2);
        append(formatter.format(quality));
        // [enddef]

        return this;
    }

}
