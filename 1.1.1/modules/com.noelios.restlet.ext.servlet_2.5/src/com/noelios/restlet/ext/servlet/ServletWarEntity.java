/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletContext;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;

import com.noelios.restlet.local.Entity;

/**
 * Local entity based on a Servlet context's resource file.
 * 
 * @author Thierry Boileau
 */
public class ServletWarEntity extends Entity {
    /**
     * List of children files if it is a directory. We suppose that in a WAR
     * entity, this list does not change, and thus can be cached during the
     * request processing.
     */
    private List<Entity> children = null;

    /** Is this file a directory? */
    private final boolean directory;

    /** The full name of the file (without trailing "/"). */
    private final String fullName;

    /** The relative path of the file inside the context. */
    private final String path;

    /** The Servlet context to use. */
    private final ServletContext servletContext;

    /**
     * Constructor.
     * 
     * @param servletContext
     *            The parent Servlet context.
     * @param path
     *            The entity path.
     */
    @SuppressWarnings("unchecked")
    public ServletWarEntity(ServletContext servletContext, String path) {
        this.children = null;
        this.servletContext = servletContext;
        this.path = path;

        if (path.endsWith("/")) {
            this.directory = true;
            this.fullName = path.substring(0, path.length() - 1);
            Set childPaths = getServletContext().getResourcePaths(path);

            if (childPaths != null && !childPaths.isEmpty()) {
                this.children = new ArrayList<Entity>();

                for (Object childPath : childPaths) {
                    this.children.add(new ServletWarEntity(this.servletContext,
                            (String) childPath));
                }
            }
        } else {
            this.fullName = path;
            Set childPaths = getServletContext().getResourcePaths(path);

            if (childPaths != null && !childPaths.isEmpty()) {
                this.directory = true;
                this.children = new ArrayList<Entity>();

                for (Object childPath : childPaths) {
                    this.children.add(new ServletWarEntity(this.servletContext,
                            (String) childPath));
                }
            } else {
                this.directory = false;
            }
        }
    }

    @Override
    public boolean exists() {
        boolean result = false;

        try {
            result = (isDirectory() && getChildren() != null)
                    || (isNormal() && getServletContext()
                            .getResource(this.path) != null);
        } catch (MalformedURLException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to test the existence of the WAR resource", e);
        }

        return result;
    }

    @Override
    public List<Entity> getChildren() {
        return this.children;
    }

    @Override
    public String getName() {
        int index = this.fullName.lastIndexOf("/");

        if (index != -1) {
            return this.fullName.substring(index + 1);
        }

        return this.fullName;
    }

    @Override
    public Entity getParent() {
        Entity result = null;
        int index = this.fullName.lastIndexOf("/");

        if (index != -1) {
            result = new ServletWarEntity(getServletContext(), this.fullName
                    .substring(0, index + 1));
        }

        return result;
    }

    @Override
    public Representation getRepresentation(MediaType defaultMediaType,
            int timeToLive) {
        final InputStream ris = getServletContext().getResourceAsStream(path);
        return (ris == null) ? null : new InputRepresentation(ris,
                defaultMediaType);
    }

    /**
     * Returns the Servlet context to use.
     * 
     * @return The Servlet context to use.
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public boolean isDirectory() {
        return this.directory;
    }

    @Override
    public boolean isNormal() {
        return !isDirectory();
    }

}
