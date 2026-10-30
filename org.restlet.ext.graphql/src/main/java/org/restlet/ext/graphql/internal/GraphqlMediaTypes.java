/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql.internal;

import java.util.List;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;

/** Media types of the GraphQL over HTTP binding and their negotiation. */
public final class GraphqlMediaTypes {

    /** The {@code application/graphql-response+json} media type of the GraphQL over HTTP draft. */
    public static final MediaType GRAPHQL_RESPONSE_JSON =
            MediaType.register("application/graphql-response+json", "GraphQL response");

    private GraphqlMediaTypes() {}

    /**
     * Selects the response media type of a single result.
     *
     * <p>{@code application/graphql-response+json} is selected only when the client names it, with
     * a quality at least equal to that of {@code application/json}. Wildcards and a missing {@code
     * Accept} header select {@code application/json}, as the draft requires for legacy clients.
     *
     * @param accepted The accepted media types of the request.
     * @return The selected media type, or null if neither is acceptable.
     */
    public static MediaType negotiate(List<Preference<MediaType>> accepted) {
        if (accepted.isEmpty()) {
            return MediaType.APPLICATION_JSON;
        }
        float graphql = quality(accepted, GRAPHQL_RESPONSE_JSON, true);
        float json = quality(accepted, MediaType.APPLICATION_JSON, false);
        if (graphql > 0F && graphql >= json) {
            return GRAPHQL_RESPONSE_JSON;
        }
        return json > 0F ? MediaType.APPLICATION_JSON : null;
    }

    /**
     * Returns the quality with which a media type is accepted.
     *
     * @param accepted The accepted media types of the request.
     * @param type The media type.
     * @param exact True to ignore wildcard ranges.
     * @return The highest matching quality, or 0 if the type is not accepted.
     */
    public static float quality(
            List<Preference<MediaType>> accepted, MediaType type, boolean exact) {
        float result = 0F;
        for (Preference<MediaType> preference : accepted) {
            MediaType range = preference.getMetadata();
            boolean matches = exact ? range.equals(type, true) : range.includes(type, true);
            if (matches) {
                result = Math.max(result, preference.getQuality());
            }
        }
        return result;
    }
}
