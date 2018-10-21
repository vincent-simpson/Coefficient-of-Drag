/*TODO

 */

package com.company.vince.physicswhiledriving;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{

    private static final int ONE_SECOND = 1000;
    public static double speedValue;
    static ProgressDialog locate;
    static int p = 0;
    public boolean hasClockStarted;
    public ArrayList<Double> timeValues = new ArrayList<>();

    TextView time, speed, notifyButtonPressed;
    Button start, pause, stop;
    Button startTimer;

    long startTime, endTime, currentTime, timeElapsed;
    LocationService myService;
    boolean status;
    LocationManager locationManager;
    ImageView image;
    Thread t;
    boolean threadInterrupted = false;

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
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateUI();
                            }
                        });
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //The method below checks if Location is enabled on device or not. If not, then an alert dialog box appears with option
                //to enable gps.
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                checkGps();
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    return;
                }


                if (!status)
                //Here, the Location Service gets bound and the GPS Speedometer gets Active.
                {
                    bindService();
                }
                locate = new ProgressDialog(MainActivity.this);
                locate.setIndeterminate(true);
                locate.setCancelable(false);
                locate.setMessage("Getting Location...");
                locate.show();

                start.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                pause.setText(R.string.Pause);
                stop.setVisibility(View.VISIBLE);


            }
        });

        pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (pause.getText().toString().equalsIgnoreCase("pause"))
                {
                    threadInterrupted = true;
                    pause.setText(R.string.Resume);
                    p = 1;

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
                    p = 0;
                }
            }
        });


        stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
                p = 0;
            }
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
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * @throws IOException
     */
    public void updateUI()
    {

        if (p == 0)
        {

            endTime = System.currentTimeMillis();
            long diff = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);

            speed.setSingleLine(false);
            time.setSingleLine(false);

            String totalTime = "Total time: " + diff + " minutes";
            String speedValueFormatted;
            time.setText(totalTime);

            if (speedValue >= 0.0 && (speedValue < 90.0))
            {
                speedValueFormatted = getString(R.string.accelerate_instruction) + speedValue + getString(R.string.default_mph);
                speed.setText(speedValueFormatted);

                if (speedValue > 30 && !hasClockStarted)
                {
                    startTimer.setVisibility(View.VISIBLE);
                    Log.i("Start timer button", "Start timer button has been displayed");
                } else
                {
                    startTimer.setVisibility(View.INVISIBLE);
                }
            }

            startTimer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    notifyButtonPressed.setVisibility(View.VISIBLE);
                    hasClockStarted = true;
                    startTime = System.currentTimeMillis();
                    Log.i("Start time seconds: ", startTime + " start time seconds");
                }
            });

            currentTime = System.currentTimeMillis();
            timeElapsed = TimeUnit.MILLISECONDS.toSeconds(currentTime - startTime);

            Log.i("Current time seconds: ", currentTime + " current time seconds");
            Log.i("Subtraction ", "Difference: " + timeElapsed);

            if (timeElapsed % 10 == 0 && timeElapsed != 0 && hasClockStarted)
            {
                timeValues.add(speedValue);
                Log.i("Adding speed to array", "Current speed of " + speed + " mph was added to the array");
            }

            String path = "/sdcard/Android/data/timevalues.xlsx";
            Log.i("The path is: ", path);
            String path2 = Environment.getExternalStorageDirectory().getPath();
            Log.i("Path 2 is: ", path2);
            File file = new File(path);
            Sheet sheet = null;
            Workbook workbook = null;
            Row row;
            Cell cell;

            if (!timeValues.isEmpty())
            {
                try
                {
                    workbook = WorkbookFactory.create(file);
                    sheet = workbook.getSheetAt(0);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

                int count = 0;
                String speedNotice;
                Log.i("timeValues.isEmpty()", "Entered if statement");
                if (count < timeValues.size())
                {
                    for (int i = 0; i < timeValues.size(); i++)
                    {
                        speedNotice = timeValues.get(i) + " mph after 10 seconds" + "\n";
                        time.setText(speedNotice);

                        row = sheet.getRow(i + 6);
                        cell = row.getCell(1);

                        cell.setCellValue(timeValues.get(i));

//                        row.createCell(0).setCellValue("Value number: " + (i + 1));
//                        row.createCell(1).setCellValue(timeValues.get(i));
                        Log.i("Creating cells", "Cells created with value: " + timeValues.get(i));
                        count++;
                    }
                }
                try
                {
                    FileOutputStream fileOut = new FileOutputStream(file, true);
                    workbook.write(fileOut);
                    fileOut.close();
                    workbook.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }


}