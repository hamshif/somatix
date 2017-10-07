package com.hamshif.common.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Cancellable;

public final class FlowableSensor {

    /**
     * copied and modified from: https://github.com/Joseph82/RxSensors
     * Creates a Flowable that subscribes to the source of data (sensor) and emits {@link SensorEvent}
     * items with a predefined frequency.
     *
     * @param sensorManager      A {@link SensorManager} object.
     * @param sensor             The {@link Sensor Sensor} to register to.
     * @param samplingPeriodUs   See {@link SensorManager#registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs)}
     * @param maxReportLatencyUs See {@link SensorManager#registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs)}
     * @return A Flowable that generates {@link SensorEvent} based on the provided parameters.
     */
    public static Flowable<SensorEvent> create(final SensorManager sensorManager, final Sensor sensor,
                                                 final int samplingPeriodUs, final int maxReportLatencyUs) {
        return Flowable.create(new FlowableOnSubscribe<SensorEvent>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<SensorEvent> e) throws Exception {
                final Listener listener = new Listener(e);
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        sensorManager.unregisterListener(listener);
                    }
                });

                if (Build.VERSION.SDK_INT < 19) {
                    sensorManager.registerListener(listener, sensor, samplingPeriodUs);
                } else {
                    sensorManager.registerListener(listener, sensor, samplingPeriodUs, maxReportLatencyUs);
                }
            }
        }, BackpressureStrategy.MISSING);
    }


    /**
     * See {@link #create(SensorManager, Sensor, int, int)}. It used a predefined value for
     * maxReportLatencyUs equal to zero. The events will then be delivered as soon as they will be
     * available.
     */
    public static Flowable<SensorEvent> create(SensorManager sensorManager, Sensor sensor,
                                                 int samplingPeriodUs) {
        return create(sensorManager, sensor, samplingPeriodUs, 0);
    }


    /**
     * Create a Flowable that emits {@link SensorEvent} items of the default sensor for the
     * provided sensorType and with the provided sampling rate.
     *
     * @param context          A Context object.
     * @param sensorType       A Context object.
     * @param samplingPeriodUs Sampling rate of the emitter. It is an indication for the system
     *                         about how fast the events should be emitted.
     * @return A Flowable object that emits {@link SensorEvent}.
     */
    public static Flowable<SensorEvent> create(@NonNull Context context, int sensorType, int samplingPeriodUs) {
        SensorManager sensorManager = getSensorManager(context);
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if(sensor == null) throw new NullPointerException("couldn't get sensor");

        return FlowableSensor.create(sensorManager, sensor, samplingPeriodUs);
    }

    /**
     * Create a Flowable that emits {@link SensorEvent} items with a default sampling period
     * {@link SensorManager#SENSOR_DELAY_NORMAL} of the default sensor for the
     * provided sensorType.
     *
     * @param context    A Context object.
     * @param sensorType A Context object.
     * @return A Flowable object that emits {@link SensorEvent}.
     */
    public static Flowable<SensorEvent> create(Context context, int sensorType) {
        return create(context, sensorType, SensorManager.SENSOR_DELAY_NORMAL);
    }


    /**
     * Create a Flowable that emits {@link SensorEvent} items for the specified sensor and with
     * the provided sampling rate.
     *
     * @param context          A Context object.
     * @param sensor           The Sensor that we want to observe.
     * @param samplingPeriodUs Sampling rate of the emitter. It is an indication for the system
     *                         about how fast the events should be emitted.
     * @return A Flowable object that emits {@link SensorEvent}.
     */
    public static Flowable<SensorEvent> create(@NonNull Context context, @NonNull Sensor sensor, int samplingPeriodUs) {
        SensorManager sensorManager = getSensorManager(context);
        return FlowableSensor.create(sensorManager, sensor, samplingPeriodUs);
    }


    static final class Listener implements SensorEventListener {
        private final FlowableEmitter<SensorEvent> emitter;

        Listener(FlowableEmitter<SensorEvent> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            emitter.onNext(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private static SensorManager getSensorManager(Context context) {
        return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }
}