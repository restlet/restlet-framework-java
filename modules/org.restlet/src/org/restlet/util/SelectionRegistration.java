/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.util;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.IoUtils;

/**
 * Represents a unique registration between a NIO selector and a selectable
 * channel. For the operation codes, see the constants in {@link SelectionKey}.
 * 
 * @author Jerome Louvel
 * @see SelectionKey
 */
public class SelectionRegistration {

    /**
     * Returns the name of the given IO operation.
     * 
     * @param operation
     *            The IO operation code.
     * @return The name of the given IO operation.
     */
    public static String getName(int operation) {
        StringBuilder result = new StringBuilder();

        if ((operation & SelectionKey.OP_ACCEPT) != 0) {
            result.append("ACCEPT ");
        }

        if ((operation & SelectionKey.OP_CONNECT) != 0) {
            result.append("CONNECT ");
        }

        if ((operation & SelectionKey.OP_READ) != 0) {
            result.append("READ ");
        }

        if ((operation & SelectionKey.OP_WRITE) != 0) {
            result.append("WRITE ");
        }

        if (operation == 0) {
            result.append("NONE ");
        }

        if (result.length() == 0) {
            result.append(operation);
        }

        return result.toString();
    }

    /** The barrier used for blocking/unblocking support. */
    private final CyclicBarrier barrier;

    /** Indicates if that registration has been canceled. */
    private volatile boolean canceling;

    /** The IO operations interest. */
    private volatile int interestOperations;

    /** The selection listener that will be notified. */
    private volatile SelectionListener listener;

    /** The previous IO operations interest. */
    private volatile int previousInterest;

    /** The IO operations ready. */
    private volatile int readyOperations;

    /** The parent selectable channel. */
    private final SelectableChannel selectableChannel;

    /** The active selection key. */
    private volatile SelectionKey selectionKey;

    /**
     * Constructor.
     * 
     * @param interestOperations
     *            The IO operations interest.
     * @param listener
     *            The selection listener that will be notified.
     */
    public SelectionRegistration(int interestOperations,
            SelectionListener listener) {
        this(null, interestOperations, listener);
    }

    /**
     * Constructor.
     * 
     * @param selectableChannel
     *            The parent selectable channel.
     * @param interestOperations
     *            The IO operations interest.
     * @param listener
     *            The selection listener that will be notified.
     */
    public SelectionRegistration(SelectableChannel selectableChannel,
            int interestOperations, SelectionListener listener) {
        this.canceling = false;
        this.selectableChannel = selectableChannel;
        this.barrier = new CyclicBarrier(2);
        this.listener = listener;
        this.setInterestOperations(interestOperations);
    }

    /**
     * Adds a given operations to the current list.
     * 
     * @param interest
     */
    public void addInterestOperations(int interest) {
        setInterestOperations(getInterestOperations() & interest);
    }

