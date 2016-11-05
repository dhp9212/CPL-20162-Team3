package kr.soen.btapp_client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class DeviceList extends AppCompatActivity  implements AdapterView.OnItemClickListener, View.OnClickListener {
    ArrayList<String> mArDevice;
    ListView mListDevice;
    TextView mTextMsg;
    EditText mEditData;
    Button mSendBtn;
    ScrollView mScrollView;
    static public BluetoothAdapter mmBTA;
    Toast logMsg;
    int count;
    static final UUID BLUE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    static public BluetoothDevice mmBTD = null;
    static public BluetoothSocket mmBTS;
    public InputStream mmInStream;
    public OutputStream mmOutStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mTextMsg = (TextView)findViewById(R.id.textMessage);
        mTextMsg.setMovementMethod(new ScrollingMovementMethod());
        mEditData = (EditText)findViewById(R.id.editData);
        mEditData.setOnClickListener(this);
        mSendBtn = (Button)findViewById(R.id.btnSend);
        mSendBtn.setOnClickListener(this);
        mScrollView = (ScrollView)findViewById(R.id.scroll);

        Intent myIntent = getIntent();
        BTASerial BTAdapter = (BTASerial)myIntent.getSerializableExtra("BTAdapter");

        mmBTA = BTAdapter.getBTAdapter();

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

        Set<BluetoothDevice> devices = mmBTA.getBondedDevices();

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

        mmBTA.startDiscovery();

        registerReceiver(mBlueRecv, new IntentFilter( BluetoothDevice.ACTION_FOUND ));
    }

    public void stopFindDevice()
    {
        if( mmBTA.isDiscovering() ) {
            mmBTA.cancelDiscovery();
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
        count = 0;
        String strItem = mArDevice.get(position);

        int pos = strItem.indexOf(" - ");
        if( pos <= 0 ) return;

        String name = strItem.substring(0, pos);
        String address = strItem.substring(pos + 3);

        String msg = "선택한 기기의 이름 : " + name + "\n선택한 기기의 주소 : " + address;
        mTextMsg.setText(msg);

        stopFindDevice();

        mmBTD = mmBTA.getRemoteDevice(address);

        doConnect(mmBTD);
    }

    public void doConnect(BluetoothDevice device) {
        mmBTD = device;

        try {
            mmBTS = mmBTD.createRfcommSocketToServiceRecord(BLUE_UUID);
            mmBTA.cancelDiscovery();
            new ConnectTask().execute();
        } catch (IOException e) {
            logMessege(e.toString());
        }
    }

    private class ConnectTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Object doInBackground(Void... params) {
            try {
                mmBTS.connect();
                mmInStream = mmBTS.getInputStream();
                mmOutStream = mmBTS.getOutputStream();
            } catch (Throwable t) {
                Log.e( "TAG", "connect? "+ t.getMessage() );
                doClose();
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                logMessege(result.toString());
            } else {
                hideWaitDialog();
            }
        }
    }

    public static void hideWaitDialog() {
    }

    public void doClose() {
        new CloseTask().execute();
    }

    private class CloseTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                try{mmOutStream.close();}catch(Throwable t){/*ignore*/}
                try{mmInStream.close();}catch(Throwable t){/*ignore*/}
                mmBTS.close();
            } catch (Throwable t) {
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                logMessege(result.toString());
            }
        }
    }

    private class SendTask extends AsyncTask<String, String, Object> {
        @Override
        protected Object doInBackground(String... params) {
            try {
                String taskString = params[0];

                publishProgress(taskString);

                byte[] outbuff = taskString.getBytes();
                mmOutStream.write(outbuff);
                mmOutStream.flush();

                byte[] inbuff = new byte[1024];
                int len = mmInStream.read(inbuff);

                Log.e( "TAG", "recv? "+ len );

                return new String(inbuff, 0, len);
            } catch (Throwable t) {
                doClose();
                return t;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            mTextMsg.append("\n" + ++count + ". 송신 메시지 : " + values[0]);

            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Exception) {
                logMessege(result.toString());
            } else {
                doSetResultText(result.toString());
            }
        }
    }

    public void doSetResultText(String text) {
        mTextMsg.append("\n" + count + ". 수신 메시지 : " + text);

        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void doSend(String msg) {
        SystemClock.sleep(1000);
        new SendTask().execute(msg);
    }

    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.editData :
            {
                mEditData.setText("");
            }
            case R.id.btnSend :
            {
                String strBuf = mEditData.getText().toString();
                if( strBuf.length() < 1 ) return;
                mEditData.setText("");
                doSend(strBuf);
                break;
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();

        stopFindDevice();
    }

    public void logMessege(String log)
    {
        logMsg = Toast.makeText(this, log, Toast.LENGTH_SHORT);
        logMsg.show();
    }
}
