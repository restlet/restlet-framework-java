/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.osgi;

import org.restlet.Context;
import org.restlet.Restlet;

/**
 * This is a common interface for several of the providers. Users are not
 * expected to implement this interface, but instead implement one of the
 * specialized provider interfaces.
 * 
 * @author Bryan Hunt
 */
public interface RestletProvider {
    /**
     * 
     * @param context
     *            the Restlet application context
     * @return the node to be used as the inbound root of the handling chain
     */
    Restlet getInboundRoot(Context context);
}
