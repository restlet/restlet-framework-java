/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import java.util.Map;
import org.restlet.util.Resolver;

/**
 * Resolves variable values based on a map.
 *
 * @author Jerome Louvel
 */
public class MapResolver extends Resolver<Object> {

    /** The variables to use when formatting. */
    private final Map<String, ?> map;

    /**
     * Constructor.
     *
     * @param map The variables to use when formatting.
     */
    public MapResolver(Map<String, ?> map) {
        this.map = map;
    }

    @Override
    public Object resolve(String variableName) {
        return this.map.get(variableName);
    }
}
