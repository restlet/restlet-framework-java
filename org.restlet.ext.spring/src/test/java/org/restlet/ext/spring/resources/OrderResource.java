/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.spring.resources;

import org.restlet.resource.Get;

public class OrderResource extends UserResource {
    String orderId;

    @Override
    public void doInit() {
        super.doInit();
        this.orderId = (String) getRequestAttributes().get("order");
    }

    @Get
    public String toString() {
        return "Order \"" + this.orderId + "\" for user \"" + this.userName + "\"";
    }

}
