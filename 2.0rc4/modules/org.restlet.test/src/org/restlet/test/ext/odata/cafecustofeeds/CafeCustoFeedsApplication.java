package org.restlet.test.ext.odata.cafecustofeeds;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.routing.Router;

public class CafeCustoFeedsApplication extends Application {

    private static class MyClapRestlet extends Restlet {
        String file;

        boolean updatable;

        public MyClapRestlet(Context context, String file, boolean updatable) {
            super(context);
            this.file = file;
            this.updatable = updatable;
        }

        @Override
        public void handle(Request request, Response response) {
            if (Method.GET.equals(request.getMethod())) {
                Form form = request.getResourceRef().getQueryAsForm();
                String uri = "/"
                        + this.getClass().getPackage().getName().replace(".",
                                "/") + "/" + file;
                if (form.getFirstValue("$expand") != null) {
                    uri += form.getFirstValue("$expand");
                }
                Response r = getContext().getClientDispatcher().handle(
                        new Request(Method.GET, LocalReference
                                .createClapReference(LocalReference.CLAP_CLASS,
                                        uri + ".xml")));
                response.setEntity(r.getEntity());
                response.setStatus(r.getStatus());
            } else if (!updatable) {
                response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            }
        }
    }

    @Override
    public Restlet createInboundRoot() {
        getMetadataService().setDefaultCharacterSet(CharacterSet.ISO_8859_1);
        getConnectorService().getClientProtocols().add(Protocol.CLAP);
        Router router = new Router(getContext());
        router.attach("/$metadata", new MyClapRestlet(getContext(), "metadata",
                false));
        router
                .attach("/Cafes", new MyClapRestlet(getContext(), "cafes",
                        false));
        router.attach("/Contacts('1')", new MyClapRestlet(getContext(),
                "contact1", false));
        return router;
    }

}
