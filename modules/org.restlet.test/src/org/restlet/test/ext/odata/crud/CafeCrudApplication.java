
package org.restlet.test.ext.odata.crud;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.LocalReference;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.routing.Router;

/**
 * Sample application that simulates the CUD operation on entities.
 * 
 */
@SuppressWarnings("unused")
public class CafeCrudApplication extends Application {

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
				String uri = "/"
						+ this.getClass().getPackage().getName()
								.replace(".", "/") + "/" + file;

				Response r = getContext().getClientDispatcher().handle(
						new Request(Method.GET, LocalReference
								.createClapReference(LocalReference.CLAP_CLASS,
										uri + ".xml")));
				response.setEntity(r.getEntity());
				response.setStatus(r.getStatus());

			} else if (Method.POST.equals(request.getMethod()) || Method.PUT.equals(request.getMethod())) {
				String rep=null;
				try {
					rep = request.getEntity().getText();
				} catch (IOException e) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				}
				if(null != rep && !rep.isEmpty()){
					response.setStatus(Status.SUCCESS_OK);
				}
				
			} else if (Method.DELETE.equals(request.getMethod())) {
				response.setStatus(Status.SUCCESS_NO_CONTENT);

			}

		}
	}

	@Override
	public Restlet createInboundRoot() {
		getMetadataService().setDefaultCharacterSet(CharacterSet.ISO_8859_1);
		getConnectorService().getClientProtocols().add(Protocol.CLAP);
		Router router = new Router(getContext());

		router.attach("/$metadata", new MyClapRestlet(getContext(), "metadata",
				true));
		router.attach("/Cafes", new MyClapRestlet(getContext(), "cafes", true));
		router.attach("/Cafes('30')", new MyClapRestlet(getContext(),
				"cafesUpdated", true));

		return router;
	}

}
