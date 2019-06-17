package com.example.assignment3;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import com.example.assignment3.MyServiceTask.ResultCallback;

public class MyService extends Service {

    private PowerManager.WakeLock wakeLock;
    private MyServiceTask myTask;
    private Thread myThread;
    int ONGOING_NOTIFICATION_ID = 1;
    NotificationManager notificationManager;

    public class MyBinder extends Binder {
        MyService getService() {
            // Returns the underlying service.
            return MyService.this;
        }
    }

    private final IBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public MyService() {

    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myTask = new MyServiceTask(getApplicationContext());
        myThread = new Thread(myTask);
        myThread.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        wakeLock= powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myapp:mywakelocktag");
        wakeLock.acquire();

        if (!myThread.isAlive()) {
            myThread.start();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;


    }

    @Override
    public void onDestroy() {
        wakeLock.release();
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);

        // Stops the motion detector.
        myTask.stopProcessing();
    }



    public void didItMove(ResultCallback resultCallback) {
        //Log.i(LOG_TAG, "Starting didItMove() thread");
        myTask.didItMove(resultCallback);
    }


    public void resetAccelerometer() {
        // We don't actually do anything, we need to ask the service thread to do it
        // for us.
        myTask.resetAccelerometer();
    }
}
