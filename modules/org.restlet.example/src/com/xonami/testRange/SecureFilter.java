package com.xonami.testRange;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;

public class SecureFilter extends Filter {
    private boolean doRedirect;

    private boolean enforceSecurity;

    public SecureFilter(Context cxt, Restlet next, boolean enforceSecurity,
            boolean doRedirect) {
        super(cxt);
        this.doRedirect = doRedirect;
        this.enforceSecurity = enforceSecurity;
        setNext(next);
    }

    public SecureFilter(Context cxt, Class<? extends ServerResource> next,
            boolean enforceSecurity, boolean doRedirect) {
        super(cxt);
        this.doRedirect = doRedirect;
        this.enforceSecurity = enforceSecurity;
        setNext(next);
    }

    public boolean isEnforceSecurity() {
        return enforceSecurity;
    }

    public void setEnforceSecurity(boolean enforceSecurity) {
        this.enforceSecurity = enforceSecurity;
    }

    public boolean isDoRedirect() {
        return doRedirect;
    }

    public void setDoRedirect(boolean doRedirect) {
        this.doRedirect = doRedirect;
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (!enforceSecurity)
            return CONTINUE;

        Form requestHeaders = (Form) request.getAttributes().get(
                "org.restlet.http.headers");

        if ((requestHeaders.getValues("x-forwarded-proto") != null)
                && (requestHeaders.getValues("x-forwarded-proto").indexOf(
                        "https") != 0)) {
            if (doRedirect) {
                String target = "https://"
                        + request.getHostRef().getHostDomain()
                        + request.getResourceRef().getPath();
                Redirector redirector = new Redirector(getContext(), target,
                        Redirector.MODE_CLIENT_SEE_OTHER);
                redirector.handle(request, response);
                return STOP;
            } else {
                response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return STOP;
            }
        }

        return CONTINUE;
    }

}