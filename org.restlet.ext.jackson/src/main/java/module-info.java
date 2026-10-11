/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"module", "requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.jackson {
    requires transitive org.restlet;
    requires tools.jackson.core;
    requires transitive tools.jackson.databind;
    requires transitive tools.jackson.dataformat.csv;
    requires tools.jackson.dataformat.smile;
    requires tools.jackson.dataformat.xml;
    requires tools.jackson.dataformat.yaml;
    requires org.codehaus.stax2;
    requires com.ctc.wstx;

    exports org.restlet.ext.jackson;

    provides org.restlet.engine.converter.ConverterHelper with
            org.restlet.ext.jackson.JacksonConverter;
}
