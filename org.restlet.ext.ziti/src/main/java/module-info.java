/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.ziti {
    requires transitive org.restlet;
    // The OpenZiti SDK jars are not modular and declare no Automatic-Module-Name.
    requires transitive ziti;
    requires edge.api;
    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
    requires io.netty.resolver;

    exports org.restlet.ext.ziti;
}
