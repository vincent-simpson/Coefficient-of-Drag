package com.company.vince.physicswhiledriving;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements
        com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener

{
    private static final long INTERVAL = 0;
    private static final int ONE_SECOND = 1000;
    private final IBinder mBinder = new LocalBinder();
    public LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    public Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
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
        createGoogleApiConnection();
        return mBinder;
    }

    protected void createGoogleApiConnection()
    {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
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
        MainActivity.locate.dismiss();
        requestLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates()
    {
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }


    protected void stopLocationUpdates()
    {
        if (mFusedLocationClient != null)
        {
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
        if (location == null)
        {
            mCurrentLocation = location;
        } else if (isBetterLocation(location, mCurrentLocation))
        {
            mCurrentLocation = location;
            MainActivity.speedValue = mCurrentLocation.getSpeed() * 2.2369;
            //Log.i("Current speed", "Your current speed is " + speed);
        } else
        {
            MainActivity.speedValue = location.getSpeed() * 2.2369;
            //Log.i("Current speed", "Your current speed is " + speed);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        return super.onUnbind(intent);
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation)
    {
        if (currentBestLocation == null)
        {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_SECOND;
        boolean isSignificantlyOlder = timeDelta < -ONE_SECOND;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer)
        {
            return true;
        } else if (isSignificantlyOlder)
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
        if (provider1 == null)
        {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    class LocalBinder extends Binder
    {
        LocationService getService()
        {
            return LocationService.this;
        }
    }
}