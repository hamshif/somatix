package com.hamshif.sensors.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hamshif.common.sensors.RXSensorManager;
import com.hamshif.common.util.DirFileUtil;
import com.hamshif.sensors.R;
import com.hamshif.sensors.model.SomatixSensor;
import com.hamshif.sensors.model.SomatixSensorEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by gideonbar on 06/10/2017.
 */

public class SensorService extends Service {

    private static final String TAG = SensorService.class.getSimpleName();

    private RXSensorManager rxSensorManager;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable focusedSensorFlowable;
    private List<SomatixSensor> sensorList = new ArrayList<SomatixSensor>();



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Log.d(TAG, "started");
        rxSensorManager = RXSensorManager.getInstance(this);

        observe(Sensor.TYPE_ACCELEROMETER);

        return super.onStartCommand(intent, flags, startId);
    }

    private void observe(int sensorType) {

        List<Sensor> sensors = rxSensorManager.getSensorList();

        for (Sensor s: sensors){

            Log.d(TAG, s.getName());
            sensorList.add(new SomatixSensor(s, R.drawable.accelerometer));
        }

        if(focusedSensorFlowable != null){
            focusedSensorFlowable.dispose();
            compositeDisposable.remove(focusedSensorFlowable);
        }

        focusedSensorFlowable = rxSensorManager.listen(sensorType)
                .sample(1200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(sensorEvent -> {
                    return new SomatixSensorEvent(sensorEvent);
                })
                .skip(2)
                .subscribe(
                    (somatixSensorEvent) -> {

                        DirFileUtil.writeToFile("/sdcard/", "somatix.csv"
                                , new String[]{somatixSensorEvent.toCSV()});

                        Log.d(TAG, somatixSensorEvent.toCSV());
                    },
                    (error) -> { Log.e(TAG, error.getMessage());},
                    () -> {}
                );

        compositeDisposable.add(focusedSensorFlowable);
    }

}
