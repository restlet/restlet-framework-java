package com.noelios.restlet.ext.jxta;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public interface StreamConnection extends Connection {

    public InputStream getInputStream();

    public OutputStream getOutputStream();

    public void receive(Socket socket);
}