package org.restlet.example.book.restlet.ch10;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class TweakingClientFilter extends Filter {

    public TweakingClientFilter(Context context, Restlet next) {
        super(context, next);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        final String agent = request.getClientInfo().getAgent();

        if (agent.startsWith("Mozilla/5.0")) {
            // Adjust the client preferences
            final Preference<MediaType> preference = new Preference<MediaType>(
                    MediaType.TEXT_HTML);
            request.getClientInfo().getAcceptedMediaTypes().add(0, preference);
        }

        return super.beforeHandle(request, response);
    }

}
