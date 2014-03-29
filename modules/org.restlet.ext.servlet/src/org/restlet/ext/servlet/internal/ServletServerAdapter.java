package org.restlet.ext.servlet.internal;

import java.util.Enumeration;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.adapter.ServerAdapter;
import org.restlet.engine.adapter.ServerCall;

public class ServletServerAdapter extends ServerAdapter {

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
                result.getAttributes().put(attributeName,
                        servletCall.getRequest().getAttribute(attributeName));
            }
        }

        return result;
    }

}
