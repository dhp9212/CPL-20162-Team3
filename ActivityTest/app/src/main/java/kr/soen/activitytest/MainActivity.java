package kr.soen.activitytest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    ArrayList<String> arr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        arr = new ArrayList<String>();
        arr.add("1번");
        arr.add("2번");
        arr.add("3번");
        arr.add("4번");
        arr.add("5번");

        Intent myIntent = new Intent(MainActivity.this, SecondActivity.class);
        myIntent.putStringArrayListExtra("arr", arr);

        startActivityForResult(myIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (requestCode) {
           case 0:
               String str = data.getStringExtra("value");

               TextView result = (TextView)findViewById(R.id.result);
               result.setText(str);

               break;
       }
    }
}
