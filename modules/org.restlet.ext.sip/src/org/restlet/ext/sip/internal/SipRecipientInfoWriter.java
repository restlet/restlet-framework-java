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
import org.restlet.ext.sip.SipRecipientInfo;

/**
 * Via header writer.
 * 
 * @author Thierry Boileau
 */
public class SipRecipientInfoWriter extends HeaderWriter<SipRecipientInfo> {

    /**
     * Creates a via header from the given recipients info.
     * 
     * @param recipientsInfo
     *            The recipients info.
     * @return Returns the Via header.
     */
    public static String write(List<SipRecipientInfo> recipientsInfo) {
        return new SipRecipientInfoWriter().append(recipientsInfo).toString();
    }

    /**
     * Creates a via header from the given recipient info.
     * 
     * @param recipientInfo
     *            The recipient info.
     * @return Returns the Via header.
     */
    public static String write(SipRecipientInfo recipientInfo) {
        return new SipRecipientInfoWriter().append(recipientInfo).toString();
    }

    @Override
    public SipRecipientInfoWriter append(SipRecipientInfo recipientInfo) {
        if (recipientInfo.getProtocol() != null) {
            appendToken(recipientInfo.getProtocol().getName());
            append('/');
            appendToken(recipientInfo.getProtocol().getVersion());

            if (recipientInfo.getTransport() != null) {
                append('/');
                appendToken(recipientInfo.getTransport());
            }

            appendSpace();

            if (recipientInfo.getName() != null) {
                append(recipientInfo.getName());

                if (!recipientInfo.getParameters().isEmpty()) {
                    for (Parameter param : recipientInfo.getParameters()) {
                        appendParameterSeparator();
                        appendExtension(param);
                    }
                }

                if (recipientInfo.getComment() != null) {
                    appendSpace();
                    appendComment(recipientInfo.getComment());
                }
            } else {
                throw new IllegalArgumentException(
                        "The name (host or pseudonym) of a recipient can't be null");
            }
        } else {
            throw new IllegalArgumentException(
                    "The protocol of a recipient can't be null");
        }

        return this;
    }
}
