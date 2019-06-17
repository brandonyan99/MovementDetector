package com.example.assignment3;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.assignment3.MyService.MyBinder;

public class MainActivity extends AppCompatActivity implements com.example.assignment3.MyServiceTask.ResultCallback{
    //static TextView ytv;
    //static TextView xtv;
    static TextView status;
    int DISPLAY_NUMBER = 10;
    Handler mUiHandler;
    boolean serviceBound;
    MyService myService;
    boolean moved = false;
    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ytv = findViewById(R.id.ytextView);
        //xtv = findViewById(R.id.xtextView);
        status = findViewById(R.id.statusbutton);
        mUiHandler = new Handler(getMainLooper(), new UiCallback());
        serviceBound = false;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindMyService();

    }

    private void bindMyService() {
        Log.i(LOG_TAG, "Starting the service");
        Intent intent = new Intent(this, MyService.class);
        Log.i("LOG_TAG", "Trying to bind");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            // We have bound to the camera service.
            MyBinder binder = (MyBinder) serviceBinder;
            myService = binder.getService();
            serviceBound = true;

            // Let's connect the callbacks.
            Log.i("MyService", "Bound succeeded, adding the callback");
            // updateResultCallback is for ServiceDemo2 lecture 14
            //myService.updateResultCallback(MainActivity.this);
            myService.didItMove(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    @Override
    protected void onPause() {
        if (serviceBound) {
            Log.i("MyService", "Unbinding");
            unbindService(serviceConnection);
            serviceBound = false;

            // If we like, stops the service.
            if (true) {
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");
            }
        }

        super.onPause();
    }

    public void onResultReady(ServiceResult result) {
        if (result != null) {
            Log.i(LOG_TAG, "Preparing a message for x_accel=" + result.x_accel
                    + " and y_accel=" + result.y_accel);
        } else {
            Log.e(LOG_TAG, "Received an empty result!");
        }
        mUiHandler.obtainMessage(DISPLAY_NUMBER, result).sendToTarget();
    }

    private class UiCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            if (moved == false) {
                if (message.what == DISPLAY_NUMBER) {
                    // Gets the result.
                    ServiceResult result = (ServiceResult) message.obj;
                    // Displays it.
                    if (result != null) {
                        Log.i(LOG_TAG, "Displaying: " + result.x_accel + " " + result.y_accel);
                        //xtv.setText(Float.toString(result.x_accel));
                        //ytv.setText(Float.toString(result.y_accel));
                        status.setText("The phone moved!");
                        moved = true;
                    } else {
                        Log.e(LOG_TAG, "Error: received empty message!");
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }



    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clearbutton:
                //xtv.setText("test");
                status.setText("Everything was quiet");
                moved = false;

                // Invoke Service to reset values
                // equivalent of myService.doSomething() in ServiceDemo2 lecture 14
                myService.resetAccelerometer();
                break;

            case R.id.exitbutton:
                Log.i("MyService", "Unbinding");
                unbindService(serviceConnection);
                serviceBound = false;  // prevents app from going into onPause()

                // Stops the service.
                Log.i(LOG_TAG, "Stopping.");
                Intent intent = new Intent(this, MyService.class);
                stopService(intent);
                Log.i(LOG_TAG, "Stopped.");

                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
        }
    }

}
