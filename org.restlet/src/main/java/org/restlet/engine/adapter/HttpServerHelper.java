/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.adapter;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ServerHelper;

/**
 * Base HTTP server connector. Here is the list of parameters that are supported. They should be set
 * in the Server's context before it is started:
 *
 * <table>
 * <caption>list of supported parameters</caption>
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
 * <td>org.restlet.engine.adapter.ServerAdapter</td>
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
     * Default constructor. Note that many methods assume that a non-null server is set to work
     * properly. You can use the setHelped(Server) method for this purpose or better rely on the
     * other constructor.
     */
    public HttpServerHelper() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param server The server to help.
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
                final String adapterClass =
                        getHelpedParameters()
                                .getFirstValue(
                                        "adapter", "org.restlet.engine.adapter.ServerAdapter");
                this.adapter =
                        (ServerAdapter)
                                Engine.loadClass(adapterClass)
                                        .getConstructor(Context.class)
                                        .newInstance(getContext());
            } catch (IllegalArgumentException
                    | SecurityException
                    | InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException
                    | ClassNotFoundException e) {
                getLogger().log(Level.SEVERE, "Unable to create the HTTP server adapter", e);
            }
        }

        return this.adapter;
    }

    /**
     * Handles the connector call. The default behavior is to create an REST call and delegate it to
     * the attached Restlet.
     *
     * @param httpCall The HTTP server call.
     */
    public void handle(ServerCall httpCall) {
        try {
            HttpRequest request = getAdapter().toRequest(httpCall);
            HttpResponse response = new HttpResponse(httpCall, request);
            handle(request, response);
            getAdapter().commit(response);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error while handling an HTTP server call", e);
        } finally {
            Engine.clearThreadLocalVariables();
        }
    }

    /**
     * Sets the adapter from HTTP calls to uniform calls.
     *
     * @param adapter The converter to set.
     */
    public void setAdapter(ServerAdapter adapter) {
        this.adapter = adapter;
    }
}
