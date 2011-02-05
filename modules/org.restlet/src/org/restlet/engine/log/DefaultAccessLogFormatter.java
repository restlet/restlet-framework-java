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
