package com.hamshif.sensors.views;

import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.hamshif.common.sensors.RXSensorManager;
import com.hamshif.sensors.R;
import com.hamshif.sensors.databinding.ActivitySensorBinding;
import com.hamshif.sensors.interfaces.SensorSetter;
import com.hamshif.sensors.model.SomatixSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SensorActivity extends AppCompatActivity implements SensorSetter {

    private static final String TAG = SensorActivity.class.getSimpleName();

    private RXSensorManager rxSensorManager;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable focusedSensorFlowable;

    private TextView tv_sensor_content;

    private RecyclerView recyclerView;
    private SensorAdapter sensorAdapter;
    private List<SomatixSensor> sensorList = new ArrayList<SomatixSensor>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySensorBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_sensor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        Log.d(TAG, TAG + "");

        recyclerView = binding.rvSensors;
        tv_sensor_content = binding.tvSensorContent;

        rxSensorManager = RXSensorManager.getInstance(this);

        setSensor(Sensor.TYPE_ACCELEROMETER);

        populateSensorList();
    }


    private void populateSensorList() {
        List<Sensor> sensors = rxSensorManager.getSensorList();

        for (Sensor s: sensors){

            sensorList.add(new SomatixSensor(s, R.drawable.accelerometer));
        }

        sensorAdapter = new SensorAdapter(sensorList, this);
        RecyclerView.LayoutManager layoutManagerayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManagerayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(sensorAdapter);
    }


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void setSensor(int sensorType) {

        if(focusedSensorFlowable != null){
            focusedSensorFlowable.dispose();
            compositeDisposable.remove(focusedSensorFlowable);
        }

        focusedSensorFlowable = rxSensorManager.listen(sensorType)
                .sample(1200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (sensorEvent) -> {tv_sensor_content.setText(SomatixSensor.sensorEventToString(sensorEvent));},
                        (error) -> {},
                        () -> {tv_sensor_content.setText("Subscribed");}
                );

        compositeDisposable.add(focusedSensorFlowable);
    }
}
