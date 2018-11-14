/*TODO

 */

package com.company.vince.physicswhiledriving;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.company.vince.physicswhiledriving.R.layout.activity_main;

public class MainActivity extends AppCompatActivity
{

    private static final int ONE_SECOND = 1000;
    public static double speedValue;
    static ProgressDialog locate;
    static int p = 0;
    public boolean hasClockStarted;
    public ArrayList<Double> trial1, trial2, trial3, trial4, trial5, trial6;
    private ArrayList<ArrayList<Double>> trials = new ArrayList<>();

    int trialNum =1;

    TextView time, speed, notifyButtonPressed;
    Button start, pause, stop;
    Button startTimer, nextTrial;

    long startTime, endTime, currentTime, timeElapsed;
    LocationService myService;
    boolean status;
    LocationManager locationManager;
    ImageView image;
    Thread t;
    boolean threadInterrupted = false;
    boolean flag = false;

    double[][] a = new double[8][6];

    private ServiceConnection sc = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            status = false;
        }
    };

    void bindService()
    {
        if (status)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }

    void unbindService()
    {
        if (!status)
            return;
        unbindService(sc);
        status = false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (status)
        {
            unbindService();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        if (!status)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        checkLocationPermissions();
        checkWriteToStoragePermissions();

        t = new Thread()
        {
            @Override
            public void run()
            {
                while (!threadInterrupted)
                {
                    try
                    {
                        Thread.sleep(ONE_SECOND);
                        runOnUiThread( () ->
                                updateUI()
                        );
                    } catch (Exception e) {e.printStackTrace();}
                }
            }
        };
        t.start();


        setContentView(activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nextTrial = findViewById(R.id.iterationButton);
        nextTrial.setVisibility(View.GONE);

        notifyButtonPressed = findViewById(R.id.notifyButtonPressed);
        notifyButtonPressed.setVisibility(View.GONE);
        time = findViewById(R.id.timetext);
        speed = findViewById(R.id.speedtext);

        start = findViewById(R.id.start);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);

        image = findViewById(R.id.image);
        startTimer = findViewById(R.id.startTimerButton);
        startTimer.setVisibility(View.GONE);

        nextTrial.setOnClickListener((v) -> {
             {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage("Proceed to next trial?").setCancelable(true)
                        .setPositiveButton("Yes", (dialogInterface, i) ->
                        {
                            {
                                Log.i("Testing", trial1.get(0) + " trial 1 at 0");
                                Log.i("Testing", trial1.get(1) + " trial 1 at 1");
                                trialNum++;
                                startTimer.performClick();
                            }
                        });
                alertBuilder.setNegativeButton("No", (dialogInterface, i) ->
                        dialogInterface.cancel());

                AlertDialog alert = alertBuilder.create();
                alert.show();
            }

        });

        start.setOnClickListener((v) ->
        {
                if (!t.isAlive())
                { t.run(); }

                threadInterrupted = false;
                Log.i("Is thread active", t.isAlive() + "");
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                checkGps();
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    return;

                if (!status)
                    bindService();

                locate = new ProgressDialog(MainActivity.this);
                locate.setIndeterminate(true);
                locate.setCancelable(false);
                locate.setMessage("Getting Location...");
                locate.show();

                nextTrial.setVisibility(View.VISIBLE);
                notifyButtonPressed.setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                pause.setText(R.string.Pause);
                stop.setVisibility(View.VISIBLE);
        });

        pause.setOnClickListener((v) ->
        {
            {
                if (pause.getText().toString().equalsIgnoreCase("pause"))
                {
                    threadInterrupted = true;
                    pause.setText(R.string.Resume);

                } else if (pause.getText().toString().equalsIgnoreCase("Resume"))
                {
                    checkGps();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    {
                        //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    threadInterrupted = false;
                    pause.setText(R.string.Pause);
                }
            }
        });

        stop.setOnClickListener((v) ->
        {
                if (status)
                {
                    threadInterrupted = true;
                    onStop();
                    Log.i("Stop button pressed", "unbound");
                }
                start.setVisibility(View.VISIBLE);
                pause.setText(R.string.Pause);
                pause.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
                threadInterrupted = true;
            });
    }

    void checkLocationPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        }
    }

    void checkWriteToStoragePermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }
    }

    void checkGps()
    {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        (dialogInterface, i) ->
                        {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        });

        alertDialogBuilder.setNegativeButton("Cancel",
                (dialogInterface, i) ->
                        dialogInterface.cancel());

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void updateUI()
    {
        speed.setSingleLine(false);
        time.setSingleLine(false);

        endTime = System.currentTimeMillis();
        long diff = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);


        if (diff > 1000000)
            diff = 0;

        String totalTime = "Total time: " + diff + " minutes";

        time.setText(totalTime);

        if (speedValue >= 0)
        {
            speed.setText(String.format("Current speed: %.2f mph", speedValue));

            if (speedValue > 30 && !hasClockStarted)
            {
                startTimer.setVisibility(View.VISIBLE);
                Log.i("Start timer button", "Start timer button has been displayed");
            } else
            {
                startTimer.setVisibility(View.INVISIBLE);
            }
        }

        startTimer.setOnClickListener(view ->
        {
                notifyButtonPressed.setVisibility(View.VISIBLE);
                hasClockStarted = true;
                startTime = System.currentTimeMillis();
                Log.i("Start time seconds: ", startTime + " start time seconds");

        });

        currentTime = System.currentTimeMillis();
        timeElapsed = TimeUnit.MILLISECONDS.toSeconds(currentTime - startTime);

        Log.i("Current time seconds: ", currentTime + " current time seconds");
        Log.i("Subtraction ", "Difference: " + timeElapsed);

        if (((timeElapsed % 10) == 0) && (timeElapsed != 0) && hasClockStarted)
        {
            switch(trialNum) {
                case 1: trial1.add(speedValue);
                case 2: trial2.add(speedValue);
                case 3: trial3.add(speedValue);
                case 4: trial4.add(speedValue);
                case 5: trial5.add(speedValue);
                case 6: trial6.add(speedValue);
            }
            Log.i("Adding speed to array", "Current speed of " + speedValue + " mph was added to the array");
            trials.add(trial1); trials.add(trial2); trials.add(trial3);
            trials.add(trial4); trials.add(trial5); trials.add(trial6);
        }



        //Array that represents the velocity decreasing as a function of time

        for(int i=0; i < trials.size(); i++) {
            if(!trials.get(i).isEmpty()) {
                moveArrayListToArray(trials.get(i), i);
                Log.i("Moving to a", "Moving arraylist to a[][]");
            }
        }

        if(flag) runCalculations();

        Log.i("Speed value: ", speedValue + "");
    }

    private void moveArrayListToArray(ArrayList<Double> aList, int column)
    {
        flag = true;
        for(int row=0; row < 8; row++) {
            a[row][column] = aList.get(row);
        }
    }

    public void runCalculations() {
        flag = false;
        CalculationOfVDCCRR calculation = new CalculationOfVDCCRR(a);

        calculation.calculateAverageVelocity();
        calculation.calculateActualVelocity();

        for(int i=0; i < 15; i++) {
            calculation.calculateForce(i);
            calculation.calculateAcceleration(i);
            calculation.calculateModelVelocity(i);
        }

        int index=0;
        for(int i=0; i < 8; i++) {
            calculation.calculateErrorSquared( i, index );
            index += 2;
        }
        //CalculationOfVDCCRR.printArrayListContents(CalculationOfVDCCRR.errorSquared);
    }
}
