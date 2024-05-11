package org.restlet.ext.platform.internal.agent;

import java.util.TimerTask;
import java.util.logging.Logger;

import org.restlet.ext.platform.internal.RestletCloudFilter;

/**
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class AgentConfigurationTimerTask extends TimerTask {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(AgentConfigurationTimerTask.class.getName());

    /** RestletCloud filter to reconfigure at each tick. */
    private RestletCloudFilter agentFilter;

    public AgentConfigurationTimerTask(RestletCloudFilter agentFilter) {
        this.agentFilter = agentFilter;
    }

    @Override
    public void run() {
        agentFilter.refreshRestletCloudFilterIfRevisionChanged();
    }
}
