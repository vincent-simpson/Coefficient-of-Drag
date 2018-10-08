package com.example.vince.physicswhiledriving;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener

{
    private FusedLocationProviderClient mFusedLocationClient;
    public LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    public Location mCurrentLocation, lStart, lEnd;
    private static final long INTERVAL = 0;

    public double speed;
    boolean wasSpeedAbove55 = false;
    long startTime, endTime, currentTime;
    public ArrayList<Double> timeValues = new ArrayList<>();
    boolean hasClockStarted = false;
    public Button startTimer;
    private static final int ONE_SECOND = 1000;

    private final IBinder mBinder = new LocalBinder();

    private LocationCallback mLocationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle)
    {
        requestLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates()
    {
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }



    protected void stopLocationUpdates()
    {
//        LocationServices.FusedLocationApi.removeLocationUpdates(
//                mGoogleApiClient, this);

        if(mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mFusedLocationClient = null;
            mLocationRequest = null;
        }
    }


    @Override
    public void onConnectionSuspended(int i)
    {
    }


    @Override
    public void onLocationChanged(Location location)
    {
        MainActivity.locate.dismiss();
        if(location == null)
        {
            mCurrentLocation = location;
        } else if(isBetterLocation(location, mCurrentLocation))
        {
            mCurrentLocation = location;
            speed =  Math.rint(mCurrentLocation.getSpeed() * 2.2369);
            Log.i("Current speed", "Your current speed is " + speed);
        }
        else {
            speed = Math.rint(location.getSpeed() * 2.2369);
            Log.i("Current speed", "Your current speed is " + speed);
        }

        //Calling the method below updates the  live values of speed to the TextViews.
        try
        {
            updateUI();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
    }

    class LocalBinder extends Binder
    {
        LocationService getService() {
            return LocationService.this;
        }
    }

    //The live feed of Speed is being set in the method below .
    public void updateUI() throws IOException
    {

        if (MainActivity.p == 0)
        {

            MainActivity.endTime = System.currentTimeMillis();
            long diff = TimeUnit.MILLISECONDS.toMinutes(MainActivity.endTime - MainActivity.startTime);

            MainActivity.speed.setSingleLine(false);
            MainActivity.time.setSingleLine(false);

           MainActivity.time.setText("Total Time: " + diff + " minutes");
            if (speed >= 0.0 && speed < 90.0)
            {
                MainActivity.speed.setText(getString(R.string.accelerate_instruction) + new DecimalFormat("#.##").format(speed) + getString(R.string.default_mph));
                if(speed > 50 && !hasClockStarted)
                {
                    MainActivity.startTimer.setVisibility(View.VISIBLE);
                    Log.i("Start timer button", "Start timer button has been displayed");
                } else{
                    MainActivity.startTimer.setVisibility(View.INVISIBLE);
                }
            }


//            else if(speed >= 20 )
//            {
//                MainActivity.speed.setText(getString(R.string.above_55) + new DecimalFormat("#.##").format(speed) + " mph");
//                MainActivity.speed.setTextColor(Color.RED);
//                MainActivity.startTimer.setVisibility(View.VISIBLE);
//                wasSpeedAbove55 = true;
//            }

            MainActivity.startTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.notifyButtonPressed.setVisibility(View.VISIBLE);
                    hasClockStarted = true;
                    startTime = System.currentTimeMillis() / 1000;
                    Log.i("Start time seconds: ", startTime + " start time seconds");
                }
            });

            currentTime = System.currentTimeMillis() / 1000;
            long timeElapsed = currentTime - startTime;

            Log.i("Current time seconds: ", currentTime + " current time seconds");
            Log.i("Subtraction ", "Difference: " + timeElapsed);

            if(timeElapsed % 10 == 0 && timeElapsed != 0)
            {
                timeValues.add(speed);
                Log.i("Adding speed to array", "Current speed of " + speed + " mph was added to the array");
            }


            String path = "/sdcard/Android/data/timevalues.xlsx";
            Log.i("The path is: ", path);
            File file = new File(path);
            Sheet sheet = null;
            Workbook workbook = null;

            try
            {
                workbook = WorkbookFactory.create(file);
                if(!workbook.getSheetName(1).equals("sheetone"))
                {
                    sheet = workbook.createSheet("sheetone");
                } else {
                    sheet = workbook.getSheetAt(1);
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }

            if(!timeValues.isEmpty())
            {
                int count=0;
                Log.i("timeValues.isEmpty()", "Entered if statement");
                if(count < timeValues.size()) {
                    for(int i=0; i < timeValues.size(); i++)
                    {
                        MainActivity.time.setText(timeValues.get(i) + " mph after 10 seconds" + "\n");
                        Row row = sheet.createRow(i);
                        row.createCell(0).setCellValue("Value number: " + (i+1));
                        row.createCell(1).setCellValue(timeValues.get(i));
                        Log.i("Creating cells", "Cells created with value: " + timeValues.get(i));
                        count++;
                    }
                }
                 FileOutputStream fileOut = new FileOutputStream(file, true);
                 workbook.write(fileOut);
                 fileOut.close();
                 workbook.close();
            }
            lStart = lEnd;
        }
    }


    @Override
    public boolean onUnbind(Intent intent)
    {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        return super.onUnbind(intent);
    }


    protected boolean isBetterLocation(Location location, Location currentBestLocation)
    {
        if(currentBestLocation == null)
        {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_SECOND;
        boolean isSignificantlyOlder = timeDelta < -ONE_SECOND;
        boolean isNewer = timeDelta > 0;

        if(isSignificantlyNewer)
        {
            return true;
        } else if(isSignificantlyOlder)
        {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        return isMoreAccurate || (isNewer && !isLessAccurate) || (isNewer && !isSignificantlyLessAccurate && isFromSameProvider);
    }

    private boolean isSameProvider(String provider1, String provider2)
    {
        if(provider1 == null)
        {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}