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
