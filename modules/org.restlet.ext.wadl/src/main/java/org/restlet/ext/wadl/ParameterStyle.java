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

package org.restlet.ext.wadl;

/**
 * Enumerates the supported styles of parameters.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public enum ParameterStyle {

    HEADER, MATRIX, PLAIN, QUERY, TEMPLATE;

    @Override
    public String toString() {
        String result = null;
        if (equals(HEADER)) {
            result = "header";
        } else if (equals(MATRIX)) {
            result = "matrix";
        } else if (equals(PLAIN)) {
            result = "plain";
        } else if (equals(QUERY)) {
            result = "query";
        } else if (equals(TEMPLATE)) {
            result = "template";
        }

        return result;
    }

}
