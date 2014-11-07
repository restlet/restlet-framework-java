package org.restlet.ext.apispark.internal.agent.resource;

import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.resource.Get;

/**
 * Resource used for communicate with apispark connector cell.
 * 
 * @author Manuel Boillod
 */
public interface ModulesSettingsResource {

    /**
     * Returns the modules settings from apispark connector cell.
     * 
     * @return The modules settings of the current cell.
     */
    @Get
    ModulesSettings getSettings();
}
