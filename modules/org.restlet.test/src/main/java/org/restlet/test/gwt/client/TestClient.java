/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.test.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 */
public class TestClient implements EntryPoint {

    public void onModuleLoad() {
        GWT.log("Restlet module loaded.", null);
        final Button button = new Button("Restlet, Fetch!");
        final Label label = new Label();

        /*
         * button.addClickListener(new ClickListener() { public void
         * onClick(Widget sender) { new Client(Protocol.HTTP).put(
         * "http://localhost:8888/demo/hello.txt", "entity", new Uniform() {
         * 
         * public void handle(Request request, Response response, Uniform
         * callback) { try { label.setText(response.getEntity() .getText()); }
         * catch (Exception ioException) { GWT.log("Restlet I/O failed",
         * ioException); } }
         * 
         * }); } });
         */

        RootPanel.get("slot1").add(button);
        RootPanel.get("slot2").add(label);
    }
}
