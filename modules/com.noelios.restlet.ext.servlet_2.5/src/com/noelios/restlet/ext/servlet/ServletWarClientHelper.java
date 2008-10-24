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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;

import com.noelios.restlet.local.LocalFile;
import com.noelios.restlet.local.LocalFileClientHelper;

/**
 * Local client connector based on a Servlet context (JEE Web application
 * context). Here is a sample resource URI:<br>
 * 
 * <pre>
 * war:///path/to/my/resource/entry.txt
 * </pre>
 * 
 * <br>
 * You can note that there is no authority which is denoted by the sequence of
 * three "/" characters. This connector is designed to be used inside a context
 * (e.g. inside a servlet based application) and subconsequently does not
 * require the use of a authority. Such URI are "relative" to the root of the
 * servlet context.<br>
 * Here is a sample code excerpt that illustrates the way to use this connector:
 * 
 * <code>Response response = getContext().getClientDispatcher().get("war:///myDir/test.txt");
 if (response.isEntityAvailable()) {
 //Do what you want to do.
 }
 </code>
 * 
 * @author Jerome Louvel
 */
public class ServletWarClientHelper extends LocalFileClientHelper {

    /**
     * Private implementation of a LocalFile based on a servlet context of the
     * LocalFile abstract class.
     * 
     */
    private class ServletLocalFile extends LocalFile {
        /**
         * List of children files if it is a directory. We suppose that in a WAR
         * file, this list does not change, and thus can be stored.
         */
        private List<LocalFile> files = null;

        /** The full name of the file (without trailing "/"). */
        private String fullName;

        /** Is this file a directory? */
        private boolean isDirectory;

        /** The relative path of the file inside the context. */
        private String path;

        /** The Servlet context to use. */
        private ServletContext servletContext;

        public ServletLocalFile(ServletContext servletContext, String path) {
            super();
            this.servletContext = servletContext;
            this.path = path;
 
            if (path.endsWith("/")) {
                this.isDirectory = true;
                fullName = path.substring(0, path.length() - 1);
            } else {
                setFiles(servletContext.getResourcePaths(path + "/"));
                this.isDirectory = this.files != null;
                fullName = path;
            }
        }

        @Override
        public boolean exists() {
            try {
                return (isDirectory() && getFiles() != null)
                        || (isFile() && servletContext.getResource(this.path) != null);
            } catch (MalformedURLException e) {
            }
            return false;
        }

        @Override
        public Collection<LocalFile> getFiles() {
            List<LocalFile> result = null;

            if (isDirectory()) {
                if (this.files == null) {
                    // Lazy initialiation
                    setFiles(this.servletContext.getResourcePaths(this.path));
                }
                result = this.files;
            }

            return result;
        }

        @Override
        public String getName() {
            int index = this.fullName.lastIndexOf("/");
            if (index != -1) {
                return this.fullName.substring(index + 1);
            } else {
                return this.fullName;
            }
        }

        @Override
        public LocalFile getParent() {
            int index = this.fullName.lastIndexOf("/");
            if (index != -1) {
                return new ServletLocalFile(this.servletContext, this.fullName
                        .substring(0, index + 1));
            }

            return null;
        }

        @Override
        public Representation getRepresentation(MediaType defaultMediaType,
                int timeToLive) {
            final InputStream ris = getServletContext().getResourceAsStream(
                    path);
            return new InputRepresentation(ris, defaultMediaType);
        }

        @Override
        public boolean isDirectory() {
            return this.isDirectory;
        }

        @Override
        public boolean isFile() {
            return !this.isDirectory;
        }

        /**
         * Set the listing of files according to a given set of file names.
         * 
         * @param set
         *            The set of file names.
         */
        private void setFiles(Set set) {
            if (set != null && !set.isEmpty()) {
                this.files = new ArrayList<LocalFile>();
                for (Object object : set) {
                    this.files.add(new ServletLocalFile(this.servletContext,
                            (String) object));
                }
            }
        }
    }

    /** The Servlet context to use. */
    private volatile ServletContext servletContext;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     * @param servletContext
     *            The Servlet context.
     */
    public ServletWarClientHelper(Client client, ServletContext servletContext) {
        super(client);
        getProtocols().clear();
        getProtocols().add(Protocol.WAR);
        this.servletContext = servletContext;
    }

    @Override
    public LocalFile getLocalFile(String decodedPath) {
        return new ServletLocalFile(this.servletContext, decodedPath);
    }

    /**
     * Returns the Servlet context.
     * 
     * @return The Servlet context.
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void handle(Request request, Response response) {
        final String scheme = request.getResourceRef().getScheme();
        if (Protocol.WAR.getSchemeName().equalsIgnoreCase(scheme)) {
            super.handle(request, response);
        } else {
            throw new IllegalArgumentException(
                    "Protocol \""
                            + scheme
                            + "\" not supported by the connector. Only WAR is supported.");
        }
    }

}
