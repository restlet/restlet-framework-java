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

import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationException;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.resource.ModulesSettingsResource;

/**
 * Get modules settings from connector service
 * 
 * @author Manuel Boillod
 */
public class ModulesSettingsModule {

    public static final String MODULE_PATH = "/settings";

    /**
     * Retrieves the modules settings from the service if it has changed, null
     * otherwise.
     * 
     * @param apiSparkConfig
     *            The service's configuration
     * @param modulesSettings
     *            The current modules settings
     * @return The updated modules settings if it has changed, null otherwise.
     */
    public static ModulesSettings getModulesSettings(
            ApiSparkConfig apiSparkConfig, ModulesSettings modulesSettings) {

        // When modulesSettings not null, cell revision header is set
        ModulesSettingsResource modulesSettingsResource = AgentUtils
                .getClientResource(apiSparkConfig, modulesSettings,
                        ModulesSettingsResource.class, MODULE_PATH);
        try {
            return modulesSettingsResource.getSettings();
        } catch (Exception e) {
            throw new AgentConfigurationException(
                    "Unable to retrieve agent settings from apispark", e);
        }
    }

}
