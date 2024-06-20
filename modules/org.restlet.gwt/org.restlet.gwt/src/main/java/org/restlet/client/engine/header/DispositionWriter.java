/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.header;

import org.restlet.client.data.Disposition;
import org.restlet.client.data.Parameter;

/**
 * Disposition header writer.
 * 
 * @author Thierry Boileau
 */
public class DispositionWriter extends HeaderWriter<Disposition> {

    /**
     * Formats a disposition.
     * 
     * @param disposition
     *            The disposition to format.
     * @return The formatted disposition.
     */
    public static String write(Disposition disposition) {
        return new DispositionWriter().append(disposition).toString();
    }

    @Override
    public DispositionWriter append(Disposition disposition) {
        if (Disposition.TYPE_NONE.equals(disposition.getType())
                || disposition.getType() == null) {
            return this;
        }

        append(disposition.getType());

        for (Parameter parameter : disposition.getParameters()) {
            append("; ");
            append(parameter.getName());
            append("=");

            if (HeaderUtils.isToken(parameter.getValue())) {
                append(parameter.getValue());
            } else {
                appendQuotedString(parameter.getValue());
            }
        }

        return this;
    }

}
