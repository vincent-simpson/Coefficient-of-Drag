package com.example.vince.physicswhiledriving;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by vipul on 12/13/2015.
 */
public class LocationService extends Service implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener

{

    private static final long INTERVAL = 0;
    public LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    public Location mCurrentLocation, lStart, lEnd;
    public double speed;
    boolean wasSpeedAbove55 = false;
    long startTime=0, endTime;
    private ArrayList<Long> timeValues = new ArrayList<>();




    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
        }
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {
        MainActivity.locate.dismiss();
        if(location == null) {
            mCurrentLocation = location;
        } else if(isBetterLocation(location, mCurrentLocation)) {
            mCurrentLocation = location;
            speed =  Math.rint(mCurrentLocation.getSpeed() * 2.2369);
        } else {
            speed = Math.rint(location.getSpeed() * 2.2369);
        }

        //Calling the method below updates the  live values of speed to the TextViews.
        updateUI();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }


    }

    //The live feed of Speed is being set in the method below .
    private void updateUI() {


        if (MainActivity.p == 0) {
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);

           MainActivity.time.setText("Total Time: " + diff + " minutes");
            if (speed >= 0.0 && speed < 10.0)
            {
                MainActivity.speed.setText(getString(R.string.accelerate_instruction) + new DecimalFormat("#.##").format(speed) + getString(R.string.default_mph));
            }
            else if(speed > 55.0)
            {
                MainActivity.speed.setText(getString(R.string.above_55) + new DecimalFormat("#.##").format(speed) + " mph");
                MainActivity.speed.setTextColor(Color.RED);
                wasSpeedAbove55 = true;
            }

            if(speed == 50 && wasSpeedAbove55) {
                startTime = System.currentTimeMillis();
            }
            if(speed == 40 && wasSpeedAbove55) {
                endTime = System.currentTimeMillis();
                timeValues.add(endTime - startTime);
                startTime = System.currentTimeMillis();
            }
            if(speed == 30 && wasSpeedAbove55) {
                endTime = System.currentTimeMillis();
                timeValues.add(endTime - startTime);
                startTime = System.currentTimeMillis();
            }
            if(speed == 20 && wasSpeedAbove55) {
                endTime = System.currentTimeMillis();
                timeValues.add(endTime - startTime);
            }

            if(!timeValues.isEmpty()) {
                MainActivity.time.setText("1st Interval: " + timeValues.get(0)+ "\n" + "2nd Interval: " + timeValues.get(1) + "\n" +
                                            "3rd Interval: " + timeValues.get(2));
            }


            lStart = lEnd;

        }

    }


    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        return super.onUnbind(intent);
    }

    private static final int TEN_SECONDS = 10000;

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if(currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TEN_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -TEN_SECONDS;
        boolean isNewer = timeDelta > 0;

        if(isSignificantlyNewer) {
            return true;
        } else if(isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        if(isMoreAccurate){
            return true;
        } else if(isNewer && !isLessAccurate) {
            return true;
        } else if(isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if(provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}