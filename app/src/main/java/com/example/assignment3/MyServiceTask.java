package com.example.assignment3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import java.util.Date;

import static android.content.Context.SENSOR_SERVICE;

public class MyServiceTask implements SensorEventListener, Runnable {

    public static final String LOG_TAG = "MyServiceTask";
    SensorManager sensorManager;
    Sensor sensor;
    Date first_accel_time = null;
    Date start_time = null;
    private Context context;
    final Object myLock = new Object();
    boolean running;
    //private float[] gravity = new float[2];

    ResultCallback resultCallback;

    public MyServiceTask(Context context){
        this.context = context;

        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        start_time = new Date();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float cx, cy;
        cx = event.values[0];
        //cy = event.values[1];
        final float alpha = (float) 0.8;
        //final float actual_gravity = (float) 9.81;

        // Isolate the force of gravity with the low-pass filter.
       // gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        //gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];

        // get the change of the x and y values of the accelerometer
        //cx = (event.values[0] - gravity[0]);
        //deltaY = (event.values[1] - gravity[1]);
        cy = (event.values[1] - (float) 9.81);

        // if the change is below 2, it is just plain noise
        if (cx < 1)
            cx = 0;
        if (cy < 1)
            cy = 0;

        // Only check for deltaX and deltaY if phone has not been moved
        if (first_accel_time == null) {
            if ((cx > 0) || (cy > 0)) {
                first_accel_time = new Date();
                synchronized (myLock) {
                    // Taken from https://stackoverflow.com/questions/1970239/in-java-how-do-i-get-
                    // the-difference-in-seconds-between-2-dates
                    int secondsBetween = (int) ((first_accel_time.getTime() - start_time.getTime()) / 1000);
                    if (first_accel_time != null && secondsBetween > 30) {
                        // Not sure if stopProcessing(); is needed to stop run()
                        notifyResultCallback(cx, cy);
                    } else {
                        // null the accel time because it has not been 30 sec since thread has started
                        first_accel_time = null;
                    }
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void run() {
        running = true;
        while (running) {
            // Sleep a tiny bit.
            try {
                //Thread.sleep(10000);  // No need for delay since using onSensorChanged()
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
        }

    }

    public void stopProcessing() {
        // No need to bother with a synchronized statement; booleans are atomically updated.
        running = false;
    }

    public void resetAccelerometer() {
        first_accel_time = null;
        start_time = new Date();
    }

    private void notifyResultCallback(float x, float y) {
        ServiceResult result = new ServiceResult();

        // If we got a null result, we have no more space in the buffer,
        // and we simply drop the integer, rather than sending it back.
        if (result != null) {
            result.moved = true;
            result.x_accel = x;
            result.y_accel = y;
            Log.i(LOG_TAG, "calling resultCallback for " + result.x_accel + " and "
                    + result.y_accel);
            resultCallback.onResultReady(result);
        }
    }

    public void didItMove(ResultCallback result) {
        Log.i(LOG_TAG, "Adding result callback");
        resultCallback = result;
    }

    // This is the interface back to MainActivity, as onResultReady() is in MainActivity
    public interface ResultCallback {
        void onResultReady(ServiceResult result);
    }
}
