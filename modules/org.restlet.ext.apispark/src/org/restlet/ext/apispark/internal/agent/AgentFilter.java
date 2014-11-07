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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.agent;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.module.ModulesSettingsModule;
import org.restlet.routing.Filter;

public class AgentFilter extends Filter {

    private AgentModulesConfigurer agentModulesConfigurer;

    /**
     * Create a new AgentFilter with the specified configuration.
     * 
     * @param agentConfig
     *            The agent configuration.
     */
    public AgentFilter(AgentConfig agentConfig) {
        this(agentConfig, null);
    }

    /**
     * Create a new AgentFilter with the specified configuration.
     * 
     * @param agentConfig
     *            The agent configuration.
     * @param context
     *            The current context.
     */
    public AgentFilter(AgentConfig agentConfig, Context context) {
        super(context);
        configureAgent(agentConfig);
    }

    /**
     * Configure the filter with the specified configuration.
     * 
     * Retrieve the modules settings from the service.
     * 
     * @param agentConfig
     *            The agent configuration.
     */
    public void configureAgent(AgentConfig agentConfig) {
        agentConfig.validate();

        ModulesSettingsModule modulesSettingsModule = new ModulesSettingsModule(
                agentConfig);
        ModulesSettings modulesSettings = modulesSettingsModule
                .getModulesSettings();

        agentModulesConfigurer = AgentModulesHelper.buildFromSettings(
                agentConfig, modulesSettings, getContext());
    }

    @Override
    public Restlet getNext() {
        return agentModulesConfigurer.getNext();
    }

    @Override
    public void setNext(Restlet next) {
        agentModulesConfigurer.setNext(next);
    }

}
