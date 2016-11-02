package kr.soen.bluetoothtest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Set;

public class MyDialogFragment extends DialogFragment {
    private final static int DEVICES_DIALOG = 1;
    private final static int ERROR_DIALOG = 2;



    public MyDialogFragment() {

    }

    public static MyDialogFragment newInstance(int id, String text) {
        MyDialogFragment frag = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("content", text);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int id = getArguments().getInt("id");
        String content = getArguments().getString("content");
        AlertDialog.Builder alertDialogBuilder = null;

        switch(id)
        {
            case DEVICES_DIALOG:
                alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Select device");

                Set<BluetoothDevice> pairedDevices = MainActivity.getPairedDevices();
                final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
                String[] items = new String[devices.length];
                for (int i=0;i<devices.length;i++) {
                    items[i] = devices[i].getName() + devices[i].getAddress();
                }

                alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((MainActivity)MainActivity.mContext).doConnect(devices[which]);
                    }
                });
                alertDialogBuilder.setCancelable(false);
                break;

         /*       alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });
        */

        }

        return alertDialogBuilder.create();
    }
}