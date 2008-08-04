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

package com.noelios.restlet.ext.jxta;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

/**
 * JXTA pipe management utilities.
 * 
 * @author James Todd (james dot w dot todd at gmail dot com)
 */
public class PipeUtility {

    public enum Http {
        HTTP_V1_0("HTTP/1.0"), CHARSET("US-ASCII");

        private final String http;

        Http(final String http) {
            this.http = http;
        }

        public String getHttp() {
            return this.http;
        }
    }

    public enum Pipe {
        P2PP_NAME("JXTA:HTTP2PP"), P2MP_NAME("JXTA:HTTP2MP"), P2PP_TYPE(
                PipeService.UnicastType), P2MP_TYPE(PipeService.PropagateType);

        public enum ATTRIBUTE {
            BUFFER_SIZE(65536);

            private final int attribute;

            ATTRIBUTE(final int attribute) {
                this.attribute = attribute;
            }

            public int getAttribute() {
                return this.attribute;
            }
        }

        private final String pipe;

        Pipe(final String pipe) {
            this.pipe = pipe;
        }

        public String getPipe() {
            return this.pipe;
        }
    }

    public enum Protocol {
        P2PP("p2pp"), P2PSP("p2psp"), P2MP("p2mp"), HTTP2PP("http2pp"), HTTP2MP(
                "http2mp"), HTTP("http");

        private final String protocol;

        Protocol(final String protocol) {
            this.protocol = protocol;
        }

        public String getProtocol() {
            return this.protocol;
        }
    }

    private static final String MESSAGE_DIGEST = "MD5";

    private static Map<String, String> SCHEMES;

    private static Map<String, String> PIPES;

    private static final Logger logger = Logger.getLogger(PipeUtility.class
            .getName());

    static {
        final Map<String, String> schemes = new HashMap<String, String>();

        schemes.put(PipeService.UnicastType, Protocol.P2PP.getProtocol());
        schemes
                .put(PipeService.UnicastSecureType, Protocol.P2PSP
                        .getProtocol());
        schemes.put(PipeService.PropagateType, Protocol.P2MP.getProtocol());

        SCHEMES = Collections.unmodifiableMap(schemes);

        final Map<String, String> pipes = new HashMap<String, String>();

        pipes.put(PipeService.UnicastType, Pipe.P2PP_NAME.getPipe());
        pipes.put(PipeService.PropagateType, Pipe.P2MP_NAME.getPipe());

        PIPES = Collections.unmodifiableMap(pipes);
    }

    public static PipeAdvertisement createPipeAdvertisement(String name,
            String type, PeerGroup group) {
        return createPipeAdvertisement(name, type, group, null);
    }

    public static PipeAdvertisement createPipeAdvertisement(String name,
            String type, PeerGroup group, PipeID pipeId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "name: " + name);
            logger.log(Level.FINE, "type: " + type);
            logger.log(Level.FINE, "group: " + group);
            logger.log(Level.FINE, "pipe: " + pipeId);
        }

        final PipeAdvertisement pa = (PipeAdvertisement) AdvertisementFactory
                .newAdvertisement(PipeAdvertisement.getAdvertisementType());

        pa.setPipeID(createPipeID(group, pipeId));
        pa.setName(PIPES.get(type) + "/" + name);
        pa.setType(type);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "pipe: " + pa);
        }

        return pa;
    }

    public static PipeID createPipeID(PeerGroup group) {
        return createPipeID(group, null);
    }

    public static PipeID createPipeID(PeerGroup group, PipeID pipeId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "group: " + group);
            logger.log(Level.FINE, "pipe: " + pipeId);
        }

        byte[] sb = null;

        if (pipeId != null) {
            final String s = pipeId.toString() + ":" + Http.HTTP_V1_0.getHttp();

            try {
                final MessageDigest a = MessageDigest
                        .getInstance(MESSAGE_DIGEST);

                a.reset();
                a.update(s.getBytes());

                sb = a.digest();
            } catch (final NoSuchAlgorithmException nsae) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "unable to digest");
                }
            }
        }

        final PipeID pid = sb != null ? IDFactory.newPipeID(group
                .getPeerGroupID(), sb) : IDFactory.newPipeID(group
                .getPeerGroupID());

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "pipe: " + pid);
        }

        return pid;
    }

    public static URI getURI(PipeAdvertisement pipe) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "pipe: " + pipe);
        }

        final String pid = pipe.getPipeID().toString();
        final int i = pid.lastIndexOf(":") + 1;
        final URI u = URI.create(SCHEMES.get(pipe.getType()) + "://"
                + pid.substring(i));

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "uri: " + u);
        }

        return u;
    }
}
