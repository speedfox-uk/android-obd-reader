package com.github.pires.obd.reader.io;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by speedfox on 3/16/16.
 */
public class BluetoothObdSocket implements ObdSocket
{
    BluetoothSocket sock;

    public BluetoothObdSocket(BluetoothSocket s)
    {
        sock = s;
    }

    public boolean isConnected()
    {
        return sock.isConnected();
    }

    public InputStream getInputStream() throws IOException {
        return sock.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return sock.getOutputStream();
    }

    public void close() throws IOException {
        sock.close();
    }
}
