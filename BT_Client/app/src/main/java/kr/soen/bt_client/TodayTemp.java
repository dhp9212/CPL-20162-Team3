package kr.soen.bt_client;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class TodayTemp extends AppCompatActivity {
    float[] tempTokens = new float[8];
    DecimalFormat form = new DecimalFormat("#.#");

    TextView currentDate;
    LineChart tempchart;
    TextView maxTemp;
    TextView avgTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_temp);

        currentDate = (TextView)findViewById(R.id.date);//현재 날짜 초기화
        currentDate.setTypeface(Typeface.createFromAsset(getAssets(), "NanumBarunGothicUltraLight.ttf"));
        tempchart = (LineChart)findViewById(R.id.chart);//그래프 초기화
        maxTemp = (TextView)findViewById(R.id.maxtemp);//최고 온도 초기화
        avgTemp = (TextView)findViewById(R.id.avgtemp);//평균 온도 초기화


        Intent myIntent = getIntent();
        String todayTemp = myIntent.getStringExtra("TempString");

        StringTokenizer str = new StringTokenizer(todayTemp, "/");

        int countTokens = str.countTokens();

        for (int i = 0; i < countTokens; i++)
        {
            tempTokens[i] = Float.parseFloat(str.nextToken());
        }


        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yy/MM/dd");
        currentDate.setText(CurDateFormat.format(date));


        ArrayList<Entry> valsTemp = new ArrayList<>();
        for (int i = 0; i < countTokens; i++)
        {
            valsTemp.add(new Entry(tempTokens[i], i));
        }

        LineDataSet setTemp = new LineDataSet(valsTemp, "온도");
        setTemp.setValueTextSize(15);

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < 8; i++)
        {
            xVals.add(i*3 + "시");
        }

        LineData data = new LineData(xVals, setTemp);

        YAxis leftAxis = tempchart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis rightAxis = tempchart.getAxisRight();
        rightAxis.setEnabled(false);

        XAxis xAxis = tempchart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        tempchart.setData(data);
        tempchart.invalidate();


        float max_f = 0;
        for (float token : tempTokens )
        {
            if(max_f < token)
            {
                max_f = token;
            }
        }

        String max_s = Float.toString(max_f);
        maxTemp.append(max_s);


        float sum = 0;
        for (float token : tempTokens )
        {
            sum += token;
        }
        float avg = sum/countTokens;

        avgTemp.append(form.format(avg));
    }
}


