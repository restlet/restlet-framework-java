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

package org.restlet.ext.apispark.internal.agent.module;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.routing.Filter;

/**
 * Analytics module for the agent. This class extends {@link Filter} and sends
 * call logs to the APISpark platform. To view them, open the Analytics tab of
 * your Connector.
 * 
 * Posting is asynchronous to preserve performance.
 * 
 * @author Cyprien Quilici
 */
public class AnalyticsModule extends Filter {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(AnalyticsModule.class
            .getName());

    /** Analytics handler */
    private AnalyticsHandler analyticsHandler;

    /**
     * Last segment of the path to the APISpark analytics service for this
     * connector.
     */
    public static final String ANALYTICS_PATH = "/analytics";

    /**
     * Create a new Analytics module with the specified settings.
     * 
     * @param apiSparkConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     */
    public AnalyticsModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings) {
        this(apiSparkConfig, modulesSettings, null);
    }

    /**
     * Create a new Authentication module with the specified settings.
     * 
     * @param apiSparkConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     * @param context
     *            The context.
     */
    public AnalyticsModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings, Context context) {
        super(context);

        analyticsHandler = new AnalyticsHandler(apiSparkConfig, modulesSettings);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        request.getAttributes().put("org.restlet.startTime", getTimeMillis());
        return CONTINUE;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        long startTime = (Long) request.getAttributes().get(
                "org.restlet.startTime");
        int duration = (int) (getTimeMillis() - startTime);
        analyticsHandler.addCallLogToBuffer(request, response, duration,
                startTime);
    }

    /**
     * Returns the current time in milliseconds. Uses {@link System#nanoTime()}
     * for enhanced precision.
     * 
     * @return The current time in milliseconds.
     */
    private long getTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    @Override
    public synchronized void stop() throws Exception {
        analyticsHandler.stop();
        super.stop();
    }
}
