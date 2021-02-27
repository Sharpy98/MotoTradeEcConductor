package com.henryalmeida.mototradeecconductor.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.GeofireProvider;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

// CLASE PARA PODER TRABAJAR EN 2DO PLANO
public class ForegroundSrevice extends Service {

    public final String NOTIFICATION_CHANEL_ID = "com.henryalmeida.mototradeecconductor";
    private LatLng mCurrentLatLng;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;


    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d("FOREGROUND", "TEMPORIZADOR");
            handler.postDelayed(runnable, 1000);
        }
    };

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d("FOREGROUND","Actulizando posicion");
                    updateLocation();

                }
            }
        }
    };

    private void startLocation() {
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        // Establecer la prioridad que va a tener el gps en la actualizacion del gps
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

    }

    private void updateLocation(){
        if(mAuthProvider.existSession() && mCurrentLatLng !=null){
            mGeofireProvider.saveLocation(mAuthProvider.getId(),mCurrentLatLng);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //handler.postDelayed(runnable,1000);
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");

        startLocation();
    }

    @Override
    public void onDestroy() {

        if (mLocationCallback != null && mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = new NotificationCompat.Builder(this,NOTIFICATION_CHANEL_ID)
                                    .setSmallIcon(R.drawable.ic_casco)
                                    .setContentTitle("Viaje en curso")
                                    .setContentText("App corriendo en segundo plano")
                                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                                    .setCategory(Notification.CATEGORY_SERVICE)
                                    .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startMyForegroundService();
        }
        else {
            startForeground(50,notification);
        }

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyForegroundService(){

        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANEL_ID,channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builer = new NotificationCompat.Builder(this,NOTIFICATION_CHANEL_ID);
        Notification notification = builer.
                                    setOngoing(true)
                                    .setSmallIcon(R.drawable.ic_casco)
                                    .setContentTitle("Viaje en curso")
                                    .setContentText("App corriendo en segundo plano")
                                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                                    .setCategory(Notification.CATEGORY_SERVICE)
                                    .build();
        startForeground(50,notification);
    }
}
