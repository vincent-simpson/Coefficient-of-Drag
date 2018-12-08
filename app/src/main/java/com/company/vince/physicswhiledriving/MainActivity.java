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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.company.vince.physicswhiledriving.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    private final int INTERVAL = 5;
    private static final int ONE_SECOND = 1000;
    public static double speedValue;
    static ProgressDialog locate;
    static int p = 0;
    public boolean hasClockStarted;
    public ArrayList<Double> trial1 = new ArrayList<>();
    public ArrayList<Double> trial2 = new ArrayList<>();
    public ArrayList<Double> trial3 = new ArrayList<>();
    public ArrayList<Double> trial4 = new ArrayList<>();
    public ArrayList<Double> trial5 = new ArrayList<>();
    public ArrayList<Double> trial6 = new ArrayList<>();
    int trialNum = 1;
    TextView time, speed, notifyButtonPressed;
    Button start, pause, stop;
    Button startTimer, nextTrial;
    long startTime, endTime, currentTime, timeElapsed;
    LocationService myService;
    LocationManager locationManager;
    ImageView image;
    Thread t;
    boolean status;
    boolean threadInterrupted = false;
    boolean flag = false;
    double[][] a = new double[8][6];
    private ArrayList<ArrayList<Double>> trials = new ArrayList<>();
    CalculationOfVDCCRR calculation;
    static CalculationOfVDCCRR calculation2;

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status = false;
        }
    };

    void bindService() {
        if (status)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }

    void unbindService() {
        if (!status)
            return;
        unbindService(sc);
        status = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (status) {
            unbindService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!status)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        checkLocationPermissions();
        final Handler handler = new Handler();
        final Runnable task = new Runnable() {
           @Override
           public void run() {
               updateUI();
               if(!threadInterrupted) {
                   handler.postDelayed(this, 1000);
               }
           }
       };

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
        startTimer = findViewById(R.id.beginButton);
        startTimer.setVisibility(View.GONE);

        nextTrial.setOnClickListener((v) -> {
            {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage("Proceed to next trial?").setCancelable(true)
                        .setPositiveButton("Yes", (dialogInterface, i) ->
                        {
                            {
                                //Log.i("Testing", trial1.get(0) + " trial 1 at 0");
                               // Log.i("Testing", trial1.get(1) + " trial 1 at 1");
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
            handler.postDelayed(task, 200);
            threadInterrupted = false;
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

            notifyButtonPressed.setVisibility(View.VISIBLE);
            startTimer.setVisibility(View.VISIBLE);
            start.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            pause.setText(R.string.Pause);
            stop.setVisibility(View.VISIBLE);
        });

        pause.setOnClickListener((v) ->
        {
            {
                if (pause.getText().toString().equalsIgnoreCase("pause")) {
                    threadInterrupted = true;
                    pause.setText(R.string.Resume);

                } else if (pause.getText().toString().equalsIgnoreCase("Resume")) {
                    checkGps();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        return;
                    }
                    threadInterrupted = false;
                    pause.setText(R.string.Pause);
                }
            }
        });

        stop.setOnClickListener((v) ->
        {
            if (status) {
                threadInterrupted = true;
                onStop();
                Log.i("Stop button pressed", "unbound");
            }
            start.setVisibility(View.VISIBLE);
            pause.setText(R.string.Pause);
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            threadInterrupted = true;
            hasClockStarted = false;
        });
    }

    void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        }
    }

    void checkGps() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser() {
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

    public void updateUI() {

        if(trials.size() < 8) nextTrial.setVisibility(View.VISIBLE);
        else nextTrial.setVisibility(View.INVISIBLE);

        speed.setSingleLine(false);
        time.setSingleLine(false);

        endTime = System.currentTimeMillis();
        long diff = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);

//        if (diff > 1000000)
//            diff = 0;

        String totalTime = "Total time: " + diff + " minutes";

        time.setText(totalTime);

        if (speedValue >= 0) {
            String speedText = String.format(Locale.ENGLISH, "Current speed: %.2f mph", speedValue);
            speed.setText(speedText);

            if (speedValue >= 0 && !hasClockStarted) {
                startTimer.setVisibility(View.VISIBLE);
                Log.i("Start timer button", "Start timer button has been displayed");
            } else {
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

        if (((timeElapsed % INTERVAL) == 0) && (timeElapsed != 0) && hasClockStarted) {
            switch (trialNum) {
                case 1:
                    trial1.add(speedValue);
                    Log.i("Adding speed to array", "Current speed of " + speedValue + " mph was added to the array: trial1");
                    break;
                case 2:
                    trial2.add(speedValue);
                    Log.i("Adding speed to array", "Current speed of " + speedValue + " mph was added to the array: trial2");
                    break;
                case 3:
                    trial3.add(speedValue);
                    Log.i("Adding speed to array", "Current speed of " + speedValue + " mph was added to the array: trial3");
                    break;
                case 4:
                    trial4.add(speedValue);
                    Log.i("Adding speed to array", "Current speed of " + speedValue + " mph was added to the array: trial4");
                    break;
                case 5:
                    trial5.add(speedValue);
                    Log.i("Adding speed to array", "Current speed of " + speedValue + " mph was added to the array: trial5");
                    break;
                case 6:
                    trial6.add(speedValue);
                    Log.i("Adding speed to array", "Current speed of " + speedValue + " mph was added to the array: trial6");
                    break;
            }
        }

        if(trialNum == 6) {
            trials.add(trial1);
            trials.add(trial2);
            trials.add(trial3);
            trials.add(trial4);
            trials.add(trial5);
            trials.add(trial6);
        }

        if (trials.size() == 6) {
            for (int i = 0; i < 6; i++) {
                if (!trials.get(i).isEmpty()) {
                    System.out.println("i : " + i);

                    moveArrayListToArray(i);
                    Log.i("Moving to a", "Moving arraylist to a[][]");
                }
            }
            if (flag) {
                runCalculations();
                calculateCoefficientOfDrag();
            }
        }
        Log.i("Speed value: ", speedValue + "");
    }

    private void moveArrayListToArray(int column) {
        flag = true;
        int rowtemp=0;
        try {
            for (int row = 0; row < trials.size(); row++) {
                for(int j = 0; j < trials.get(row).size(); j++) {
                    a[row][column] = trials.get(row).get(j);
                }
                rowtemp = row;
            }
        } catch(IndexOutOfBoundsException e) {
            System.out.println("row is: " + rowtemp);
            e.printStackTrace();
        }
    }

    public void runCalculations() {
        Log.i("Running calculations", "runCalculations() called");
        flag = false;
        calculation = new CalculationOfVDCCRR(a);

        calculation.calculateAverageVelocity();
        calculation.calculateActualVelocity();

        for (int i = 0; i < 15; i++) {
            calculation.calculateForce(i);
            calculation.calculateAcceleration(i);
            calculation.calculateModelVelocity(i);
        }

        int index = 0;
        for (int i = 0; i < trials.get(i).size(); i++) {
            calculation.calculateErrorSquared(i, index);
            index += 2;
        }
        calculation.calculateSumOfError();
        CalculationOfVDCCRR.printArrayListContents(CalculationOfVDCCRR.errorSquared);
        System.out.println("Sum of error: " + calculation.getSumOfError().toPlainString());
    }

    private void calculateCoefficientOfDrag() {
//        ProgressBar progressBar = findViewById(R.id.indeterminateBar);
//        progressBar.setIndeterminate(true);
//        progressBar.setVisibility(View.VISIBLE);

        System.out.println("Calculate coefficient of drag called.");
        BigDecimal DragCoefficient = new BigDecimal(CalculationOfVDCCRR.DRAG_COEFFICIENT);
        calculation.clearAllValues();

        while(calculation.getSumOfError().doubleValue() > 0.9) {
            System.out.println(calculation.getSumOfError().doubleValue());
            DragCoefficient = DragCoefficient.subtract(new BigDecimal(0.001));
            calculation.setDragCoefficient(DragCoefficient);
            System.out.println("Current drag coef: " +calculation.getDragCoefficient().doubleValue());

            calculation.calculateAverageVelocity();
            calculation.calculateActualVelocity();
            for (int i = 0; i < 15; i++) {
                calculation.calculateForce(i);
                calculation.calculateAcceleration(i);
                calculation.calculateModelVelocity(i);
            }
            int index = 0;
            for (int i = 0; i < trials.get(i).size(); i++) {
                calculation.calculateErrorSquared(i, index);
                index += 2;
            }
            calculation.calculateSumOfError();
            calculation.printAllSizes();
            calculation.clearAllValues();
            notifyButtonPressed.setText("Drag Co: " + calculation.getDragCoefficient());
        }
    }

}
