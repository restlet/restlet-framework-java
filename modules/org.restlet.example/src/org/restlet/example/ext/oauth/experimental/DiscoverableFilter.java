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

package org.restlet.example.ext.oauth.experimental;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.Role;

/**
 * EXPERIMENTAL, and not part of the OAuth specification Implementation might
 * change in future releases.
 * 
 * @author Kristoffer Gronowski
 */
public class DiscoverableFilter extends Filter {
    public static final String CONTENT_TYPE = "application/restlet-oauth-discovery+json";

    public static final MediaType MEDIA_TYPE = new MediaType(CONTENT_TYPE);

    public static final String DISCOVERY_RESOURCE = "disc";

    public static final String WADL_RESOURCE = "wadl";

    private DiscoverableAuthServerInfo serverInfo;

    Logger log;

    public DiscoverableFilter(DiscoverableAuthServerInfo info) {
        serverInfo = info;
        log = getLogger();
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (request.getResourceRef().hasExtensions()
                && DISCOVERY_RESOURCE.equals(request.getResourceRef()
                        .getExtensions())) {
            createInfo(request, response);
            return Filter.STOP;
        } else if (request.getResourceRef().hasExtensions()
                && WADL_RESOURCE.equals(request.getResourceRef()
                        .getExtensions())) {
            createWadl(request, response);
            return Filter.STOP;
        }
        return super.beforeHandle(request, response);
    }

    private void createInfo(Request request, Response response) {
        JSONObject rep = new JSONObject();
        try {
            rep.put("auth_server", serverInfo.toJson());
            DiscoverableEndpointInfo ei = findEndpointInfo(request, response);
            if (ei != null) {
                rep.put("endpoint", ei.toJson());
            }
        } catch (JSONException e) {
            log.warning("Failed to produce JSON OAuth info");
        }
        JsonRepresentation repr = new JsonRepresentation(rep);
        repr.setMediaType(MEDIA_TYPE);
        response.setEntity(repr);

    }

    private void createWadl(Request request, Response response) {
        WadlServerResource wadl = null;

        log.info("Looking for a wadl resource");
        for (Restlet next = getNext(); next != null;) {
            if (next instanceof Finder) {
                Finder f = (Finder) next;
                ServerResource sr = f.find(request, response);

                if (sr instanceof WadlServerResource) {
                    wadl = (WadlServerResource) sr;
                }
                break;
                // next = ((Filter)next).getNext();
            } else if (next instanceof Filter) {
                next = ((Filter) next).getNext();
            } else if (next instanceof Router) { // TODO update other places...
                                                 // also add testcases...
                next = ((Router) next).getNext(request, response);
            } else {
                getLogger().warning(
                        "Unsupported class found in loop : "
                                + next.getClass().getCanonicalName());
                break;
            }
        }
        log.info("After wadl resource - " + wadl);
        // if( wadl != null ) {
        // ResourceInfo info = new ResourceInfo();
        // wadl.describe(request.getResourceRef().getPath(), info);
        // WadlRepresentation repr = new WadlRepresentation(info);
        // response.setEntity(repr);
        // }
    }

    private DiscoverableEndpointInfo findEndpointInfo(Request request,
            Response response) {
        DiscoverableResource scoped = null;
        Set<Method> methods = new HashSet<Method>();

        log.info("Looking for a scoped resource");
        for (Restlet next = getNext(); next != null;) {
            if (next instanceof Finder) {
                Finder f = (Finder) next;
                ServerResource sr = f.find(request, response);

                if (sr instanceof DiscoverableResource) {
                    scoped = (DiscoverableResource) sr;
                    List<AnnotationInfo> ai = AnnotationUtils.getAnnotations(sr
                            .getClass());
                    for (AnnotationInfo i : ai) {
                        methods.add(i.getRestletMethod());
                    }
                    Set<Method> am = sr.getAllowedMethods();
                    log.info("AllowedMethods size = " + am);
                }
                break;
                // next = ((Filter)next).getNext();
            } else if (next instanceof Filter) {
                next = ((Filter) next).getNext();
            } else {
                getLogger().warning(
                        "Unsupported class found in loop : "
                                + next.getClass().getCanonicalName());
                break;
            }
        }
        log.info("After scoped resource - " + scoped);
        if (scoped == null)
            return null; // could not find a scoped resource

        // Supported methods
        DiscoverableEndpointInfo result = new DiscoverableEndpointInfo(methods);
        // Required scopes
        List<Role> roles = scoped.getRoles(request.getResourceRef(),
                request.getMethod());
        if (roles != null)
            result.setScopes(roles);
        // Resource owner
        String owner = scoped.getOwner(request.getResourceRef());
        if (owner != null)
            result.setOwner(owner);

        // Scope set mutable
        // TODO

        // TODO WADL
        return result;
    }

}
