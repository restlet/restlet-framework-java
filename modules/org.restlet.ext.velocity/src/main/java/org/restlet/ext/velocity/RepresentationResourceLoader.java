/**
 * Copyright 2005-2019 Talend
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.velocity;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;
import org.restlet.representation.Representation;

/**
 * Velocity resource loader based on a static map of representations or on a
 * default representation.
 * 
 * @author Jerome Louvel
 */
public class RepresentationResourceLoader extends ResourceLoader {

    /** The cache of template representations. */
    private static final Map<String, Representation> store = new ConcurrentHashMap<String, Representation>();

    /**
     * Returns the cache of template representations.
     * 
     * @return The cache of template representations.
     */
    public static Map<String, Representation> getStore() {
        return store;
    }

    /** The default representation to load. */
    private final Representation defaultRepresentation;

    /**
     * Constructor.
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
        return original == null ? 0 : original.getModificationDate().getTime();
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return getLastModified(resource) != resource.getLastModified();
    }

    @Override
    public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException {
        try {
            Representation resultRepresentation = getStore().get(source);

            if (resultRepresentation == null) {
                resultRepresentation = this.defaultRepresentation;
            }

            if (resultRepresentation == null) {
                throw new ResourceNotFoundException("Could not locate resource '" + source + "'");
            }

            return resultRepresentation.getReader();
        } catch (IOException ioe) {
            throw new ResourceNotFoundException(ioe);
        }
    }

    @Override
    public void init(ExtProperties configuration) {
    }

}
