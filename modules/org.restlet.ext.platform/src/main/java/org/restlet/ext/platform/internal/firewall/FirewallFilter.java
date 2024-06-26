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

package org.restlet.ext.platform.internal.firewall;

import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.platform.internal.firewall.rule.FirewallRule;
import org.restlet.routing.Filter;

/**
 * Filter that controls the incoming requests by applying a set of rules.
 *
 * @author Guillaume Blondeau
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class FirewallFilter extends Filter {

    /** The list of associated {@link FirewallRule}. */
    protected final List<FirewallRule> rules;

    /**
     * Constructor.
     *
     * @param context
     *            The context.
     * @param rules
     *            The list of associated {@link FirewallRule}.
     */
    public FirewallFilter(Context context, List<FirewallRule> rules) {
        super(context);
        this.rules = rules;
    }

    /**
     * Invokes each {@link FirewallRule#afterHandle(Request, Response)} method.
     */
    @Override
    public void afterHandle(Request request, Response response) {
        for (FirewallRule rule : rules) {
            rule.afterHandle(request, response);
        }
    }

    /**
     * Applies each rules to the incoming request.
     */
    @Override
    public int beforeHandle(Request request, Response response) {
        int result = Filter.CONTINUE;

        for (FirewallRule rule : rules) {
            int value = rule.beforeHandle(request, response);
            if (value != Filter.CONTINUE) {
                return value;
            }
            result = value;
        }

        return result;
    }

}
