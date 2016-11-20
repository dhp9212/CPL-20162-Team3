package kr.soen.bt_client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.Set;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeviceList extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static public BluetoothAdapter mmBTA;
    static public BluetoothDevice mmBTD = null;
    static final int RESULT_DEVICE = 1;

    private ArrayList<String> mArDevice;
    private ListView mListDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        Intent myIntent = getIntent();
        BTSerial BTAdapter = (BTSerial)myIntent.getSerializableExtra("BTAdapter");

        mmBTA = BTAdapter.getBTAdapter();

        initListView();//listview 초기화

        getParedDevice();//페어링된 기기 불러오기
    }

    public void initListView()
    {
        // 어댑터 생성
        mArDevice = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mArDevice);

        // ListView 에 어댑터와 이벤트 리스너를 지정
        mListDevice = (ListView)findViewById(R.id.listDevice);
        mListDevice.setAdapter(adapter);
        mListDevice.setOnItemClickListener(this);
    }

    public void getParedDevice()
    {
        // 블루투스 어댑터에서 페어링된 원격 디바이스 목록을 구한다
        Set<BluetoothDevice> devices = mmBTA.getBondedDevices();

        // 디바이스 목록에서 하나씩 추출
        for( BluetoothDevice device : devices )
        {
            addDeviceToList(device.getName(), device.getAddress());// 디바이스를 목록에 추가
        }

        startFindDevice();// 원격 디바이스 검색 시작
    }

    public void addDeviceToList(String name, String address)
    {
        // ListView 와 연결된 ArrayList 에 새로운 항목을 추가
        String deviceInfo = name + " - " + address;
        Log.d("tag1", "Device Find: " + deviceInfo);
        mArDevice.add(deviceInfo);

        // 화면을 갱신한다
        ArrayAdapter adapter = (ArrayAdapter)mListDevice.getAdapter();
        adapter.notifyDataSetChanged();
    }

    public void startFindDevice()
    {
        stopFindDevice();// 원격 디바이스 검색 중지

        mmBTA.startDiscovery();// 디바이스 검색 시작

        // 원격 디바이스 검색 이벤트 리시버 등록
        registerReceiver(mBlueRecv, new IntentFilter( BluetoothDevice.ACTION_FOUND ));
    }

    public void stopFindDevice()
    {
        if( mmBTA.isDiscovering() ) // 현재 디바이스 검색 중이라면 취소한다
        {
            mmBTA.cancelDiscovery();
            unregisterReceiver(mBlueRecv); // 브로드캐스트 리시버를 등록 해제한다
        }
    }

    BroadcastReceiver mBlueRecv = new BroadcastReceiver() {
        public void  onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(BluetoothDevice.ACTION_FOUND))
            {
                // 인텐트에서 디바이스 정보 추출
                BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );

                // 페어링된 디바이스가 아니라면
                if( device.getBondState() != BluetoothDevice.BOND_BONDED )
                    addDeviceToList(device.getName(), device.getAddress());// 디바이스를 목록에 추가
            }
        }
    };

    public void onItemClick(AdapterView parent, View view, int position, long id)
    {
        String strItem = mArDevice.get(position);// 사용자가 선택한 항목의 내용을 구한다

        // 사용자가 선택한 디바이스의 주소를 구한다
        int pos = strItem.indexOf(" - ");
        if( pos <= 0 ) return;

        String address = strItem.substring(pos + 3);

        stopFindDevice();// 디바이스 검색 중지

        mmBTD = mmBTA.getRemoteDevice(address); // 상대방 디바이스를 구한다

        Intent data = new Intent();
        data.putExtra("BTDevice", new BTSerial(mmBTD));
        setResult(RESULT_DEVICE, data);

        finish();
    }

    public void onDestroy() {
        super.onDestroy();

        stopFindDevice();
    }
}
