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

package org.restlet.engine.log;

import java.util.logging.Handler;

import org.restlet.engine.Engine;

/**
 * Access log record formatter which writes a header describing the default log
 * format.
 * 
 * @author Jerome Louvel
 */
public class DefaultAccessLogFormatter extends AccessLogFormatter {

    @Override
    public String getHead(Handler h) {
        final StringBuilder sb = new StringBuilder();
        sb.append("#Software: Restlet Framework ").append(Engine.VERSION)
                .append('\n');
        sb.append("#Version: 1.0\n");
        sb.append("#Date: ");
        final long currentTime = System.currentTimeMillis();
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
