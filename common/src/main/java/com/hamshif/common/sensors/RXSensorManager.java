package com.hamshif.common.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import io.reactivex.Flowable;

/**
 * Created by gideonbar on 06/10/2017.
 *
 * This class lazyly initializes Flowables wrapping sensor listeners and manages subscriptions
 * TODO If there are no subscribers Flowables are recycled
 */

public final class RXSensorManager {

    private static final String TAG = RXSensorManager.class.getSimpleName();
    private static RXSensorManager instance;

    private final Context context;
    private SensorManager sensorManager;
    private final List<Sensor> sensors;
    private final HashMap<Integer, Flowable<SensorEvent>> sensorFlowables = new HashMap<Integer, Flowable<SensorEvent>>();

    public static RXSensorManager getInstance(Context context){

        if(instance == null) {
            instance = new RXSensorManager(context);
        }

        return instance;
    }

    private RXSensorManager(Context context) {
        this.context = context.getApplicationContext();
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensors = getSensorList();

        for (Sensor s : sensors) {

//            Log.d(TAG, "Sensor name: " + s.getName() + "   type: " + s.getType());
           sensorFlowables.put(s.getType(), null);
        }
    }


    public List<Sensor> getSensorList() {
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    public Set<Integer> getSensorKeys() {
        return sensorFlowables.keySet();
    }

    public Flowable<SensorEvent> listen(int sensorType) throws NoSuchElementException{

        if(!sensorFlowables.containsKey(sensorType)) {
            throw new NoSuchElementException("couldn't find this sensor");
        }

        Flowable<SensorEvent> flowable = sensorFlowables.get(sensorType);

        if(flowable == null){
            flowable = FlowableSensor.create(context, sensorType);
            sensorFlowables.put(sensorType, flowable);
        }

        return flowable;
    }

    public Flowable<SensorEvent> listen() throws NoSuchElementException {
        return listen(Sensor.TYPE_ACCELEROMETER);
    }
}
