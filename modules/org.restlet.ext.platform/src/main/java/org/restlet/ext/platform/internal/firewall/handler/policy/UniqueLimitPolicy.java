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
 * https://restlet.talend.com
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.platform.internal.firewall.handler.policy;

import org.restlet.Request;

/**
 * Defines a single limit.
 *
 * @author Guillaume Blondeau
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class UniqueLimitPolicy extends LimitPolicy {

    /** The single limit. */
    private int limit;

    /**
     * Constructor.
     *
     * @param limit
     *            The associated limit.
     */
    public UniqueLimitPolicy(int limit) {
        this.limit = limit;
    }

    @Override
    public int getLimit(Request request, String countedValue) {
        return limit;
    }

}
