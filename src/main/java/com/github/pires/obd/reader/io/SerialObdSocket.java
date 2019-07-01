package com.github.pires.obd.reader.io;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.SerialInputStream;
import com.felhr.usbserial.SerialOutputStream;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by speedfox on 3/22/16.
 */
public class SerialObdSocket implements ObdSocket {

    private static final String TAG = SerialObdSocket.class.getName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    UsbSerialInterface iface;
    Context ctx;
    SerialInputStream inStream;
    SerialOutputStream outStream;
    UsbManager usbManager;
    UsbDevice dev;
    BroadcastReceiver permRec;


    public SerialObdSocket(UsbDevice dev, Context ctx){
        usbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        this.ctx = ctx;
        this.dev = dev;
        this.inStream = null;
        this.outStream = null;
        permRec = null;

        //this.dev = dev;
        if(usbManager.hasPermission(dev))
        {
            initUsb();
        }
        else
        {
            permRec = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (SerialObdSocket.this.dev.equals(device))
                    {
                        if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            SerialObdSocket.this.initUsb();
                        }
                        else {
                            Log.e(TAG, device.getDeviceName() + " is not the device we are looking for " + SerialObdSocket.this.dev.getDeviceName());
                        }
                    }
                    else {
                        String devName = (null == device) ? "(null device)" : device.getDeviceName();
                        Log.e(TAG, devName + " is not the device we are looking for " + SerialObdSocket.this.dev.getDeviceName());
                    }

                    SerialObdSocket.this.ctx.unregisterReceiver(permRec);
                    permRec = null;

                }

            };

            PendingIntent permissionIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), 0);
            Log.e(TAG, "Got permission intent " + ((permissionIntent== null)? "{null object}" : permissionIntent.toString()));
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            ctx.registerReceiver(permRec, filter);
            usbManager.requestPermission(dev, permissionIntent);

        }


    }

    private void initUsb()
    {
        UsbDeviceConnection con = usbManager.openDevice(this.dev);
        iface = UsbSerialDevice.createUsbSerialDevice(this.dev, con);
        iface.open(); //I think this needs be to be BEFORE setting any flow control.
        //If this all gets into live code, yes I know that this needs to be set in settings.
        iface.setBaudRate(38400);
        iface.setDataBits(UsbSerialInterface.DATA_BITS_8);
        iface.setParity(UsbSerialInterface.PARITY_NONE);
        iface.setStopBits(UsbSerialInterface.STOP_BITS_1);
        iface.setFlowControl(UsbSerialInterface.FLOW_CONTROL_XON_XOFF);
        iface.setDTR(true);
        iface.setRTS(true);


        Log.e(TAG, "Connected to "  + this.dev.getDeviceName() + " Type " +this.iface.getClass().getName());
        inStream = new SerialInputStream(iface);
        outStream = new SerialOutputStream(iface);
    }

    @Override
    public boolean isConnected() {
        return null != iface;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return outStream;
    }

    @Override
    public void close() throws IOException {
        if(null != iface) {
            iface.close();
        }

        if(null!= permRec)
        {
            ctx.unregisterReceiver(permRec);
            permRec = null;
        }
    }
}
