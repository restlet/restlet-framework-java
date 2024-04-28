/**
 * Copyright 2005-2020 Talend
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
 * https://restlet.com/open-source/
 *
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.platform.internal.agent.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * List of CallLog element, used for communication with Restlet Cloud to store
 * analytics about the Web API.
 *
 * @author Cyprien Quilici
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class CallLogs extends ArrayList<CallLog> implements Serializable {

    private static final long serialVersionUID = 1L;

    public CallLogs() {
    }

    public CallLogs(int size) {
        super(size);
    }
}
