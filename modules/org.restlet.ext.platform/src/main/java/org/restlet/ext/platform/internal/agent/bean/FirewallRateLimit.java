/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.platform.internal.agent.bean;

/**
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class FirewallRateLimit {

    private boolean defaultRateLimit;

    private String group;

    private String id;

    private String name;

    private int period;

    private int rateLimit;

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPeriod() {
        return period;
    }

    public int getRateLimit() {
        return rateLimit;
    }

    public boolean isDefaultRateLimit() {
        return defaultRateLimit;
    }

    public void setDefaultRateLimit(boolean defaultRateLimit) {
        this.defaultRateLimit = defaultRateLimit;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setRateLimit(int rateLimit) {
        this.rateLimit = rateLimit;
    }
}
