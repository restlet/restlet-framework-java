package org.restlet.ext.apispark.internal.agent.application;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.ext.apispark.AgentService;

/**
 *
 * Simple Agent Application
 *
 * @author Manuel Boillod
 */
public class AgentApplication extends Application {

    public AgentApplication() {
        this(null);
    }

    public AgentApplication(Context context) {
        super(context);
        AgentService agentService = new AgentService();
        agentService.loadConfiguration();
        getServices().add(agentService);
    }

}