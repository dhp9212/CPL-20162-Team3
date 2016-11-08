package kr.soen.btapp_client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Chat extends AppCompatActivity implements View.OnClickListener{
    TextView mTextMsg;
    EditText mEditData;
    Button mSendBtn;
    ScrollView mScrollView;
    static public BluetoothAdapter mmBTA;
    Toast logMsg;
    int count = 0;
    boolean connect = false;
    static final UUID BLUE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    static public BluetoothDevice mmBTD = null;
    static public BluetoothSocket mmBTS;
    public InputStream mmInStream;
    public OutputStream mmOutStream;
    static final int REQUEST_DEVICE = 1;
    static final int RESULT_DEVICE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mTextMsg = (TextView)findViewById(R.id.textMessage);
        mEditData = (EditText)findViewById(R.id.editData);
        mEditData.setOnClickListener(this);
        mSendBtn = (Button)findViewById(R.id.btnSend);
        mSendBtn.setOnClickListener(this);
        mScrollView = (ScrollView)findViewById(R.id.scroll);

        Intent myIntent = getIntent();
        BTSerial BTAdapter = (BTSerial)myIntent.getSerializableExtra("BTAdapter");

        mmBTA = BTAdapter.getBTAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menu_select:
                Intent myIntent = new Intent(Chat.this, DeviceList.class);
                myIntent.putExtra("BTAdapter", new BTSerial(mmBTA));
                startActivityForResult(myIntent, REQUEST_DEVICE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DEVICE) {
            if (resultCode == RESULT_DEVICE) {
                BTSerial BTDevice = (BTSerial)data.getSerializableExtra("BTDevice");
                mmBTD = BTDevice.getBTDevice();
                String name = BTDevice.getName();
                String address = BTDevice.getAddress();

                String msg = " 선택한 기기의 이름 : " + name + "\n 선택한 기기의 주소 : " + address + "\n ";
                mTextMsg.setText(msg);

                doConnect(mmBTD);

                connect = true;
            }
        }
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

                byte[] outbuff = taskString.getBytes("UTF-8");
                mmOutStream.write(outbuff);
                mmOutStream.flush();

                byte[] inbuff = new byte[1024];
                int len;
                while (true)
                {
                    if((len = mmInStream.read(inbuff)) != -1)
                    {
                        break;
                    }
                }

                return new String(inbuff, 0, len, "UTF-8");
            } catch (Throwable t) {
                doClose();
                return t;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            mTextMsg.append(++count + ". 송신 메시지 : " + values[0] + "\n ");
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
        mTextMsg.append(count + ". 수신 메시지 : " + text + "\n ");
        mScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void doSend(String msg) {
        new SendTask().execute(msg);
    }

    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.editData :
            {
                mEditData.setText("");
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                break;
            }
            case R.id.btnSend :
            {
                if(connect) {
                    String strBuf = mEditData.getText().toString();
                    if(!strBuf.equals(""))
                    {
                        mEditData.setText("");
                        doSend(strBuf);
                    }
                    break;
                }

                break;
            }
        }
    }

    public void logMessege(String log)
    {
        logMsg = Toast.makeText(this, log, Toast.LENGTH_SHORT);
        logMsg.show();
    }
}