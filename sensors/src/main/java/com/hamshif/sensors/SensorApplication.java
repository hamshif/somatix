package com.hamshif.sensors;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.hamshif.sensors.services.SensorService;

/**
 * Created by gideonbar on 06/10/2017.
 */

public class SensorApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, SensorService.class));
    }
}
