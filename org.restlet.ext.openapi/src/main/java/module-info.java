/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.openapi {
    requires transitive org.restlet;
    requires org.apache.commons.lang3;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires io.swagger.v3.oas.integration;
    requires io.swagger.v3.oas.annotations;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports org.restlet.ext.openapi;
}
