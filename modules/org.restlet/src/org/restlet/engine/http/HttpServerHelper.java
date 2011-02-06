/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.engine.Engine;
import org.restlet.engine.ServerHelper;
import org.restlet.engine.http.adapter.ServerAdapter;

/**
 * Base HTTP server connector. Here is the list of parameters that are
 * supported. They should be set in the Server's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>useForwardedForHeader</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Lookup the "X-Forwarded-For" header supported by popular proxies and
 * caches and uses it to populate the Request.getClientAddresses() method
 * result. This information is only safe for intermediary components within your
 * local network. Other addresses could easily be changed by setting a fake
 * header and should not be trusted for serious security checks.</td>
 * </tr>
 * <tr>
 * <td>adapter</td>
 * <td>String</td>
 * <td>org.restlet.engine.http.adapter.ServerAdapter</td>
 * <td>Class name of the adapter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public class HttpServerHelper extends ServerHelper {

    /** The adapter from HTTP calls to uniform calls. */
    private volatile ServerAdapter adapter;

    /**
     * Default constructor. Note that many methods assume that a non-null server
     * is set to work properly. You can use the setHelped(Server) method for
     * this purpose or better rely on the other constructor.
     */
    public HttpServerHelper() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        this.adapter = null;
    }

    /**
     * Returns the adapter from HTTP calls to uniform calls.
     * 
     * @return the adapter from HTTP calls to uniform calls.
     */
    public ServerAdapter getAdapter() {
        if (this.adapter == null) {
            try {
                final String adapterClass = getHelpedParameters()
                        .getFirstValue("adapter",
                                "org.restlet.engine.http.adapter.ServerAdapter");
                this.adapter = (ServerAdapter) Engine.loadClass(adapterClass)
                        .getConstructor(Context.class)
                        .newInstance(getContext());
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server adapter", e);
            } catch (SecurityException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server adapter", e);
            } catch (InstantiationException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server adapter", e);
            } catch (IllegalAccessException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server adapter", e);
            } catch (InvocationTargetException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server adapter", e);
            } catch (NoSuchMethodException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server adapter", e);
            } catch (ClassNotFoundException e) {
                getLogger().log(Level.SEVERE,
                        "Unable to create the HTTP server adapter", e);
            }
        }

        return this.adapter;
    }

    /**
     * Handles the connector call. The default behavior is to create an REST
     * call and delegate it to the attached Restlet.
     * 
     * @param httpCall
     *            The HTTP server call.
     */
    public void handle(ServerCall httpCall) {
        try {
            HttpRequest request = getAdapter().toRequest(httpCall);
            HttpResponse response = new HttpResponse(httpCall, request);
            handle(request, response);
            getAdapter().commit(response);
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Error while handling an HTTP server call: ",
                    e.getMessage());
            getLogger().log(Level.INFO,
                    "Error while handling an HTTP server call", e);
        } finally {
            Engine.clearThreadLocalVariables();
        }
    }

    /**
     * Sets the adapter from HTTP calls to uniform calls.
     * 
     * @param adapter
     *            The converter to set.
     */
    public void setAdapter(ServerAdapter adapter) {
        this.adapter = adapter;
    }
}
