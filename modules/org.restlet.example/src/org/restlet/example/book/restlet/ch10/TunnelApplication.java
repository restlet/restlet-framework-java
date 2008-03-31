package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;

public class TunnelApplication extends Application {

    public TunnelApplication(Context parentContext) {
        super(parentContext);
        // Update the default value of the method parameter
        this.getTunnelService().setMethodParameter("_method");
    }

    @Override
    public Restlet createRoot() {
        Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                StringBuilder builder = new StringBuilder();
                builder.append("<html><body>");

                if (Method.GET.equals(request.getMethod())) {
                    // Append a new "method" parameter to the query part of the
                    // resource's reference
                    Reference ref = new Reference(request.getResourceRef());
                    ref.addQueryParameter("_method", "put");

                    // Build a POST form with the updated action
                    builder.append("<form method=\"POST\"");
                    builder.append("action=\"");
                    builder.append(ref.getIdentifier());
                    builder.append("\">");
                    builder.append("<input type=\"submit\">");
                    builder.append("</form>");
                } else {
                    builder.append("request method is ");
                    builder.append(request.getMethod());
                    builder.append(".");
                }
                builder.append("</body></html>");

                response.setStatus(Status.SUCCESS_OK);
                response.setEntity(new StringRepresentation(builder.toString(),
                        MediaType.TEXT_HTML));
            }
        };

        return restlet;
    }
}
