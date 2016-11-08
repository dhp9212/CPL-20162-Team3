package kr.soen.btapp_client;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    static public BluetoothAdapter mBTA;
    Button btBtn;
    boolean selection = false;
    Toast logMsg;
    static final int ACTION_ENABLE_BT = 101;
    static final int ACTION_DISCOVERABLE_BT = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBTA = BluetoothAdapter.getDefaultAdapter();

        btBtn = (Button) findViewById(R.id.bt_btn);
        btBtn.setOnTouchListener(this);

        if (mBTA == null) {
            logMessege("블루투스가 존재하지 않습니다.");
        } else {
            logMessege("블루투스가 존재합니다.");

            if (mBTA.isEnabled())
            {
                btBtn.setBackgroundResource(R.drawable.bt_selected);
                selection = true;

                logMessege("블루투스가 활성화되었습니다.");

                setDiscoverable();
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        Button b=(Button)v;

        int action=event.getAction();

        if(action==MotionEvent.ACTION_DOWN)
        {
            if(!selection) b.setBackgroundResource(R.drawable.bt_pressed_off);
            else b.setBackgroundResource(R.drawable.bt_press_on);
        }
        else if(action==MotionEvent.ACTION_UP){
            if(!selection)
            {
                b.setBackgroundResource(R.drawable.bt_selected);

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, ACTION_ENABLE_BT);

            }
            else
            {
                b.setBackgroundResource(R.drawable.bt);

                mBTA.disable();
                selection = false;
            }
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                logMessege("블루투스가 활성화되었습니다.");
                selection = true;

                setDiscoverable();
            } else {
                logMessege("블루투스가 활성화되지 않았습니다.");
                btBtn.setBackgroundResource(R.drawable.bt);
                selection = false;
            }
        }
        else
        {
            if (resultCode == RESULT_OK) {
                Intent myIntent = new Intent(this, Chat.class);
                myIntent.putExtra("BTAdapter", new BTSerial(mBTA));
                startActivity(myIntent);
            } else {
                logMessege("다른 기기에서의 검색을 허용하지 않았습니다.");

                Intent myIntent = new Intent(this, Chat.class);
                myIntent.putExtra("BTAdapter", new BTSerial(mBTA));
                startActivity(myIntent);
            }
        }
    }

    public void logMessege(String log) {
        logMsg = Toast.makeText(this, log, Toast.LENGTH_SHORT);
        logMsg.show();
    }

    public void setDiscoverable() {
        if (mBTA.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            return;
        }

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(intent, ACTION_DISCOVERABLE_BT);
    }
}