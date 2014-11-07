package org.restlet.ext.apispark.internal.agent.module;

import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.internal.agent.AgentConfig;
import org.restlet.ext.apispark.FirewallService;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationException;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.FirewallIpFilter;
import org.restlet.ext.apispark.internal.agent.bean.FirewallRateLimit;
import org.restlet.ext.apispark.internal.agent.bean.FirewallSettings;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.resource.FirewallSettingsResource;
import org.restlet.ext.apispark.internal.firewall.FirewallFilter;
import org.restlet.routing.Filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Manuel Boillod
 */
public class FirewallModule extends Filter {

    public static final String MODULE_PATH = "/firewall";
    public static final String SETTINGS_PATH = MODULE_PATH + "/settings";

    private final FirewallFilter firewallFilter;

    public FirewallModule(AgentConfig agentConfig, ModulesSettings modulesSettings) {
        this(agentConfig, modulesSettings, null);
    }

    public FirewallModule(AgentConfig agentConfig, ModulesSettings modulesSettings, Context context) {
        super(context);

        FirewallSettingsResource firewallSettingsClientResource = AgentUtils.getConfiguredClientResource(
                agentConfig, modulesSettings, FirewallSettingsResource.class, SETTINGS_PATH);

        FirewallSettings firewallSettings;
        try {
            firewallSettings = firewallSettingsClientResource.getSettings();
        } catch (Exception e) {
            throw new AgentConfigurationException("Could not get firewall module configuration from APISpark connector service", e);
        }

        firewallFilter = createFirewallFilter(firewallSettings);
    }

    private FirewallFilter createFirewallFilter(FirewallSettings firewallSettings) {
        FirewallService firewallService = new FirewallService();

        addIpFilterRules(firewallSettings, firewallService);
        addRateLimitationRules(firewallSettings, firewallService);

        return (FirewallFilter) firewallService.createInboundFilter(getContext());
    }

    private void addIpFilterRules(FirewallSettings firewallSettings, FirewallService firewallService) {
        if (firewallSettings.getIpFilters() != null) {
            for (FirewallIpFilter ipFilter : firewallSettings.getIpFilters()) {
                if (ipFilter.isWhiteList()) {
                    firewallService.addIpAddressesWhiteList(ipFilter.getIps());
                } else {
                    firewallService.addIpAddressesBlackList(ipFilter.getIps());
                }
            }
        }
    }

    private void addRateLimitationRules(FirewallSettings firewallSettings, FirewallService firewallService) {
        if (firewallSettings.getRateLimits() != null) {
            List<FirewallRateLimit> rateLimits = firewallSettings.getRateLimits();
            Map<Integer, Collection<FirewallRateLimit>> rateLimitsByPeriod = sortRateLimitsByPeriod(rateLimits);
            for (Integer period : rateLimitsByPeriod.keySet()) {
                Map<String, Integer> limitsPerRole = new HashMap<>();
                int defaultRateLimit = Integer.MAX_VALUE;

                for (FirewallRateLimit firewallRateLimit : rateLimitsByPeriod.get(period)) {
                    if (firewallRateLimit.isDefaultRateLimit()) {
                        defaultRateLimit = firewallRateLimit.getRateLimit();
                    } else {
                        limitsPerRole.put(firewallRateLimit.getGroup(), firewallRateLimit.getRateLimit());
                    }
                }

                firewallService.addRolesPeriodicCounter(period, TimeUnit.SECONDS, limitsPerRole, defaultRateLimit);
            }
        }
    }

    private Map<Integer, java.util.Collection<FirewallRateLimit>> sortRateLimitsByPeriod(
            List<FirewallRateLimit> rateLimits) {

        ListMultimap<Integer, FirewallRateLimit> rateLimitsByPeriod =
                Multimaps.index(rateLimits, new Function<FirewallRateLimit, Integer>() {
            @Override
            public Integer apply(FirewallRateLimit firewallRateLimit) {
                return firewallRateLimit.getPeriod();
            }
        });
        return rateLimitsByPeriod.asMap();
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        return firewallFilter.beforeHandle(request, response);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        firewallFilter.afterHandle(request, response);
    }
}
