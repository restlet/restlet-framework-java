package org.restlet.ext.platform.internal.agent.resource;

import org.restlet.ext.platform.internal.agent.bean.CallLogs;
import org.restlet.resource.Post;

/**
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public interface AnalyticsResource {

    @Post
    void postLogs(CallLogs callLogs);
}
