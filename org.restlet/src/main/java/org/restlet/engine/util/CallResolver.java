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

package org.restlet.engine.util;

import static org.restlet.engine.util.DateUtils.FORMAT_RFC_1123;

import java.util.Date;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Reference;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.util.Resolver;

/**
 * Resolves variable values based on a request and a response.
 * 
 * @author Jerome Louvel
 */
public class CallResolver extends Resolver<Object> {

    /** The request to use as a model. */
    private final Request request;

    /** The response to use as a model. */
    private final Response response;

    /**
     * Constructor.
     * 
     * @param request
     *            The request to use as a model.
     * @param response
     *            The response to use as a model.
     */
    public CallResolver(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public Object resolve(String variableName) {
        // Check for a matching response attribute
        Object result = (this.response != null) ? this.response.getAttributes().get(variableName) : null;
        if (result != null) {
            return result;
        }

        // Check for a matching request attribute
        result = (this.request != null) ? this.request.getAttributes().get(variableName) : null;
        if (result != null) {
            return result;
        }

        if (variableName == null) {
            return null;
        }

        // Check for a matching request or response property
        if (this.request != null) {
            ChallengeResponse cr = this.request.getChallengeResponse();
            Representation entity = this.request.getEntity();

            switch (variableName) {
            case "c":
                return Boolean.toString(this.request.isConfidential());
            case "cia":
                return this.request.getClientInfo().getAddress();
            case "ciua":
                return this.request.getClientInfo().getUpstreamAddress();
            case "cig":
                return this.request.getClientInfo().getAgent();
            case "cri":
                return (cr != null) ? cr.getIdentifier() : null;
            case "crs":
                return (cr != null && cr.getScheme() != null) ? cr.getScheme().getTechnicalName() : null;
            case "d":
                return DateUtils.format(new Date(), FORMAT_RFC_1123.get(0));
            case "ecs":
                return (entity != null && entity.getCharacterSet() != null) ? entity.getCharacterSet().getName() : null;
            case "ee":
                return getEncodingsAsString(entity);
            case "eed":
                return getExpirationDateAsString(entity);
            case "el":
                return getLanguagesAsString(entity);
            case "emd":
                return getModificationDateAsString(entity);
            case "emt":
                return (entity != null && entity.getMediaType() != null) ? entity.getMediaType().getName() : null;
            case "es":
                return (entity != null && entity.getSize() != -1) ? Long.toString(entity.getSize()) : null;
            case "et":
                return (entity != null && entity.getTag() != null) ? entity.getTag().getName() : null;
            case "f":
                return getReferenceContent(variableName.substring(1), this.request.getReferrerRef());
            case "h":
                return getReferenceContent(variableName.substring(1), this.request.getHostRef());
            case "m":
                return (this.request.getMethod() != null) ? this.request.getMethod().getName() : null;
            case "p":
                return (this.request.getProtocol() != null) ? this.request.getProtocol().getName() : null;
            default:
                if (variableName.startsWith("o")) {
                    return getReferenceContent(variableName.substring(1), this.request.getRootRef());
                } else if (variableName.startsWith("r")) {
                    return getReferenceContent(variableName.substring(1), this.request.getResourceRef());
                }
                break;
            }
        }

        if (this.response != null) {
            Representation entity = this.response.getEntity();
            Status status = this.response.getStatus();
            ServerInfo serverInfo = this.response.getServerInfo();

            switch (variableName) {
            case "ECS":
                return (entity != null && entity.getCharacterSet() != null) ? entity.getCharacterSet().getName() : null;
            case "EE":
                return getEncodingsAsString(entity);
            case "EED":
                return getExpirationDateAsString(entity);
            case "EL":
                return getLanguagesAsString(entity);
            case "EMD":
                return getModificationDateAsString(entity);
            case "EMT":
                return (entity != null && entity.getMediaType() != null) ? entity.getMediaType().getName() : null;
            case "ES":
                return (entity != null && entity.getSize() != -1) ? Long.toString(entity.getSize()) : null;
            case "ET":
                return (entity != null && entity.getTag() != null) ? entity.getTag().getName() : null;
            case "S":
                return (status != null) ? Integer.toString(status.getCode()) : null;
            case "SIA":
                return serverInfo.getAddress();
            case "SIG":
                return serverInfo.getAgent();
            case "SIP":
                return (serverInfo.getPort() != -1) ? Integer.toString(serverInfo.getPort()) : null;
            default:
                if (variableName.startsWith("R")) {
                    return getReferenceContent(variableName.substring(1), this.response.getLocationRef());
                }
            }
        }

        return null;
    }

    /**
     * Returns the content corresponding to a reference property.
     * 
     * @param partName
     *            The variable sub-part name.
     * @param reference
     *            The reference to use as a model.
     * @return The content corresponding to a reference property.
     */
    private String getReferenceContent(String partName, Reference reference) {
        if (reference == null || partName == null) {
            return null;
        }

        switch (partName) {
        case "a":
            return reference.getAuthority();
        case "e":
            return reference.getRelativePart();
        case "f":
            return reference.getFragment();
        case "h":
            return reference.getHostIdentifier();
        case "i":
            return reference.getIdentifier();
        case "p":
            return reference.getPath();
        case "q":
            return reference.getQuery();
        case "r":
            return reference.getRemainingPart();
        default:
            if (partName.startsWith("b")) {
                return getReferenceContent(partName.substring(1), reference.getBaseRef());
            } else if (partName.startsWith("t")) {
                return getReferenceContent(partName.substring(1), reference.getTargetRef());
            } else if (partName.isEmpty()) {
                return reference.toString(false, false);
            }
            break;
        }

        return null;
    }

    private Object getModificationDateAsString(Representation entity) {
        return (entity != null && (entity.getModificationDate() != null)) ?
                DateUtils.format(entity.getModificationDate(), FORMAT_RFC_1123.get(0)) :
                null;
    }

    private Object getExpirationDateAsString(Representation entity) {
        return (entity != null && entity.getExpirationDate() != null) ?
                DateUtils.format(entity.getExpirationDate(), FORMAT_RFC_1123.get(0)) :
                null;
    }

    private Object getLanguagesAsString(Representation entity) {
        if (entity != null && !entity.getLanguages().isEmpty()) {
            final StringBuilder value = new StringBuilder();
            for (int i = 0; i < entity.getLanguages().size(); i++) {
                if (i > 0) {
                    value.append(", ");
                }
                value.append(entity.getLanguages().get(i).getName());
            }
            return value.toString();
        }
        return null;
    }

    private Object getEncodingsAsString(Representation entity) {
        if (entity != null && !entity.getEncodings().isEmpty()) {
            final StringBuilder value = new StringBuilder();
            for (int i = 0; i < entity.getEncodings().size(); i++) {
                if (i > 0) {
                    value.append(", ");
                }
                value.append(entity.getEncodings().get(i).getName());
            }
            return value.toString();
        }
        return null;
    }
}
