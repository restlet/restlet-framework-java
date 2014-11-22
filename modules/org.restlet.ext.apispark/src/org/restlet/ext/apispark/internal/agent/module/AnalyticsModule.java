/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.agent.module;

import org.restlet.Context;
import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.routing.Filter;

/**
 * @author Manuel Boillod
 */
public class AnalyticsModule extends Filter {
    public AnalyticsModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings) {
        this(apiSparkConfig, modulesSettings, null);
    }

    public AnalyticsModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings, Context context) {
        super(context);
    }
}

// public String post(String request) throws ResourceException, IOException {
// ClientResource cr = new ClientResource("");
// String response = cr.post(request).getText();
// return response;
// }
//
// public Future<String> postAsync(final String request) {
// Future<String> future = getExecutor().submit(new Callable<String>() {
// public String call() throws Exception {
// return post(request);
// }
// });
// return future;
// }
//
// protected ThreadPoolExecutor getExecutor() {
// if (executor == null) {
// executor = createExecutor();
// }
// return executor;
// }
//
// protected synchronized ThreadPoolExecutor createExecutor() {
// return new ThreadPoolExecutor(0, configuration.getMaxThreads(), 5,
// TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>(),
// createThreadFactory());
// }
//
// protected ApisparkAnalyticsThreadFactory createThreadFactory() {
// return new ApisparkAnalyticsThreadFactory(
// configuration.getThreadNameFormat());
// }
//
// class ApisparkAnalyticsThreadFactory implements ThreadFactory {
// private final AtomicInteger threadNumber = new AtomicInteger(1);
//
// private String threadNameFormat = null;
//
// public ApisparkAnalyticsThreadFactory(String threadNameFormat) {
// this.threadNameFormat = threadNameFormat;
// }
//
// public Thread newThread(Runnable r) {
// Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
// r, MessageFormat.format(threadNameFormat,
// threadNumber.getAndIncrement()), 0);
// thread.setDaemon(true);
// thread.setPriority(Thread.MIN_PRIORITY);
// return thread;
// }
// }