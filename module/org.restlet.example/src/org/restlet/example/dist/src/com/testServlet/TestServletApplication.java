package com.testServlet;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

public class TestServletApplication extends Application {

    public static void main(String[] args) throws Exception {
        // Create a component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);

        TestServletApplication application = new TestServletApplication(
                component.getContext());

        // Attach the application to the component and start it
        component.getDefaultHost().attach("", application);
        component.start();
    }

    public TestServletApplication() {
        super();
    }

    public TestServletApplication(Context context) {
        super(context);
    }

    @Override
    public Restlet createRoot() {

        Restlet restlet = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append("<html>");
                stringBuilder
                        .append("<head><title>Sample Application Servlet Page</title></head>");
                stringBuilder.append("<body bgcolor=white>");

                stringBuilder.append("<table border=\"0\">");
                stringBuilder.append("<tr>");
                stringBuilder.append("<td>");
                stringBuilder.append("<h1>Sample Application Restlet</h1>");
                stringBuilder
                        .append("This is the output of a restlet that is part of");
                stringBuilder
                        .append("the testServlet application.  It displays the");
                stringBuilder
                        .append("request headers from the request we are currently");
                stringBuilder.append("processing.");
                stringBuilder.append("</td>");
                stringBuilder.append("</tr>");
                stringBuilder.append("</table>");

                stringBuilder.append("<table border=\"0\" width=\"100%\">");
                stringBuilder.append("<tr>");
                stringBuilder
                        .append("  <th align=\"right\">Accepted character sets :</th>");
                stringBuilder.append("  <td>"
                        + request.getClientInfo().getAcceptedCharacterSets()
                        + "</td>");
                stringBuilder.append("</tr>");
                stringBuilder.append("<tr>");
                stringBuilder
                        .append("  <th align=\"right\">Accepted encodings :</th>");
                stringBuilder.append("  <td>"
                        + request.getClientInfo().getAcceptedEncodings()
                        + "</td>");
                stringBuilder.append("</tr>");

                stringBuilder.append("<tr>");
                stringBuilder
                        .append("  <th align=\"right\">Accepted media types :</th>");
                stringBuilder.append("  <td>"
                        + request.getClientInfo().getAcceptedMediaTypes()
                        + "</td>");
                stringBuilder.append("</tr>");

                stringBuilder.append("<tr>");
                stringBuilder.append("  <th align=\"right\">Address :</th>");
                stringBuilder.append("  <td>"
                        + request.getClientInfo().getAddress() + "</td>");
                stringBuilder.append("</tr>");

                stringBuilder.append("<tr>");
                stringBuilder.append("  <th align=\"right\">Agent :</th>");
                stringBuilder.append("  <td>"
                        + request.getClientInfo().getAgent() + "</td>");
                stringBuilder.append("</tr>");

                stringBuilder.append("</table>");

                stringBuilder.append("</body>");
                stringBuilder.append("</html>");

                response.setEntity(new StringRepresentation(stringBuilder
                        .toString(), MediaType.TEXT_HTML));

            }
        };

        return restlet;
    }

}
