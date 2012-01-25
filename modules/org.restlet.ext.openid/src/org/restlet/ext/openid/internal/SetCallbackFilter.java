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

package org.restlet.ext.openid.internal;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.Reference;
import org.restlet.routing.Filter;

/**
 * Stores a identity lookup result in a cache so that multiple lookups from the
 * same client can be avoided.
 * 
 * @author Kristoffer Gronowski
 */
@Deprecated
public class SetCallbackFilter extends Filter {
    /** Internal cache. */
    private CacheLookup cache = null;

    public SetCallbackFilter() {
    } // No caching used

    // Check for cached id before invoking
    public SetCallbackFilter(CacheLookup cache) {
        this.cache = cache;
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        String cb = request.getOriginalRef().getQueryAsForm()
                .getFirstValue(CallbackCacheFilter.EXTERNAL_SERVER_COOKIE);

        if (cb != null && cb.length() > 0 && response.getStatus().isSuccess()) {
            Reference ref = new Reference(cb);

            if (ref.getQueryAsForm().removeFirst("internal")) {
                getLogger().fine("OpenID - setting internal cb cookie = " + cb);
                CookieSetting cs = new CookieSetting(
                        CallbackCacheFilter.INTERNAL_SERVER_COOKIE, cb);
                // cs.setAccessRestricted(true);
                // cs.setSecure(true);
                response.getCookieSettings().add(cs);

            } else {
                getLogger().fine("OpenID - setting external cb cookie = " + cb);
                CookieSetting cs = new CookieSetting(
                        CallbackCacheFilter.EXTERNAL_SERVER_COOKIE, cb);
                // cs.setAccessRestricted(true);
                // cs.setSecure(true);
                response.getCookieSettings().add(cs);
            }
        }

        return super.beforeHandle(request, response);
    }

    @Override
    protected int doHandle(Request request, Response response) {
        if (cache != null) { // Let's try to find it
            boolean found = cache.handleCached(request, response);

            if (found)
                return Filter.STOP;
        }

        return super.doHandle(request, response);
    }
}
