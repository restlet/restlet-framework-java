/*
 * Copyright 2005-2007 Noelios Technologies.
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

package com.noelios.restlet.util;

import java.util.logging.Handler;

import com.noelios.restlet.Engine;

/**
 * Access log record formatter which writes a header describing the default log
 * format.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class DefaultAccessLogFormatter extends AccessLogFormatter {

    @Override
    public String getHead(Handler h) {
        StringBuilder sb = new StringBuilder();
        sb.append("#Software: Noelios Restlet Engine ").append(Engine.VERSION)
                .append('\n');
        sb.append("#Version: 1.0\n");
        sb.append("#Date: ");
        long currentTime = System.currentTimeMillis();
        sb.append(String.format("%tF", currentTime));
        sb.append(' ');
        sb.append(String.format("%tT", currentTime));
        sb.append('\n');
        sb.append("#Fields: ");
        sb.append("date time c-ip cs-username s-ip s-port cs-method ");
        sb.append("cs-uri-stem cs-uri-query sc-status sc-bytes cs-bytes ");
        sb.append("time-taken cs-host cs(User-Agent) cs(Referrer)\n");
        return sb.toString();
    }

}
