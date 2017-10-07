package com.hamshif.sensors.model;

import android.hardware.SensorEvent;

import com.hamshif.sensors.interfaces.CSVAble;

/**
 * Created by gideonbar on 06/10/2017.
 */

public class SomatixSensorEvent implements CSVAble {

    private static final char COMMA = ',';

    private SensorEvent sensorEvent;

    public SomatixSensorEvent(SensorEvent sensorEvent){
        this.sensorEvent = sensorEvent;
    }


    public String toCSV(){

        String s =
            sensorEvent.sensor.getName() + COMMA +
            sensorEvent.sensor.getType() + COMMA +
            sensorEvent.sensor.getVendor() + COMMA +
            sensorEvent.timestamp
        ;

        for (float v: sensorEvent.values){
            s += COMMA + v;
        }

        return s;
    }

    @Override
    public String toCSV(Object o) {
        return toCSV();
    }
}
