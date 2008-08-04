/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.restlet.resource.Representation;

/**
 * Velocity resource loader based on a static map of representations or on a
 * default representation.
 * 
 * @author Jerome Louvel
 */
public class RepresentationResourceLoader extends ResourceLoader {

    private static final Map<String, Representation> store = new ConcurrentHashMap<String, Representation>();

    public static Map<String, Representation> getStore() {
        return store;
    }

    /** The default representation to load. */
    private final Representation defaultRepresentation;

    /**
     * Constructeur.
     * 
     * @param defaultRepresentation
     *            The default representation to use.
     */
    public RepresentationResourceLoader(Representation defaultRepresentation) {
        this.defaultRepresentation = defaultRepresentation;
    }

    @Override
    public long getLastModified(Resource resource) {
        final Representation original = getStore().get(resource.getName());
        return (original != null) ? original.getModificationDate().getTime()
                : 0;
    }

    @Override
    public InputStream getResourceStream(String name)
            throws ResourceNotFoundException {
        InputStream result = null;

        try {
            Representation resultRepresentation = getStore().get(name);

            if (resultRepresentation == null) {
                resultRepresentation = this.defaultRepresentation;

                if (resultRepresentation == null) {
                    throw new ResourceNotFoundException(
                            "Could not locate resource '" + name + "'");
                } else {
                    result = resultRepresentation.getStream();
                }
            } else {
                result = resultRepresentation.getStream();
            }
        } catch (final IOException ioe) {
            throw new ResourceNotFoundException(ioe);
        }

        return result;
    }

    @Override
    public void init(ExtendedProperties configuration) {

    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return getLastModified(resource) != resource.getLastModified();
    }

}
