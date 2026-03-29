/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import java.util.List;
import org.restlet.data.CacheDirective;

/**
 * Cache directive header writer.
 *
 * @author Thierry Boileau
 */
public class CacheDirectiveWriter extends HeaderWriter<CacheDirective> {

    /**
     * Writes a list of cache directives with a comma separator.
     *
     * @param directives The list of cache directives.
     * @return The formatted list of cache directives.
     */
    public static String write(List<CacheDirective> directives) {
        return new CacheDirectiveWriter().append(directives).toString();
    }

    @Override
    public CacheDirectiveWriter append(CacheDirective directive) {
        appendExtension(directive);
        return this;
    }
}
