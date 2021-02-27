package com.henryalmeida.mototradeecconductor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.henryalmeida.mototradeecconductor.HistoryBookingDriverActivity;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.includes.MyToolbar;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.DriversFoundProvider;
import com.henryalmeida.mototradeecconductor.providiers.GeofireProvider;
import com.henryalmeida.mototradeecconductor.providiers.TokenProvider;
import com.henryalmeida.mototradeecconductor.services.ForegroundSrevice;

public class MapDriver extends AppCompatActivity implements OnMapReadyCallback {

    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private DriversFoundProvider mDriverFoundProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragmet;

    // BANDERA PARA PERMISO DE UBICACION
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    // Para cambiar el icono en el mapa
    private Marker mMarker;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private Button btnConect;
    private boolean mIsconnect = false;

    private LatLng mCurrentLatLng;

    // Para que se ejecute hasta que el conductor este en driver working
    private ValueEventListener mLister;

    private boolean mExtraConnect;

    // para recuperar el viaje

    SharedPreferences mPref;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    //Para que no nos duplique
                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    // Colocamos una imagen para que salga en el mapa con la localizacion
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_cascov1))
                    );
                    // Obtener la localizacion en tiempo real
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    updateLocation();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);

        MyToolbar.show1(MapDriver.this, "Condutor", false);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mTokenProvider = new TokenProvider();
        mDriverFoundProvider = new DriversFoundProvider();

        // Iniciar o detener la ubicacion del usuario
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragmet = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragmet.getMapAsync(this);

        mExtraConnect =getIntent().getBooleanExtra("connect",false);

        btnConect = findViewById(R.id.btn_Conect);
        btnConect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsconnect) {
                    disconnect();
                } else {
                    startLocation();
                }
            }
        });

        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
        String status = mPref.getString("status","");
        String idClient = mPref.getString("idClient","");
        Double priceKm = Double.valueOf(mPref.getFloat("priceKm",0));
        String price = mPref.getString("price","");

        if (status.equals("start")||status.equals("ride")){
            goToMapDriverActivity(idClient,priceKm,price);
        }
        else {
            generateToken();
            deleteDriverWorking();
            delteDriverFound();   
        }
    }


    private void goToMapDriverActivity(String idClient, Double priceKm, String price) {
        Intent intent = new Intent(MapDriver.this,MapDriverBooking.class);
        intent.putExtra("idClient",idClient);
        intent.putExtra("priceKm",priceKm);
        intent.putExtra("price",price);
        startActivity(intent);
    }


    private void delteDriverFound() {
        mDriverFoundProvider.delete(mAuthProvider.getId());
    }

    private void deleteDriverWorking() {
        mGeofireProvider.deleteDriverWorking(mAuthProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isDriverWorking(); // METODO QUE NOS INDICA SI ESTA ACTIVO O NO

                if (mExtraConnect){
                    startLocation();
                }
            }
        });
    }

    private void checkIfDriverIsActived() {
        mGeofireProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    startLocation();
                    Log.d("Star","Entro");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationCallback != null && mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
        if (mLister != null) {
             if (mAuthProvider.existSession()) {
            mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mLister);
            }
        }
    }

    private void isDriverWorking() {
        mLister = mGeofireProvider.isDriverWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    disconnect();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Poner los botones de zoom en el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(false);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        // Establecer la prioridad que va a tener el gps en la actualizacion del gps
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        checkIfDriverIsActived(); // Para que se conecte despues de rechazar un pedido

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        // Si quiero utilizar el punto azul y tendriamos que poner en todos los mFused menos en disconect
                        // PAra obtener el punto exacto
                        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(false);*/
                    } else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermission();
                }
            } else {
                checkLocationPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    // Para saber si tiene activado la ubicacion
    private boolean gpsActived(){
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isActive = true;
        }
        return isActive;
    }

    private  void disconnect(){
        if(mFusedLocation!=null){
            btnConect.setText("CONECTARSE");
            mIsconnect = false;
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()){
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        }
        else{
            Toast.makeText(this,"No te puedes desconectar",Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                if(gpsActived()){
                    btnConect.setText("DESCONECTARSE");
                    mIsconnect = true;
                    mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermission();
            }
        }
        else {
            if (gpsActived()){
                mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta apicación requiere de los permisos de ubicación para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapDriver.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                            }
                        })
                .create()
                .show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriver.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.action_logout){
            logout();
        }
        if (item.getItemId()==R.id.action_update){
            Intent intent = new Intent(MapDriver.this,UpdateProfileDriverActivity.class);
            startActivity(intent);
        }
        if (item.getItemId()==R.id.action_history){
            Intent intent = new Intent(MapDriver.this, HistoryBookingDriverActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void logout(){
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapDriver.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    void generateToken(){

        mTokenProvider.create(mAuthProvider.getId());

    }
}