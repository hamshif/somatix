package com.hamshif.sensors.model;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.hamshif.common.util.HamshifUtil;
import com.hamshif.sensors.R;

/**
 * Created by gideonbar on 05/10/2017.
 */

public class SomatixSensor {

    private Sensor sensor;
    private int iconId;

    public SomatixSensor(Sensor sensor, int iconId) {
        this.sensor = sensor;
        this.iconId = R.drawable.accelerometer;
    }

    public int getIconId() {
        return iconId;
    }

    public static String sensorEventToString(SensorEvent sensorEvent) {

        String s =
                "Sensor Event:\n" +
                        sensorEvent.sensor.getName() + "\n" +
                        HamshifUtil.getDate(sensorEvent.timestamp)
                ;

        for (float v: sensorEvent.values){
            s += "\n" + v;
        }

        return s;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
