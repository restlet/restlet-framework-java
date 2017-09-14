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

import org.restlet.data.Expectation;
import org.restlet.data.Parameter;

/**
 * Expectation header writer.
 * 
 * @author Jerome Louvel
 */
public class ExpectationWriter extends HeaderWriter<Expectation> {

    /**
     * Writes a list of expectations with a comma separator.
     * 
     * @param expectations
     *            The list of expectations.
     * @return The formatted list of expectations.
     */
    public static String write(List<Expectation> expectations) {
        return new ExpectationWriter().append(expectations).toString();
    }

    @Override
    public ExpectationWriter append(Expectation expectation) {
        if ((expectation.getName() != null)
                && (expectation.getName().length() > 0)) {
            appendExtension(expectation);

            if (!expectation.getParameters().isEmpty()) {
                for (Parameter param : expectation.getParameters()) {
                    appendParameterSeparator();
                    appendExtension(param);
                }
            }
        }

        return this;
    }

}
