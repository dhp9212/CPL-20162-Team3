package kr.soen.btapp_client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class BTSerial implements Serializable{
    static public BluetoothAdapter BTAdapter;
    static public BluetoothDevice BTDevice;
    static public String name;
    static public String address;

    public BTSerial(BluetoothAdapter BTAdapter)
    {
        this.BTAdapter = BTAdapter;
    }

    public BTSerial(BluetoothDevice BTDevice, String name, String address)
    {
        this.BTDevice = BTDevice;
        this.name = name;
        this.address = address;
    }

    public BluetoothAdapter getBTAdapter()
    {
        return BTAdapter;
    }

    public BluetoothDevice getBTDevice()
    {
        return BTDevice;
    }

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }
}
