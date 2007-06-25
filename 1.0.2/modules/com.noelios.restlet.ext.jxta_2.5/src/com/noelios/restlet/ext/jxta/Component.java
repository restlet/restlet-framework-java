package com.noelios.restlet.ext.jxta;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public interface Component {

    public String getName();

    public void start() throws Exception;

    public void stop() throws Exception;
}