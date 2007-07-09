package com.noelios.restlet.ext.jxta;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import net.jxta.socket.JxtaSocket;

import org.restlet.data.Request;

import com.noelios.restlet.http.StreamClientCall;

public class JxtaClientCall extends StreamClientCall {

    public JxtaClientCall(JxtaClientHelper helper, Request request) {
        super(helper, request);
    }

    @Override
    public JxtaClientHelper getHelper() {
        return (JxtaClientHelper) super.getHelper();
    }

    @Override
    public Socket createSocket(String hostDomain, int hostPort)
            throws UnknownHostException, IOException {
        return new JxtaSocket(getHelper().getPeerGroup(), getHelper().getPipeAdvertisement());
    }

}
