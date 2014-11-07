package org.restlet.ext.apispark.internal.agent.resource;

import org.restlet.ext.apispark.internal.agent.bean.OperationsAuthorization;
import org.restlet.resource.Get;

public interface AuthorizationOperationsResource {

    @Get
    public OperationsAuthorization getAuthorizations();
}
