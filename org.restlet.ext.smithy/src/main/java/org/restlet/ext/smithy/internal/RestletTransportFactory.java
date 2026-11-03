/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy.internal;

import org.restlet.Context;
import org.restlet.ext.smithy.SmithyClientAdapter;
import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.ClientTransportFactory;
import software.amazon.smithy.java.client.core.MessageExchange;
import software.amazon.smithy.java.client.http.HttpMessageExchange;
import software.amazon.smithy.java.client.http.HttpTransportConfig;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.http.api.HttpRequest;
import software.amazon.smithy.java.http.api.HttpResponse;
import software.amazon.smithy.java.http.api.HttpVersion;

/**
 * Factory discovered by SDK clients built without a transport. Each transport owns its client.
 *
 * <p>Like the SDK's {@code java.net.http} transport, it reads the {@code httpConfig} settings:
 * {@code httpVersion}, {@code connectTimeoutMs} and {@code requestTimeoutMs}.
 */
public final class RestletTransportFactory
        implements ClientTransportFactory<HttpRequest, HttpResponse> {
    @Override
    public String name() {
        return "restlet";
    }

    @Override
    public byte priority() {
        // smithy-java 1.8.0 picks the lowest priority first, contrary to its Javadoc
        // (smithy-java#1438): -1 comes before the java.net.http transport (0).
        return -1;
    }

    @Override
    public MessageExchange<HttpRequest, HttpResponse> messageExchange() {
        return HttpMessageExchange.INSTANCE;
    }

    @Override
    public ClientTransport<HttpRequest, HttpResponse> createTransport(
            Document settings, Document pluginSettings) {
        var config =
                new HttpTransportConfig()
                        .fromDocument(
                                pluginSettings
                                        .asStringMap()
                                        .getOrDefault("httpConfig", Document.EMPTY_MAP));
        config.fromDocument(settings);
        var context = new Context();
        if (config.httpVersion() == HttpVersion.HTTP_2) {
            context.getParameters().set("httpClientTransportMode", "HTTP2");
        }
        if (config.connectTimeout() != null) {
            context.getParameters()
                    .set("connectTimeout", Long.toString(config.connectTimeout().toMillis()));
        }
        var transport = new SmithyClientAdapter(context);
        try {
            transport.setRequestTimeout(config.requestTimeout());
        } catch (RuntimeException e) {
            transport.close();
            throw e;
        }
        return transport;
    }
}
