package org.restlet.ext.apispark.internal.agent.bean;

/**
 * @author Manuel Boillod
 */
@SuppressWarnings("ALL")
public class ModulesSettings {

    String cellRevision;

    boolean authenticationModuleEnabled;

    boolean authorizationModuleEnabled;

    boolean firewallModuleEnabled;

    boolean analyticsModuleEnabled;

    public String getCellRevision() {
        return cellRevision;
    }

    public void setCellRevision(String cellRevision) {
        this.cellRevision = cellRevision;
    }

    public boolean isAuthenticationModuleEnabled() {
        return authenticationModuleEnabled;
    }

    public void setAuthenticationModuleEnabled(boolean authenticationModuleEnabled) {
        this.authenticationModuleEnabled = authenticationModuleEnabled;
    }

    public boolean isAuthorizationModuleEnabled() {
        return authorizationModuleEnabled;
    }

    public void setAuthorizationModuleEnabled(boolean authorizationModuleEnabled) {
        this.authorizationModuleEnabled = authorizationModuleEnabled;
    }

    public boolean isFirewallModuleEnabled() {
        return firewallModuleEnabled;
    }

    public void setFirewallModuleEnabled(boolean firewallModuleEnabled) {
        this.firewallModuleEnabled = firewallModuleEnabled;
    }

    public boolean isAnalyticsModuleEnabled() {
        return analyticsModuleEnabled;
    }

    public void setAnalyticsModuleEnabled(boolean analyticsModuleEnabled) {
        this.analyticsModuleEnabled = analyticsModuleEnabled;
    }
}
