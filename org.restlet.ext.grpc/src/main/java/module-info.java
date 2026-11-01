/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.grpc {
    requires transitive org.restlet;
    requires transitive io.grpc;
    requires io.grpc.internal;
    requires com.google.common;

    exports org.restlet.ext.grpc;
}
