package com.google.gwt.emul.java.io;

import java.io.IOException;

public abstract class Reader {

    public abstract void close() throws IOException;

    public abstract int read(char[] cbuf, int off, int len) throws IOException;

}
