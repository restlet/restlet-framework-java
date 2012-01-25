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

package org.restlet.ext.oauth.internal;

import java.net.URI;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.CookieSetting;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;

/**
 * ClientResource that will copy any server cookies to client cookies in
 * redirects. Default behavior is to copy cookies
 * 
 * @author Martin Svensson
 */
public class CookieCopyClientResource extends ClientResource {

    private volatile boolean copyCookies = true;

    public CookieCopyClientResource() {
        super();
    }

    public CookieCopyClientResource(ClientResource resource) {
        super(resource);
    }

    public CookieCopyClientResource(Context context, Method method,
            Reference reference) {
        super(context, method, reference);
    }

    public CookieCopyClientResource(Context context, Method method, String uri) {
        super(context, method, uri);
    }

    public CookieCopyClientResource(Context context, Method method, URI uri) {
        super(context, method, uri);
    }

    public CookieCopyClientResource(Context context, Reference reference) {
        super(context, reference);
    }

    public CookieCopyClientResource(Context context, Request request,
            Response response) {
        super(context, request, response);
    }

    public CookieCopyClientResource(Context context, String uri) {
        super(context, uri);
    }

    public CookieCopyClientResource(Context context, URI uri) {
        super(context, uri);
    }

    public CookieCopyClientResource(Method method, Reference reference) {
        super(method, reference);
    }

    public CookieCopyClientResource(Method method, String uri) {
        super(method, uri);
    }

    public CookieCopyClientResource(Method method, URI uri) {
        super(method, uri);
    }

    public CookieCopyClientResource(Reference reference) {
        super(reference);
    }

    public CookieCopyClientResource(Request request, Response response) {
        super(request, response);
    }

    public CookieCopyClientResource(String uri) {
        super(uri);
    }

    public CookieCopyClientResource(URI uri) {
        super(uri);
    }

    public boolean getCopyCookies() {
        return this.copyCookies;
    }

    @Override
    protected void redirect(Request request, Response response,
            List<Reference> references, int retryAttempt, Uniform next) {
        // TODO Auto-generated method stub
        if (retryAttempt < 0) {
            for (CookieSetting cs : response.getCookieSettings()) {
                request.getCookies().add(cs.getName(), cs.getValue());
            }
        }
        super.redirect(request, response, references, retryAttempt, next);
    }

    public void setCopyCookies(boolean copyCookies) {
        this.copyCookies = copyCookies;
    }

}
