/*
 * Copyright 2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test.gwt.client;

import org.restlet.gwt.Callback;
import org.restlet.gwt.Client;
import org.restlet.gwt.data.Protocol;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 */
public class TestClient implements EntryPoint {

    public void onModuleLoad() {
        GWT.log("Restlet module loaded.", null);
        final Button button = new Button("Restlet, Fetch!");
        final Label label = new Label();

        button.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                new Client(Protocol.HTTP).put(
                        "http://localhost:8888/demo/hello.txt", "entity",
                        new Callback() {
                            @Override
                            public void onEvent(Request request,
                                    Response response) {
                                try {
                                    label.setText(response.getEntity()
                                            .getText());
                                } catch (final Exception ioException) {
                                    GWT.log("Restlet I/O failed", ioException);
                                }
                            }

                        });
            }
        });

        RootPanel.get("slot1").add(button);
        RootPanel.get("slot2").add(label);
    }
}
