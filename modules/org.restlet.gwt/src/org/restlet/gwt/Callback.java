package org.restlet.gwt;

import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;

public abstract class Callback {

    public abstract void onEvent(Request request, Response response);

}
