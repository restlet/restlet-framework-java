/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.servlet.internal;

import java.util.Enumeration;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.adapter.ServerAdapter;
import org.restlet.engine.adapter.ServerCall;

/**
 * Default server adapter from Servlet calls to Restlet calls. This class is
 * used by the {@code ServerServlet} to ensure that Servlet specific concepts
 * are properly transfered to Restlet.<br>
 * Especially, it makes sure that the "jsessionid" matrix parameter is removed
 * from the resource's reference, as this may interfer with the routing process,
 * and because this parameter is useless for Restlet-based applications.<br>
 * <br>
 * it also copies the Servlet's request attributes into the Restlet's request
 * attributes map.
 * 
 * @author Jeremy Gustie
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class ServletServerAdapter extends ServerAdapter {

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use.
     */
    public ServletServerAdapter(Context context) {
        super(context);
    }

    @Override
    public HttpRequest toRequest(ServerCall httpCall) {
        final HttpRequest result = super.toRequest(httpCall);

        // Remove the Servlet API "jsessionid" matrix parameter
        Form matrixForm = result.getResourceRef().getMatrixAsForm();
        if (matrixForm.removeAll("jsessionid", true)) {
            String lastSegment = result.getResourceRef().getLastSegment();
            final int matrixIndex = lastSegment.indexOf(';');
            if (matrixForm.isEmpty()) {
                // No matrix left
                lastSegment = lastSegment.substring(0, matrixIndex);
            } else {
                // Add the remaining matrix parameters back in
                lastSegment = lastSegment.substring(0, matrixIndex + 1)
                        + matrixForm.getMatrixString();
            }
            result.getResourceRef().setLastSegment(lastSegment);
        }

        if (httpCall instanceof ServletCall) {
            ServletCall servletCall = (ServletCall) httpCall;

            // Copy all Servlet's request attributes
            String attributeName;
            for (final Enumeration<String> namesEnum = servletCall.getRequest()
                    .getAttributeNames(); namesEnum.hasMoreElements();) {
                attributeName = namesEnum.nextElement();
                Object attribute = servletCall.getRequest().getAttribute(attributeName);
                if (attribute != null) {
                    result.getAttributes().put(attributeName, attribute);
                }
            }
        }

        return result;
    }

}
