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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.CallLog;
import org.restlet.ext.apispark.internal.agent.bean.CallLogs;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.resource.AnalyticsResource;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class AnalyticsHandler {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(AnalyticsHandler.class
            .getName());

    /** Maximum number of concurrent call logs post threads */
    private static final int THREAD_MAX_NUMBER = 3;

    /**
     * Number of buffered calls. Asynchronous post of analytics is triggered
     * either every POST_PERIOD or when the buffer exceeds this number.
     */
    private int bufferSize = 100;


    /** Maximum time between two asynchronous call logs post. */
    private long postPeriodInSecond = 60;

    /**
     * Initial time to wait between to attempts to reach the APISpark analytics
     * service in milliseconds.
     * 
     * This number is multiplied at each attempt. See
     * {@link AsyncCallLogsPostTask#getRetryTime(int)} for more details.
     */
    private static final long RETRY_AFTER = 500;

    /**
     * Maximum number of attempts to reach the APISpark analytics service if
     * there are errors before the call logs are lost.
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * Maximum time between two attempts to reach the APISpark analytics
     * service.
     */
    private static final long MAX_TIME = TimeUnit.SECONDS.toMillis(10);

    /** Timer trigerring call logs post to APISpark */
    private Timer asyncPostTimer;

    /** Client resourceused to post call logs to APISpark */
    private AnalyticsResource analyticsClientResource;

    /** Executor service used for async tasks */
    private ExecutorService executorService;

    /** List of call logs */
    private final List<CallLog> callLogs;

    /**
     * Create a new analytics handler with the specified settings.
     * 
     * @param apiSparkConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     */
    public AnalyticsHandler(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings) {
        analyticsClientResource = AgentUtils.getClientResource(apiSparkConfig,
                modulesSettings, AnalyticsResource.class,
                AnalyticsModule.ANALYTICS_PATH);
        callLogs = Collections.synchronizedList(Lists
                .<CallLog> newArrayListWithExpectedSize(bufferSize));

        bufferSize = apiSparkConfig.getAgentAnalyticsBufferSize();
        executorService = new ThreadPoolExecutor(1, THREAD_MAX_NUMBER, 0L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(bufferSize),
                new ThreadFactoryBuilder().setNameFormat("analytics-poster-%d")
                        .build());

        postPeriodInSecond = apiSparkConfig.getAgentAnalyticsPostPeriodInSecond();
        long postPeriodInMs = TimeUnit.SECONDS.toMillis(postPeriodInSecond);

        asyncPostTimer = new Timer();
        asyncPostTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                flushLogs();

            }
        }, postPeriodInMs, postPeriodInMs);
    }

    /**
     * Generates a CallLog for the request and adds it to the buffer.
     * 
     * @param request
     *            The Request object associated with the request.
     * @param response
     *            The Response object associated with the request.
     * @param duration
     *            The duration of the request in milliseconds.
     * @param startTime
     *            The time at which the request arrived to the agent as an
     *            epoch.
     */
    public void addCallLogToBuffer(Request request, Response response,
            int duration, long startTime) {

        CallLog callLog = new CallLog();
        callLog.setDate(new Date(startTime));
        callLog.setDuration(duration);
        callLog.setMethod(request.getMethod().getName());
        callLog.setPath(request.getResourceRef().getPath());
        callLog.setRemoteIp(request.getClientInfo().getUpstreamAddress());
        callLog.setStatusCode(response.getStatus().getCode());
        callLog.setUserAgent(request.getClientInfo().getAgent());

        callLog.setUserToken((request.getClientInfo().getUser() == null) ? ""
                : request.getClientInfo().getUser().getIdentifier());

        callLogs.add(callLog);

        if (callLogs.size() >= bufferSize) {
            flushLogs();
        }
    }

    /**
     * Creates a new Thread that asynchronously posts call logs to APISpark
     */
    public void flushLogs() {
        if (callLogs.isEmpty()) {
            return;
        }

        CallLogs logsToPost;
        synchronized (callLogs) {
            if (callLogs.isEmpty()) {
                return;
            }
            logsToPost = new CallLogs(callLogs.size());
            logsToPost.addAll(callLogs);
            callLogs.clear();
        }

        postLogs(logsToPost);
    }

    /**
     * Adds a task to the executor service to post call logs to the APISpark
     * analytics service.
     * 
     * If the executor service cannot satisfy the request, the call logs are
     * lost and an error message is logged with the reason of the failure.
     * 
     * @param logsToPost
     *            The call logs to post to the APISpark analytics service.
     */
    private void postLogs(CallLogs logsToPost) {
        try {
            executorService.execute(new AsyncCallLogsPostTask(logsToPost));
        } catch (RejectedExecutionException e) {
            LOGGER.severe("Posting " + logsToPost.size()
                    + " call logs failed permanently due to \""
                    + e.getCause().getMessage() + "\".");
            errorSendLog(logsToPost);
        }
    }

    /**
     * Called on permanent errors. Override to add your own behavior.
     * 
     * @param logsToPost
     *            The list of logs that were not posted.
     */
    protected void errorSendLog(CallLogs logsToPost) {
        // do nothing
    }

    /**
     * Asynchronous task posting the call logs to APISpark and implementing
     * fall-back methods if attempts are not successful.
     * 
     * @author Cyprien Quilici
     * 
     */
    private class AsyncCallLogsPostTask implements Runnable {

        private CallLogs logsToPost;

        public AsyncCallLogsPostTask(CallLogs logsToPost) {
            this.logsToPost = logsToPost;
        }

        @Override
        public void run() {
            for (int attemptNumber = 1; attemptNumber <= MAX_ATTEMPTS + 1; attemptNumber++) {
                try {
                    analyticsClientResource.postLogs(logsToPost);
                    LOGGER.fine(logsToPost.size()
                            + " call logs sent to the analytics service.");
                    break;
                } catch (Exception e) {
                    if (attemptNumber == MAX_ATTEMPTS) {
                        LOGGER.severe("Posting " + logsToPost.size()
                                + " call logs failed permanently after "
                                + MAX_ATTEMPTS + " attempts.");
                        errorSendLog(logsToPost);
                    } else {
                        LOGGER.warning("Error sending "
                                + logsToPost.size()
                                + " call logs to the analytics service during attempt n°"
                                + attemptNumber + " because \""
                                + e.getMessage() + "\".");
                        try {
                            Thread.sleep(getRetryTime(attemptNumber));
                        } catch (InterruptedException e1) {
                            // ignore
                        }
                    }
                }
            }
        }

        /**
         * Returns the time to wait between two attempts to reach the APISpark
         * analytics service.
         * 
         * It is multiplied by 2 each attempt with a maximum limit of
         * {@link AnalyticsHandler#MAX_TIME}.
         * 
         * @param attemptNumber
         *            The number of the attempt.
         * @return The time to wait between two attempts to reach the APISpark
         *         analytics service.
         */
        private long getRetryTime(int attemptNumber) {
            long newTime = RETRY_AFTER
                    * ((int) Math.pow(2.0d, attemptNumber - 1));
            return Math.min(newTime, MAX_TIME);
        }
    }

    public synchronized void stop() throws Exception {
        asyncPostTimer.cancel();
        executorService.shutdown();
    }
}
