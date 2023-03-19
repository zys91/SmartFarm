package com.seu.smartfarm.modules.widget.dialog;

import static com.seu.smartfarm.activity.LoginActivity.What_NetworkErr;
import static com.seu.smartfarm.app.MainApplication.app_did;
import static com.seu.smartfarm.app.MainApplication.app_token;
import static com.seu.smartfarm.app.MainApplication.app_uid;
import static com.seu.smartfarm.fragment.account.Account.What_UserError;
import static com.seu.smartfarm.fragment.videolist.VideoList.What_GetDataDone;
import static com.seu.smartfarm.fragment.videolist.VideoList.What_GetDataFail;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.seu.smartfarm.R;
import com.seu.smartfarm.modules.userapi.TGetHistoricalData;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineChartDialog extends DialogFragment implements View.OnClickListener, OnChartValueSelectedListener {
    private LineChart chart;
    private TextView title;
    private String titleStr;
    private String dataType;
    private String period;
    private ImageView iv;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case What_NetworkErr:
                    Toast.makeText(getActivity(), "网络连接异常，请检查网络连接！", Toast.LENGTH_SHORT).show();
                    break;
                case What_UserError:
                    Toast.makeText(getActivity(), "用户登录已过期，请重新登录！", Toast.LENGTH_SHORT).show();
                    break;
                case What_GetDataDone:
                    ArrayList<Entry> values = new ArrayList<>();
                    int i = 0;
                    Map<String, String> res = (Map<String, String>) msg.obj;
                    Pattern p = Pattern.compile("(?<=\\()[^,\\)]+");
                    Matcher m = p.matcher(res.get("data"));
                    while (m.find()) {
                        values.add(new Entry(i, Float.parseFloat(m.group())));
                        i++;
                    }
                    setData(values);
                    break;
                case What_GetDataFail:
                    break;
            }
            return false;
        }
    });

    public LineChartDialog(String title, String dataType, String period) {
        this.titleStr = title;
        this.dataType = dataType;
        this.period = period;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置背景透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_chart, null);
        iv = view.findViewById(R.id.iv_unbind_quit);
        chart = view.findViewById(R.id.lineChart);
        title = view.findViewById(R.id.chartTitle);
        title.setText(titleStr);
        iv.setOnClickListener(this);
        initLineChart();
        Thread aGetDataThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Map<String, String> GetDataRes = TGetHistoricalData.GetHistoricalData(getActivity(), app_uid, app_token, app_did, dataType, period);
                Message msg = new Message();
                if (GetDataRes != null) {
                    if (Objects.equals(GetDataRes.get("status"), "1")) {
                        msg.what = What_GetDataDone;
                        msg.obj = GetDataRes;
                    } else if (Objects.equals(GetDataRes.get("status"), "2"))
                        msg.what = What_GetDataFail;
                    else if (Objects.equals(GetDataRes.get("status"), "0"))
                        msg.what = What_UserError;
                } else
                    msg.what = What_NetworkErr;
                mHandler.sendMessage(msg);
            }
        };
        aGetDataThread.setDaemon(true);
        aGetDataThread.start();
        builder.setView(view);
        return builder.create();
    }

    private void initLineChart() {
        chart.setViewPortOffsets(0, 0, 0, 0);
        chart.setBackgroundColor(Color.rgb(104, 241, 175));
        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(500);
        XAxis x = chart.getXAxis();
//        x.setLabelCount(12, false);
//        x.setAvoidFirstLastClipping(true);
//        x.setTextColor(Color.BLACK);
//        x.setPosition(XAxis.XAxisPosition.BOTTOM);
//        x.setDrawGridLines(false);
//        x.setGranularity(1f);
//        x.setAxisLineColor(Color.WHITE);
        YAxis y = chart.getAxisLeft();
        y.setLabelCount(8, false);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setGranularity(0.5f);
        y.setTextSize(12f);
        y.setAxisLineColor(Color.WHITE);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateXY(2000, 2000);
        // don't forget to refresh the drawing
        chart.invalidate();
    }

    private void setData(ArrayList<Entry> values) {
        LineDataSet set1;
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setCircleRadius(4f);
            set1.setCircleColor(Color.WHITE);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.WHITE);
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });
            // create a data object with the data sets
            LineData data = new LineData(set1);
            data.setValueTextSize(10f);
            data.setDrawValues(false);
            // set data
            chart.setData(data);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_unbind_quit) {
            dismiss();
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
