/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"module", "requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.spring {
    requires transitive org.restlet;
    requires transitive spring.beans;
    requires transitive spring.context;
    requires transitive spring.core;
    requires transitive org.jspecify;

    exports org.restlet.ext.spring;
}
