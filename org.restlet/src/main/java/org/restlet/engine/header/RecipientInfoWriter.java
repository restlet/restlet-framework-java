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

package org.restlet.engine.header;

import java.util.List;

import org.restlet.data.RecipientInfo;

/**
 * Recipient info header writer.
 * 
 * @author Jerome Louvel
 */
public class RecipientInfoWriter extends HeaderWriter<RecipientInfo> {

    /**
     * Creates a via header from the given recipients info.
     * 
     * @param recipientsInfo
     *            The recipients info.
     * @return Returns the Via header.
     */
    public static String write(List<RecipientInfo> recipientsInfo) {
        return new RecipientInfoWriter().append(recipientsInfo).toString();
    }

    @Override
    public RecipientInfoWriter append(RecipientInfo recipientInfo) {
        if (recipientInfo.getProtocol() != null) {
            appendToken(recipientInfo.getProtocol().getName());
            append('/');
            appendToken(recipientInfo.getProtocol().getVersion());
            appendSpace();

            if (recipientInfo.getName() != null) {
                append(recipientInfo.getName());

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
