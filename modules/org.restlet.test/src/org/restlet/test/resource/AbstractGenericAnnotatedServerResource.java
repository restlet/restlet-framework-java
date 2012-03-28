package org.restlet.test.resource;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public abstract class AbstractGenericAnnotatedServerResource<R> extends ServerResource {

    @Post
    public abstract R addResponse(R representation);
}
