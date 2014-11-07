package org.restlet.ext.apispark.internal.agent.resource;

import org.restlet.ext.apispark.internal.agent.bean.FirewallSettings;
import org.restlet.resource.Get;

public interface FirewallSettingsResource {

    /**
     * Retrieve the firewall settings from apispark connector cell.
     */
    @Get
    public FirewallSettings getSettings();
}
