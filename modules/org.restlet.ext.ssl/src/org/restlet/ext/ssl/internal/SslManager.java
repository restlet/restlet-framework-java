/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.ssl.internal;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Manages an SSL engine provided by a parent SSL context.
 * 
 * @author Jerome Louvel
 */
public class SslManager {

    /** Indicates if the manager is used on the client-side of the SSL protocol. */
    private final boolean clientSide;

    /** The context to use for SSL engine creation. */
    private final SSLContext context;

    /** The engine to use for wrapping and unwrapping. */
    private volatile SSLEngine engine;

    /** The peer address. */
    private volatile InetSocketAddress peerAddress;

    /** The global state. */
    private volatile SslState state;

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use for SSL engine creation.
     * @param peerAddress
     *            The peer address.
     * @param clientSide
     *            Indicates if the manager is used on the client-side of the SSL
     *            protocol.
     * @throws SSLException
     */
    public SslManager(SSLContext context, InetSocketAddress peerAddress,
            boolean clientSide) throws SSLException {
        this.context = context;
        this.peerAddress = peerAddress;
        this.clientSide = clientSide;
        this.state = SslState.IDLE;
        initEngine();
    }

    /**
     * Returns the
     * 
     * @return The
     */
    public int getApplicationBufferSize() {
        return getSession() == null ? 0 : getSession()
                .getApplicationBufferSize();
    }

    /**
     * Returns the context to use for SSL engine creation.
     * 
     * @return The context to use for SSL engine creation.
     */
    public SSLContext getContext() {
        return context;
    }

    /**
     * Returns the engine to use for wrapping and unwrapping.
     * 
     * @return The engine to use for wrapping and unwrapping.
     */
    public SSLEngine getEngine() {
        return engine;
    }

    /**
     * Returns the
     * 
     * @return The
     */
    public int getPacketBufferSize() {
        return getSession() == null ? 0 : getSession().getPacketBufferSize();
    }

    /**
     * Returns the peer address.
     * 
     * @return The peer address.
     */
    public InetSocketAddress getPeerAddress() {
        return peerAddress;
    }

    /**
     * Returns the current SSL session or null.
     * 
     * @return The current SSL session or null.
     */
    public SSLSession getSession() {
        return getEngine() == null ? null : getEngine().getSession();
    }

    /**
     * Returns the global state.
     * 
     * @return The global state.
     */
    public SslState getState() {
        return state;
    }

    @Override
    public String toString() {
        return getState() + " | " + getEngine();
    }

    /**
     * Initializes the SSL engine with the current SSL context and socket
     * address.
     * 
     * @throws SSLException
     */
    public void initEngine() throws SSLException {
        if (getContext() != null) {
            if (getPeerAddress() != null) {
                setEngine(getContext().createSSLEngine(
                        getPeerAddress().getHostName(),
                        getPeerAddress().getPort()));
            } else {
                setEngine(getContext().createSSLEngine());
            }

            setState(SslState.CREATED);
            getEngine().setUseClientMode(isClientSide());
            getEngine().beginHandshake();
            setState(SslState.HANDSHAKING);
        }
    }

    /**
     * Indicates if the manager is used on the client-side of the SSL protocol.
     * 
     * @return True if the manager is used on the client-side of the SSL
     *         protocol.
     */
    public boolean isClientSide() {
        return clientSide;
    }

    /**
     * Sets the engine to use for wrapping and unwrapping.
     * 
     * @param engine
     *            The engine to use for wrapping and unwrapping.
     */
    public void setEngine(SSLEngine engine) {
        this.engine = engine;
    }

    /**
     * Sets the peer address.
     * 
     * @param peerAddress
     *            The peer address.
     */
    public void setPeerAddress(InetSocketAddress peerAddress) {
        this.peerAddress = peerAddress;
    }

    /**
     * Sets the global state.
     * 
     * @param state
     *            The global state.
     */
    public void setState(SslState state) {
        this.state = state;
    }

}
