/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(ClientResource resource) {
        super(resource);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Context context, Method method,
            Reference reference) {
        super(context, method, reference);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Context context, Method method, String uri) {
        super(context, method, uri);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Context context, Method method, URI uri) {
        super(context, method, uri);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Context context, Reference reference) {
        super(context, reference);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Context context, String uri) {
        super(context, uri);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Context context, URI uri) {
        super(context, uri);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Method method, Reference reference) {
        super(method, reference);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Method method, String uri) {
        super(method, uri);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Method method, URI uri) {
        super(method, uri);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Reference reference) {
        super(reference);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(Request request, Response response) {
        super(request, response);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(String uri) {
        super(uri);
        // TODO Auto-generated constructor stub
    }

    public CookieCopyClientResource(URI uri) {
        super(uri);
        // TODO Auto-generated constructor stub
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

    // TODO: CHANGE WHEN NEW SNAPSHOT IS AVAIL and override redirect() instead
    /*
     * @Override protected void handle(Request request, Response response,
     * List<Reference> references, int retryAttempt, Uniform next) { if
     * (copyCookies && isFollowingRedirects() &&
     * response.getStatus().isRedirection() && (response.getLocationRef() !=
     * null)) {
     * 
     * 
     * boolean doRedirection = false;
     * 
     * if (request.getMethod().isSafe()) { doRedirection = true; } else { if
     * (Status.REDIRECTION_SEE_OTHER.equals(response .getStatus())) { // The
     * user agent is redirected using the GET method
     * //request.setMethod(Method.GET); //request.setEntity(null); doRedirection
     * = true; } else if (Status.REDIRECTION_USE_PROXY.equals(response
     * .getStatus())) { doRedirection = true; } } if (doRedirection){
     * for(CookieSetting cs : response.getCookieSettings()){
     * request.getCookies().add(cs.getName(), cs.getValue()); } } }
     * super.handle(request, response, references, retryAttempt, next); }
     */

    public void setCopyCookies(boolean copyCookies) {
        this.copyCookies = copyCookies;
    }

}
