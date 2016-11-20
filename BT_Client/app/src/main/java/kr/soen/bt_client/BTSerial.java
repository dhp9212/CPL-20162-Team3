package kr.soen.bt_client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.io.Serializable;

public class BTSerial implements Serializable{
    static public BluetoothAdapter BTAdapter;
    static public BluetoothDevice BTDevice;

    public BTSerial(BluetoothAdapter BTAdapter)
    {
        this.BTAdapter = BTAdapter;
    }

    public BTSerial(BluetoothDevice BTDevice)
    {
        this.BTDevice = BTDevice;
    }

    public BluetoothAdapter getBTAdapter()
    {
        return BTAdapter;
    }

    public BluetoothDevice getBTDevice()
    {
        return BTDevice;
    }
}