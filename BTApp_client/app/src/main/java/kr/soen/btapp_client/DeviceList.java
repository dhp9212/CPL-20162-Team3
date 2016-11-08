package kr.soen.btapp_client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends AppCompatActivity  implements AdapterView.OnItemClickListener {
    ArrayList<String> mArDevice;
    ListView mListDevice;
    static public BluetoothAdapter mmmBTA;
    static public BluetoothDevice mmmBTD = null;
    static final int RESULT_DEVICE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        Intent myIntent = getIntent();
        BTSerial BTAdapter = (BTSerial)myIntent.getSerializableExtra("BTAdapter");

        mmmBTA = BTAdapter.getBTAdapter();

        initListView();

        getParedDevice();
    }

    public void initListView()
    {
        mArDevice = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mArDevice);

        mListDevice = (ListView)findViewById(R.id.listDevice);
        mListDevice.setAdapter(adapter);
        mListDevice.setOnItemClickListener(this);
    }

    public void getParedDevice() {

        Set<BluetoothDevice> devices = mmmBTA.getBondedDevices();

        for( BluetoothDevice device : devices )
        {
            addDeviceToList(device.getName(), device.getAddress());
        }

        startFindDevice();
    }

    public void addDeviceToList(String name, String address)
    {
        String deviceInfo = name + " - " + address;
        Log.d("tag1", "Device Find: " + deviceInfo);
        mArDevice.add(deviceInfo);

        ArrayAdapter adapter = (ArrayAdapter)mListDevice.getAdapter();
        adapter.notifyDataSetChanged();
    }

    public void startFindDevice()
    {
        stopFindDevice();

        mmmBTA.startDiscovery();

        registerReceiver(mBlueRecv, new IntentFilter( BluetoothDevice.ACTION_FOUND ));
    }

    public void stopFindDevice()
    {
        if( mmmBTA.isDiscovering() ) {
            mmmBTA.cancelDiscovery();
            unregisterReceiver(mBlueRecv);
        }
    }

    BroadcastReceiver mBlueRecv = new BroadcastReceiver() {
        public void  onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );

                if( device.getBondState() != BluetoothDevice.BOND_BONDED )
                    addDeviceToList(device.getName(), device.getAddress());
            }
        }
    };

    public void onItemClick(AdapterView parent, View view, int position, long id)
    {
        String strItem = mArDevice.get(position);

        int pos = strItem.indexOf(" - ");
        if( pos <= 0 ) return;

        String name = strItem.substring(0, pos);
        String address = strItem.substring(pos + 3);

        stopFindDevice();

        mmmBTD = mmmBTA.getRemoteDevice(address);

        Intent data = new Intent();
        data.putExtra("BTDevice", new BTSerial(mmmBTD, name, address));
        setResult(RESULT_DEVICE, data);

        finish();
    }

    public void onDestroy() {
        super.onDestroy();

        stopFindDevice();
    }
}
