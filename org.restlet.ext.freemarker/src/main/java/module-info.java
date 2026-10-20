/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.freemarker {
    requires transitive org.restlet;
    requires transitive freemarker;

    exports org.restlet.ext.freemarker;

    provides org.restlet.engine.converter.ConverterHelper with
            org.restlet.ext.freemarker.FreemarkerConverter;
}
