package org.restlet.ext.apispark.internal.agent.resource;

import org.restlet.ext.apispark.internal.agent.bean.CallLogs;
import org.restlet.resource.Post;

public interface AnalyticsResource {

    @Post
    void postLogs(CallLogs callLogs);
}
