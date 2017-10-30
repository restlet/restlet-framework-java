package org.restlet.ext.platform.internal.agent.resource;

import org.restlet.ext.platform.internal.agent.bean.CallLogs;
import org.restlet.resource.Post;

public interface AnalyticsResource {

    @Post
    void postLogs(CallLogs callLogs);
}
