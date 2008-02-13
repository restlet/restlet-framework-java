package com.noelios.restlet.ext.shell.controller;

class DispatcherHelpers {

    public static String[] tokenize(String string) {
        if (string == null) {
            return new String[1];
        } else {
            return string.trim().split("\\p{Space}+");
        }
    }
}
