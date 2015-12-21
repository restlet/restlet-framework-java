/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.netty.internal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.restlet.engine.io.IoUtils;

/**
 * 
 * @author Jerome Louvel
 */
public class HttpContentInputStream extends InputStream {

    private final CyclicBarrier barrier;

    private volatile ByteBuf content;

    private volatile boolean lastContent;

    private final ChannelHandlerContext nettyContext;

    public HttpContentInputStream(ChannelHandlerContext nettyContext) {
        this.nettyContext = nettyContext;
        this.lastContent = false;
        this.content = null;
        this.barrier = new CyclicBarrier(2);
    }

    @Override
    public int available() throws IOException {
        return (content == null) ? 0 : content.readableBytes();
    }

    protected void getMoreContent() {
        try {
            // Ask to read more content
            nettyContext.channel().config().setAutoRead(true);

            // Block until new content is available
            barrier.await(IoUtils.TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    protected boolean isLastContent() {
        return lastContent;
    }

    public void onContent(ByteBuf content, boolean lastContent)
            throws IOException {
        try {
            // Set the new content
            this.content = content;
            this.lastContent = lastContent;

            // Unblock waiting consumer thread
            barrier.await(IoUtils.TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int read() throws IOException {
        int available = available();

        if (available == 0) {
            if (isLastContent()) {
                return -1;
            } else {
                getMoreContent();
                available = available();
            }
        }

        return content.readByte() & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int available = available();

        if (available == 0) {
            if (isLastContent()) {
                return -1;
            } else {
                getMoreContent();
                available = available();
            }
        }

        len = Math.min(available, len);
        content.readBytes(b, off, len);
        return len;
    }

}
