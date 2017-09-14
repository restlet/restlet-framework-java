package org.restlet.ext.apispark.internal.agent;

import java.util.TimerTask;
import java.util.logging.Logger;

import org.restlet.ext.apispark.internal.ApiSparkFilter;

public class AgentConfigurationTimerTask extends TimerTask {

    /** Internal logger. */
    protected static Logger LOGGER = Logger
            .getLogger(AgentConfigurationTimerTask.class.getName());

    /** ApiSpark filter to reconfigure at each tick. */
    private ApiSparkFilter agentFilter;

    public AgentConfigurationTimerTask(ApiSparkFilter agentFilter) {
        this.agentFilter = agentFilter;
    }

    @Override
    public void run() {
        agentFilter.refreshApiSparkFilterIfRevisionChanged();
    }
}
