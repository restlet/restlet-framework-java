package org.restlet.ext.apispark.internal.agent.bean;

/**
 * @author Manuel Boillod
 */
@SuppressWarnings("ALL")
public class ModulesSettings {

    boolean analyticsModuleEnabled;

    boolean authenticationModuleEnabled;

    boolean authorizationModuleEnabled;

    String cellRevision;

    boolean firewallModuleEnabled;

    public String getCellRevision() {
        return cellRevision;
    }

    public boolean isAnalyticsModuleEnabled() {
        return analyticsModuleEnabled;
    }

    public boolean isAuthenticationModuleEnabled() {
        return authenticationModuleEnabled;
    }

    public boolean isAuthorizationModuleEnabled() {
        return authorizationModuleEnabled;
    }

    public boolean isFirewallModuleEnabled() {
        return firewallModuleEnabled;
    }

    public void setAnalyticsModuleEnabled(boolean analyticsModuleEnabled) {
        this.analyticsModuleEnabled = analyticsModuleEnabled;
    }

    public void setAuthenticationModuleEnabled(
            boolean authenticationModuleEnabled) {
        this.authenticationModuleEnabled = authenticationModuleEnabled;
    }

    public void setAuthorizationModuleEnabled(boolean authorizationModuleEnabled) {
        this.authorizationModuleEnabled = authorizationModuleEnabled;
    }

    public void setCellRevision(String cellRevision) {
        this.cellRevision = cellRevision;
    }

    public void setFirewallModuleEnabled(boolean firewallModuleEnabled) {
        this.firewallModuleEnabled = firewallModuleEnabled;
    }
}
