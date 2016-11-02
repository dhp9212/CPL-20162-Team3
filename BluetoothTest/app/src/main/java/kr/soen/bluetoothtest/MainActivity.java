package kr.soen.bluetoothtest;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.util.Log;

import android.app.ProgressDialog;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity{
    public static Context mContext;

    private final static int DEVICES_DIALOG = 1;
    static final int ACTION_ENABLE_BT = 101;

    private static ProgressDialog waitDialog;

    private TextView mTextMsg;
    private EditText mSendData;
    private EditText mReceiveData;

    static final String  BLUE_NAME = "BluetoothTest";  // 접속시 사용하는 이름

    static private BluetoothAdapter mBA;
    static private BluetoothDevice mBD = null;
    static private BluetoothSocket bluetoothSocket;
    static private InputStream btIn;
    static private OutputStream btOut;
    public static AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        activity = this;

        mTextMsg = (TextView)findViewById(R.id.textMessage);
        mSendData = (EditText)findViewById(R.id.editData1);
        mReceiveData = (EditText)findViewById(R.id.editData2);

        Button mClearBtn = (Button) findViewById(R.id.btnClear);
        mClearBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendData.setText("");
                mReceiveData.setText("");
            }
        });

        Button mSendBtn = (Button) findViewById(R.id.btnSend);
        mSendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mSendData.getText().toString();
                doSend(msg);
            }
        });

        // 블루투스 사용 가능상태 판단
        boolean isBlue = canUseBluetooth();

        if( isBlue ) {
            // 페어링된 원격 디바이스 목록 구하기
            DeviceDialog();
        }
    }

     // 블루투스 사용 가능상태 판단
    public boolean canUseBluetooth() {

        // 블루투스 어댑터를 구한다
        mBA = BluetoothAdapter.getDefaultAdapter();

        // 블루투스 어댑터가 null 이면 블루투스 장비가 존재하지 않는다.
        if( mBA == null ) {
            mTextMsg.setText("This device is not implemented Bluetooth.");
            return false;
        }
        mTextMsg.setText("This device is implemented Bluetooth.");

        // 블루투스 활성화 상태라면 함수 탈출
        if( mBA.isEnabled() ) {
            mTextMsg.append("\nThis device is abled Bluetooth.");
            return true;
        }

        // 사용자에게 블루투스 활성화를 요청한다
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, ACTION_ENABLE_BT);
        return false;
    }

    // 블루투스 활성화 요청 결과 수신
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == ACTION_ENABLE_BT ) {
            // 사용자가 블루투스 활성화 승인했을때
            if( resultCode == RESULT_OK ) {
                mTextMsg.append("\nThis device is abled Bluetooth.");
                // 페어링된 원격 디바이스 목록 구하기
                DeviceDialog();
            }
            // 사용자가 블루투스 활성화 취소했을때
            else {
                mTextMsg.append("\nThis device is disabled Bluetooth.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doClose();
    }

    public void DeviceDialog()
    {
        if (activity.isFinishing()) return;

        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(DEVICES_DIALOG, "");
        alertDialog.show(fm, "");
    }

    public void doSetResultText(String text) {
        mReceiveData.setText(text);
    }

    public static void hideWaitDialog() {
    }



    static public Set<BluetoothDevice> getPairedDevices() {
        return mBA.getBondedDevices();
    }


    public void doConnect(BluetoothDevice device) {
        mBD = device;

        //Standard SerialPortService ID
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111123");

        try {

            bluetoothSocket = mBD.createRfcommSocketToServiceRecord(uuid);
            mBA.cancelDiscovery();
            new ConnectTask().execute();
        } catch (IOException e) {
            Log.e("TAG", e.toString(), e);
            mTextMsg.append("\n" + e.toString());
        }
    }


    public void doClose() {
        new CloseTask().execute();
    }


    public void doSend(String msg) {
        new SendTask().execute(msg);
    }


    private class ConnectTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Object doInBackground(Void... params) {
            try {
                bluetoothSocket.connect();
                btIn = bluetoothSocket.getInputStream();
                btOut = bluetoothSocket.getOutputStream();
            } catch (Throwable t) {
                Log.e("TAG", "connect? "+ t.getMessage() );
                doClose();
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                Log.e("TAG", result.toString(), (Throwable)result);
                mTextMsg.append("\n" + result.toString());
            } else {
                hideWaitDialog();
            }
        }
    }


    private class CloseTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                try{btOut.close();}catch(Throwable t){/*ignore*/}
                try{btIn.close();}catch(Throwable t){/*ignore*/}
                bluetoothSocket.close();
            } catch (Throwable t) {
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                Log.e("TAG", result.toString(), (Throwable)result);
                mTextMsg.append("\n" + result.toString());
            }
        }
    }


    private class SendTask extends AsyncTask<String, Void, Object> {
        @Override
        protected Object doInBackground(String... params) {
            try {

                btOut.write(params[0].getBytes());
                btOut.flush();

                byte[] buff = new byte[512];
                int len = btIn.read(buff);

                Log.e("TAG", "recv? "+ len );

                return new String(buff, 0, len);
            } catch (Throwable t) {
                doClose();
                return t;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Exception) {
                Log.e("TAG", result.toString(), (Throwable)result);
                mTextMsg.append("\n" + result.toString());
            } else {
                doSetResultText(result.toString());
            }
        }
    }
}