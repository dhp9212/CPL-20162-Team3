package kr.soen.btapp_client;

import android.bluetooth.BluetoothAdapter;
import java.io.Serializable;

public class BTASerial implements Serializable{
    static public BluetoothAdapter BTAdapter;

    public BTASerial(BluetoothAdapter BTAdapter)
    {
        this.BTAdapter = BTAdapter;
    }

    public BluetoothAdapter getBTAdapter()
    {
        return BTAdapter;
    }
}
