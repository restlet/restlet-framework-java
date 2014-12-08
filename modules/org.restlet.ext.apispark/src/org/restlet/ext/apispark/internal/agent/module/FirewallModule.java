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

import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.restlet.ext.apispark.FirewallConfig;
import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationException;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.FirewallIpFilter;
import org.restlet.ext.apispark.internal.agent.bean.FirewallRateLimit;
import org.restlet.ext.apispark.internal.agent.bean.FirewallSettings;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.resource.FirewallSettingsResource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Manuel Boillod
 */
public class FirewallModule {

    public static final String MODULE_PATH = "/firewall";

    public static final String SETTINGS_PATH = MODULE_PATH + "/settings";

    FirewallSettings firewallSettings;

    public FirewallModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings) {
        FirewallSettingsResource firewallSettingsClientResource = AgentUtils
                .getClientResource(apiSparkConfig, modulesSettings,
                        FirewallSettingsResource.class, SETTINGS_PATH);

        try {
            firewallSettings = firewallSettingsClientResource.getSettings();
        } catch (Exception e) {
            throw new AgentConfigurationException(
                    "Could not get firewall module configuration from APISpark connector service",
                    e);
        }
    }

    public void updateFirewallConfig(FirewallConfig firewallConfig) {
        addIpFilterRules(firewallSettings, firewallConfig);
        addRateLimitationRules(firewallSettings, firewallConfig);
    }

    private void addIpFilterRules(FirewallSettings firewallSettings,
            FirewallConfig firewallConfig) {
        if (firewallSettings.getIpFilters() != null) {
            for (FirewallIpFilter ipFilter : firewallSettings.getIpFilters()) {
                if (ipFilter.isWhiteList()) {
                    firewallConfig.addIpAddressesWhiteList(ipFilter.getIps());
                } else {
                    firewallConfig.addIpAddressesBlackList(ipFilter.getIps());
                }
            }
        }
    }

    private void addRateLimitationRules(FirewallSettings firewallSettings,
            FirewallConfig firewallConfig) {
        if (firewallSettings.getRateLimits() != null) {
            List<FirewallRateLimit> rateLimits = firewallSettings
                    .getRateLimits();
            Map<Integer, Collection<FirewallRateLimit>> rateLimitsByPeriod = sortRateLimitsByPeriod(rateLimits);
            for (Integer period : rateLimitsByPeriod.keySet()) {
                Map<String, Integer> limitsPerRole = new HashMap<>();
                int defaultRateLimit = Integer.MAX_VALUE;

                for (FirewallRateLimit firewallRateLimit : rateLimitsByPeriod
                        .get(period)) {
                    if (firewallRateLimit.isDefaultRateLimit()) {
                        defaultRateLimit = firewallRateLimit.getRateLimit();
                    } else {
                        limitsPerRole.put(firewallRateLimit.getGroup(),
                                firewallRateLimit.getRateLimit());
                    }
                }

                firewallConfig.addRolesPeriodicCounter(period,
                        TimeUnit.SECONDS, limitsPerRole, defaultRateLimit);
            }
        }
    }

    private Map<Integer, java.util.Collection<FirewallRateLimit>> sortRateLimitsByPeriod(
            List<FirewallRateLimit> rateLimits) {

        ListMultimap<Integer, FirewallRateLimit> rateLimitsByPeriod = Multimaps
                .index(rateLimits, new Function<FirewallRateLimit, Integer>() {
                    @Override
                    public Integer apply(FirewallRateLimit firewallRateLimit) {
                        return firewallRateLimit.getPeriod();
                    }
                });
        return rateLimitsByPeriod.asMap();
    }

}
