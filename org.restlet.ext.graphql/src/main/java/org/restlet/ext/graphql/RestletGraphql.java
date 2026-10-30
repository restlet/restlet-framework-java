/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

/**
 * Keys of the {@code graphql.GraphQLContext} entries set by {@link GraphqlServerAdapter} for each
 * operation. Data fetchers read them with {@code env.getGraphQlContext().get(key)}.
 *
 * <p>They are set after the execution input customizer runs, so neither the customizer nor the
 * {@code extensions} of a request can replace them.
 */
public final class RestletGraphql {

    /** Key of the Restlet {@link org.restlet.Request} being handled. */
    public static final String REQUEST = "org.restlet.ext.graphql.request";

    /**
     * Key of the {@link org.restlet.data.ClientInfo} of the request, including the authenticated
     * user and roles set by Restlet authenticators.
     */
    public static final String CLIENT_INFO = "org.restlet.ext.graphql.clientInfo";

    /**
     * Key of the payload of the {@code connection_init} message of a {@code graphql-transport-ws}
     * connection, a {@code Map<String, Object>}, absent if the client sent none.
     */
    public static final String CONNECTION_INIT_PAYLOAD =
            "org.restlet.ext.graphql.connectionInitPayload";

    private RestletGraphql() {}
}
