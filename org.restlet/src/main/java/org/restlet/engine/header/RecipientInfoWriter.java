/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
     * Creates a via header from the given list of recipient info.
     *
     * @param recipientsInfo The list of recipient info.
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
            throw new IllegalArgumentException("The protocol of a recipient can't be null");
        }

        return this;
    }
}
