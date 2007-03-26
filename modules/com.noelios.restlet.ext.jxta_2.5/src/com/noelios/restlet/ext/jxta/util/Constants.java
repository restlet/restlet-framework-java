/*
 * Copyright 2007 Noelios Consulting.
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

package com.noelios.restlet.ext.jxta.util;

import net.jxta.pipe.PipeService;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class Constants {

    public static final String JXTA_HOME = "JXTA_HOME";
    public static final String PROFILE = "PROFILE";

    public enum Protocol {
        P2PP("p2pp"),
        P2PSP("p2psp"),
        P2MP("p2mp"),
        HTTP2PP("http2pp"),
        HTTP2MP("http2mp"),
        HTTP("http");

        private String protocol;

        Protocol(final String protocol) {
            this.protocol = protocol;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    public enum Pipe {
        P2PP_NAME("JXTA:HTTP2PP"),
        P2MP_NAME("JXTA:HTTP2MP"),
        P2PP_TYPE(PipeService.UnicastType),
        P2MP_TYPE(PipeService.PropagateType);

        public enum ATTRIBUTE {
            BUFFER_SIZE(65536);

            private int attribute;

            ATTRIBUTE(final int attribute) {
                this.attribute = attribute;
            }

            public int getAttribute() {
                return attribute;
            }
        }

        private String pipe;

        Pipe(final String pipe) {
            this.pipe = pipe;
        }

        public String getPipe() {
            return pipe;
        }
    }

    public enum Http {
        HTTP_V1_0("HTTP/1.0"),
        CHARSET("US-ASCII");

        private String http;

        Http(final String http) {
            this.http = http;
        }

        public String getHttp() {
            return http;
        }
    }
}
