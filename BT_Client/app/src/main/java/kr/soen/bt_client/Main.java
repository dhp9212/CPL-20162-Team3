package kr.soen.bt_client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class Main extends AppCompatActivity implements OnClickListener{
    static public BluetoothAdapter mBTA;
    static public BluetoothDevice mBTD = null;
    static public BluetoothSocket mBTS;
    public InputStream mmInStream;
    public OutputStream mmOutStream;

    BackPressCloseHandler backPressCloseHandler;
    ActionBar title;
    RelativeLayout background;
    TextView btState;
    TextView tempText1;
    TextView tempText2;
    Button currenttempBtn;
    Toast logMsg;

    boolean isConnect = false;
    boolean isHandover = false;
    String taskString;
    String remoteDeviceName;
    IntentFilter filter;
    AlertDialog.Builder dialog;
    BRhandler handler;

    static final UUID BLUE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    static final String RESULT_CONNECTION = "1";
    static final String REQUEST_CURRENT_TEMP = "2";
    static final String RQUEST_TODAY_TEMP ="3";
    static final int REQUEST_DEVICE = 1;
    static final int RESULT_DEVICE = 1;
    static final int ACTION_ENABLE_BT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backPressCloseHandler = new BackPressCloseHandler(this);//뒤로가기 handler

        mBTA = BluetoothAdapter.getDefaultAdapter();//블투 adapter 초기화

        background = (RelativeLayout) findViewById(R.id.activity_main);//background layout 초가화

        //title bar 초기화
        title = getSupportActionBar();
        title.setBackgroundDrawable(new ColorDrawable(0xff4c8eff));

        //현재 블루투스 상태 표시 text 초기화
        btState = (TextView)findViewById(R.id.btstate);

        //현재 온도 표시 text 초기화
        tempText1 = (TextView) findViewById(R.id.temptext1);
        tempText1.setTypeface(Typeface.createFromAsset(getAssets(), "NanumBarunGothicUltraLight.ttf"));

        //온도 단위 표시 text 설정(이후 변화 없음)
        tempText2 = (TextView) findViewById(R.id.temptext2);
        tempText2.setTypeface(Typeface.createFromAsset(getAssets(), "NanumBarunGothicUltraLight.ttf"));

        //현재 온도 표시 button 설정
        currenttempBtn = (Button)findViewById(R.id.currenttemp_btn);
        currenttempBtn.setOnClickListener(this);

        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(BlueRecv, filter);

        if (mBTA == null)
        {
            logMessege("기기가 블루투스를 지원하지 않습니다.");
        }
        else
        {
            if (mBTA.isEnabled())
            {
                logMessege("블루투스가 이미 활성화되었습니다.");

                btState.setText("블루투스가 활성화되었습니다.");

                setDiscoverable();//블투 검색 허용
            }
            else
            {
                btState.setText("블루투스가 활성화되지 않았습니다.");

                dialog = new AlertDialog.Builder(Main.this);
                dialog.setTitle("블루투스가 활성화되지 않았습니다.");
                dialog.setMessage("블루투스를 활성화 하시겠습니까?");

                // OK 버튼 이벤트
                dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자에게 블루투스 활성화 요청
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, ACTION_ENABLE_BT);
                    }
                });

                // Cancel 버튼 이벤트
                dialog.setNegativeButton("아니요",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logMessege("블루투스 활성화가 취소되었습니다.");
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        }
    }

    // 블루투스 활성화 요청 결과 수신
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == ACTION_ENABLE_BT )
        {
            if( resultCode == RESULT_OK )// 사용자가 블루투스 활성화 승인했을때
            {
                logMessege("블루투스가 활성화되었습니다.");

                setDiscoverable();//블투 검색 허용

                btState.setText("기기와 연결되지 않았습니다.");
            }
            else // 사용자가 블루투스 활성화 취소했을때
            {
                logMessege("블루투스가 활성화되지 않았습니다.");
            }
        }
        else if(requestCode == REQUEST_DEVICE)
        {
            if (resultCode == RESULT_DEVICE)
            {
                BTSerial BTDevice = (BTSerial)data.getSerializableExtra("BTDevice");

                mBTD = BTDevice.getBTDevice();

                remoteDeviceName = mBTD.getName();
                logMessege("'" + remoteDeviceName + "' 기기와 연결을 시도합니다.");

                doConnect(mBTD);//디바이스 연결
            }
        }
    }

    public void setDiscoverable()
    {
        if (mBTA.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            return;
        }

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(intent);
    }

    public void doConnect(BluetoothDevice device) {
        mBTD = device;

        try {
            mBTS = mBTD.createRfcommSocketToServiceRecord(BLUE_UUID);
            mBTA.cancelDiscovery();

            new ConnectTask().execute();//connect 쓰레드 시작
        } catch (IOException e) {
            logMessege(e.toString());
        }
    }

    public void doHandoverConnect() {
        isHandover = true;
        new ConnectTask().execute();
    }

    public void doHandover() {
        Set<BluetoothDevice> devices = mBTA.getBondedDevices();

        try {

            for( BluetoothDevice device : devices )
            {
                mBTD = mBTA.getRemoteDevice(device.getAddress());
                mBTS = mBTD.createRfcommSocketToServiceRecord(BLUE_UUID);

                try {
                    mBTS.connect();
                } catch (IOException e){
                    mBTD = null;
                    mBTS = null;
                    continue;
                }
                break;
            }

            if (mBTS != null && mBTS.isConnected())
            {
                mBTA.cancelDiscovery();
            }

        } catch (IOException e) {
            //logMessege(e.toString());
        }
    }

    public void doCommu(String msg) {
        new CommuTask().execute(msg);//통신 쓰레드 시작
    }

    public void doClose() {
        new CloseTask().execute();//종료쓰레드 시작
    }

    public class ConnectTask extends AsyncTask<Void, Void, Object> {
        ProgressDialog asyncDialog = new ProgressDialog(Main.this);

        @Override
        protected void onPreExecute() {
            //연결을 기다림
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("연결 중입니다..");
            asyncDialog.show();
        }

        @Override
        protected Object doInBackground(Void... params) {
            if (isHandover)
            {
                doHandover();
            }

            try {
                if (!isHandover) {
                    mBTS.connect();
                }
                else
                {
                    isHandover = false;
                }
                mmInStream = mBTS.getInputStream();
                mmOutStream = mBTS.getOutputStream();

                byte[] inbuff = new byte[1024];
                clearArray(inbuff);//buffer clear
                int len;
                len = mmInStream.read(inbuff);

                return new String(inbuff, 0, len, "UTF-8");
            } catch (Throwable t) {
                Log.e( "TAG", "connect? "+ t.getMessage() );
                doClose();//쓰레드 종료
                return t;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable)
            {
                asyncDialog.dismiss();

                logMessege("연결이 비정상적으로 종료되었습니다.");
            }
            else
            {
                if(result.toString().equals(RESULT_CONNECTION))
                {
                    asyncDialog.dismiss();

                    isConnect = true;

                    logMessege("연결이 정상적으로 완료되었습니다.");

                    String str = "현재 연결된 기기 : " + remoteDeviceName;
                    btState.setText(str);
                }
                else
                {
                    asyncDialog.dismiss();

                    logMessege("연결이 비정상적으로 종료되었습니다.");
                }
            }
        }
    }

    BroadcastReceiver BlueRecv = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            handler = new BRhandler();

            String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_OFF)
                {
                    if (!isConnect)
                    {
                        handler.sendEmptyMessage(1);
                    }
                    else
                    {
                        handler.sendEmptyMessage(2);
                    }

                }
                else if (state == BluetoothAdapter.STATE_ON)
                {
                    handler.sendEmptyMessage(3);
                }
            }
            if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            {
                handler.sendEmptyMessage(4);
            }


        }
    };

    class BRhandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 1:
                    logMessege("블루투스가 비활성화 되었습니다.");
                    btState.setText("블루투스가 활성화되지 않았습니다.");
                    dialog.show();
                    break;

                case 2:
                    logMessege("블루투스가 비활성화 되었습니다.");
                    btState.setText("블루투스가 활성화되지 않았습니다.");
                    logMessege(remoteDeviceName + "와의 연결이 끊어졌습니다.");
                    doClose();
                    dialog.show();
                    break;

                case 3:
                    logMessege("블루투스가 활성화되었습니다.");
                    btState.setText("기기와 연결되지 않았습니다.");
                    break;

                case 4:
                    logMessege(remoteDeviceName + "와의 연결이 끊어졌습니다.");
                    doClose();
                    doHandoverConnect();
                    break;
            }
        }
    }

    private class CommuTask extends AsyncTask<String, String, Object> {
        @Override
        protected Object doInBackground(String... params) {
            try {
                taskString = params[0];

                mmOutStream.write(taskString.getBytes("UTF-8"));
                mmOutStream.flush();

                byte[] inbuff = new byte[1024];
                clearArray(inbuff);//buffer clear
                int len;
                len = mmInStream.read(inbuff);

                return new String(inbuff, 0, len, "UTF-8");
            } catch (Throwable t) {
                doClose();
                return t;
            }
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
        if(taskString.equals(REQUEST_CURRENT_TEMP)) {
            setTemp(text);
        }
        else if(taskString.equals(RQUEST_TODAY_TEMP))
        {
            Intent myIntent2 = new Intent(Main.this, TodayTemp.class);
            myIntent2.putExtra("TempString", text);
            startActivity(myIntent2);
        }
    }

    private class CloseTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                try{mmOutStream.close();}catch(Throwable t){/*ignore*/}
                try{mmInStream.close();}catch(Throwable t){/*ignore*/}
                mBTS.close();
                isConnect = false;
            } catch (Throwable t) {
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                logMessege(result.toString());
            } else {
                setTemp("--");
                btState.setText("기기와 연결되지 않았습니다.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menu_select:
                if (mBTA != null && !isConnect)
                {
                    Intent myIntent1 = new Intent(Main.this, DeviceList.class);
                    myIntent1.putExtra("BTAdapter", new BTSerial(mBTA));
                    startActivityForResult(myIntent1, REQUEST_DEVICE);
                    return true;
                }
                else if (mBTA == null)
                {
                    logMessege("기기가 블루투스를 지원하지 않습니다.");
                    return true;
                }

                logMessege("이미 다른 기기와 연결되었습니다.");
                return true;

            case R.id.menu_avg:
                if(isConnect) {
                    doCommu(RQUEST_TODAY_TEMP);
                    return true;
                }
                else if (mBTA == null)
                {
                    logMessege("기기가 블루투스를 지원하지 않습니다.");
                    return true;
                }

                logMessege("기기와 연결되지 않았습니다.");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.currenttemp_btn:
                if(isConnect)
                {
                    doCommu(REQUEST_CURRENT_TEMP);
                    break;
                }
                else if (mBTA == null)
                {
                    logMessege("기기가 블루투스를 지원하지 않습니다.");
                    break;
                }

                logMessege("기기와 연결되지 않았습니다.");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public void setTemp(String temp_s) {

        if(!temp_s.equals("--") && Float.parseFloat(temp_s) >= 45)
        {
            title.setBackgroundDrawable(new ColorDrawable(0xffff4c4c));
            background.setBackgroundColor(Color.parseColor("#ff4c4c"));
            tempText1.setText(temp_s);
        }
        else if(!temp_s.equals("--") && Float.parseFloat(temp_s) >= 35)
        {
            title.setBackgroundDrawable(new ColorDrawable(0xffffa54c));
            background.setBackgroundColor(Color.parseColor("#ffa54c"));
            tempText1.setText(temp_s);
        }
        else if(!temp_s.equals("--") && Float.parseFloat(temp_s) >= 25)
        {
            title.setBackgroundDrawable(new ColorDrawable(0xffffdc4c));
            background.setBackgroundColor(Color.parseColor("#ffdc4c"));
            tempText1.setText(temp_s);
        }
        else if(!temp_s.equals("--") && Float.parseFloat(temp_s) >= 15)
        {
            title.setBackgroundDrawable(new ColorDrawable(0xff86ea6e));
            background.setBackgroundColor(Color.parseColor("#86ea6e"));
            tempText1.setText(temp_s);
        }
        else
        {
            title.setBackgroundDrawable(new ColorDrawable(0xff4c8eff));
            background.setBackgroundColor(Color.parseColor("#4c8eff"));
            tempText1.setText(temp_s);
        }
    }

    public void logMessege(String log) {
        logMsg = Toast.makeText(this, log, Toast.LENGTH_SHORT);
        logMsg.show();
    }

    public void clearArray(byte[] buff) {
        for (int i = 0; i < buff.length; i++)
        {
            buff[i] = 0;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(isConnect) {
            doClose();
        }

        unregisterReceiver(BlueRecv);

        if(mBTA != null && mBTA.isEnabled())
        {
            mBTA.disable();
        }
    }
}