    /**
     * Blocks the calling thread.
     * 
     * @throws IOException
     * 
     * @see #block()
     */
    public void block() throws IOException {
        try {
            if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                Context.getCurrentLogger().log(
                        Level.FINEST,
                        "Calling thread about to block on the NIO selection registration. Timeout: "
                                + TimeUnit.MILLISECONDS
                                        .toMillis(IoUtils.TIMEOUT_MS)
                                + " ms. Waiting: "
                                + this.barrier.getNumberWaiting());
            }

            this.barrier.await(IoUtils.TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to block the thread at the cyclic barrier", e);
            IOException ioe = new IOException(
                    "Unable to block the thread at the cyclic barrier.");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Clears the registration.
     */
    public void clear() {
        this.barrier.reset();
        this.canceling = false;
        this.interestOperations = 0;
        this.previousInterest = 0;
        this.readyOperations = 0;
        this.selectionKey = null;
    }

    /**
     * Returns the IO operations interest.
     * 
     * @return The IO operations interest.
     */
    public int getInterestOperations() {
        return interestOperations;
    }

    /**
     * Returns the selection listener that will be notified.
     * 
     * @return The selection listener that will be notified.
     */
    public SelectionListener getListener() {
        return listener;
    }

    /**
     * Returns the IO operations ready.
     * 
     * @return The IO operations ready.
     */
    public int getReadyOperations() {
        return readyOperations;
    }

    /**
     * Returns the parent selectable channel.
     * 
     * @return The parent selectable channel.
     */
    public SelectableChannel getSelectableChannel() {
        return this.selectableChannel;
    }

    /**
     * Indicates if a canceling of the registration has been requested.
     * 
     * @return True if the registration has been canceled.
     */
    public boolean isCanceling() {
        return this.canceling;
    }

    /**
     * Indicates if the NIO channel is connectable.
     * 
     * @return True if the NIO channel is connectable.
     */
    public boolean isConnectable() {
        return (getReadyOperations() & SelectionKey.OP_CONNECT) != 0;
    }

    /**
     * Indicates if the operations of interest are ready.
     * 
     * @return True if the operations of interest are ready.
     */
    public boolean isInterestReady() {
        return ((getReadyOperations() & getInterestOperations()) > 0);
    }

    /**
     * Indicates if the NIO channel is readable.
     * 
     * @return True if the NIO channel is readable.
     */
    public boolean isReadable() {
        return (getReadyOperations() & SelectionKey.OP_READ) != 0;
    }

    /**
     * Indicates if the NIO channel is writable.
     * 
     * @return True if the NIO channel is writable.
     */
    public boolean isWritable() {
        return (getReadyOperations() & SelectionKey.OP_WRITE) != 0;
    }

    /**
     * Called back with some interest operations are ready. By default, it calls
     * back the registered listener provided by {@link #getListener()}.
     * 
     * @param readyOperations
     *            The ready operations.
     * @throws IOException
     */
    public void onSelected(int readyOperations) throws IOException {
        this.readyOperations = readyOperations;

        if ((getListener() != null) && isInterestReady()) {
            getListener().onSelected();
        }
    }

    /**
     * Effectively registers the {@link #getSelectableChannel()} with the given
     * {@link Selector} for the {@link #getInterestOperations()} operations.
     * 
     * @param selector
     *            The NIO selector to register to.
     */
    public SelectionKey register(Selector selector) {
        try {
            this.selectionKey = getSelectableChannel().register(selector,
                    getInterestOperations(), this);
        } catch (ClosedChannelException cce) {
            Context.getCurrentLogger().log(Level.FINE,
                    "Unable to register again", cce);
        }

        return this.selectionKey;
    }

    /**
     * Resume interest in new listener notifications. This should be called
     * after a {@link #suspend()} call.
     */
    public void resume() {
        if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
            Context.getCurrentLogger().log(Level.FINER,
                    "Resuming previous NIO interest");
        }

        setInterestOperations(this.previousInterest);
    }

    /**
     * Sets interest in canceling the registration.
     * 
     * @param canceling
     *            True if a canceling request is made.
     */
    public void setCanceling(boolean canceling) {
        this.canceling = canceling;
    }

    /**
     * Sets the IO operations interest. Note that it also clears the ready
     * operations.
     * 
     * @param interest
     *            The IO operations interest.
     * @return True if the operations effectively changed.
     */
    public boolean setInterestOperations(int interest) {
        boolean result = false;

        if (this.interestOperations != interest) {
            this.interestOperations = interest;
            result = true;
        }

        setReadyOperations(0);
        return result;
    }

    /**
     * Sets the selection listener that will be notified.
     * 
     * @param listener
     *            The selection listener that will be notified.
     */
    public void setListener(SelectionListener listener) {
        this.listener = listener;
    }

    /**
     * Sets interest in no IO operations.
     */
    public void setNoInterest() {
        setInterestOperations(0);
    }

    /**
     * Sets interest in IO read operations.
     */
    public void setReadInterest() {
        setInterestOperations(SelectionKey.OP_READ);
    }

    /**
     * Sets the IO operations ready.
     * 
     * @param readyOperations
     *            The IO operations ready.
     */
    public void setReadyOperations(int readyOperations) {
        this.readyOperations = readyOperations;
    }

    /**
     * Sets interest in IO read operations.
     */
    public void setWriteInterest() {
        setInterestOperations(SelectionKey.OP_WRITE);
    }

    /**
     * Suspend interest in new listener notifications. By default, remembers the
     * current interest and calls {@link #setInterestOperations(int)} with a 0
     * value.
     */
    public void suspend() {
        this.previousInterest = getInterestOperations();
        setInterestOperations(0);
    }

    @Override
    public String toString() {
        return "Interest= " + getName(getInterestOperations()) + ", Ready="
                + getName(getReadyOperations()) + ", Canceling="
                + Boolean.toString(isCanceling());
    }

    /**
     * Unblocks the optionally blocked thread.
     * 
     * @throws IOException
     * 
     * @see #block()
     */
    public void unblock() throws IOException {
        if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
            Context.getCurrentLogger().log(
                    Level.FINEST,
                    "Calling thread about to unblock the NIO selection registration. Timeout: "
                            + TimeUnit.MILLISECONDS
                                    .toMillis(IoUtils.TIMEOUT_MS)
                            + " ms. Waiting: "
                            + this.barrier.getNumberWaiting());
        }

        try {
            this.barrier.await(IoUtils.TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Context.getCurrentLogger()
                    .log(Level.WARNING,
                            "Unable to unblock the waiting thread at the cyclic barrier",
                            e);
            IOException ioe = new IOException(
                    "Unable to unblock the waiting thread at the cyclic barrier.");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Effectively updates the registration of the
     * {@link #getSelectableChannel()} with the given {@link Selector} for the
     * {@link #getInterestOperations()} operations.
     * 
     * @return The updated selection key or a new one if it was registered
     *         again.
     */
    public SelectionKey update() {
        if (this.selectionKey.isValid()) {
            if (isCanceling()) {
                Context.getCurrentLogger().log(Level.FINER,
                        "Cancelling of the selection key requested");
                this.selectionKey.cancel();
            } else {
                try {
                    if (Context.getCurrentLogger().isLoggable(Level.FINEST)) {
                        Context.getCurrentLogger()
                                .log(Level.FINEST,
                                        "Update key (old | new) : "
                                                + SelectionRegistration
                                                        .getName(this.selectionKey
                                                                .interestOps())
                                                + " | "
                                                + SelectionRegistration
                                                        .getName(getInterestOperations()));
                    }

                    this.selectionKey.interestOps(getInterestOperations());
                } catch (CancelledKeyException cke) {
                    Context.getCurrentLogger()
                            .log(Level.FINE,
                                    "Unable to update a cancelled key, registering again",
                                    cke);
                    this.selectionKey = register(this.selectionKey.selector());
                }
            }
        } else {
            Context.getCurrentLogger().log(Level.FINE,
                    "Invalid key detected, registering again");
            this.selectionKey = register(this.selectionKey.selector());
        }

        return this.selectionKey;
    }

}
