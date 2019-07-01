package com.github.pires.obd.reader.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by speedfox on 3/16/16.
 */
public interface ObdSocket
{
    public boolean isConnected();

    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public void close() throws IOException;
}
