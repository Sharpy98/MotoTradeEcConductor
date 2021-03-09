package com.henryalmeida.mototradeecconductor.activities.Route;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.henryalmeida.mototradeecconductor.CalificationClientActivity;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.activities.MapDriver;
import com.henryalmeida.mototradeecconductor.activities.MapDriverBooking;
import com.henryalmeida.mototradeecconductor.models.FCMBody;
import com.henryalmeida.mototradeecconductor.models.FCMResponse;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientBookingProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientProvider;
import com.henryalmeida.mototradeecconductor.providiers.GeofireProvider;
import com.henryalmeida.mototradeecconductor.providiers.GoogleApiProvider;
import com.henryalmeida.mototradeecconductor.providiers.NotificationProvider;
import com.henryalmeida.mototradeecconductor.providiers.Route.BookingProviderRoute;
import com.henryalmeida.mototradeecconductor.providiers.TokenProvider;
import com.henryalmeida.mototradeecconductor.services.ForegroundSrevice;
import com.henryalmeida.mototradeecconductor.utils.DecodePoints;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDriverBookingRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClientProvider mClientProvider;
    private BookingProviderRoute mClientBookingProvider;
    private NotificationProvider mNotificationProvider;

    private String numDelivery;


    private GoogleMap mMap;
    private SupportMapFragment mMapFragmet;

    // BANDERA PARA PERMISO DE UBICACION
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    // Para cambiar el icono en el mapa
    private Marker mMarker;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private LatLng mCurrentLatLng;

    private TextView tvClientBooking;
    private TextView tvPhoneOrigin;
    private TextView tvOrigin;
    private TextView tvDestination;
    private TextView tvTime;

    private Button btnStartBooking;
    private Button btnFinishBooking;
    private Button btnCancel;

    private ImageView mImageViewBooking;

    private String mExtraClientId;

    // Para trazar la ruta del conductor hacia el cliente
    private LatLng mOrigenLatLng;
    private LatLng mDestinationLatLng;
    private LatLng mDestinationLatLng2;
    private LatLng mDestinationLatLng3;
    private LatLng mDestinationLatLng4;
    private LatLng mDestinationLatLng5;
    private LatLng mDestinationLatLng6;
    private LatLng mDestinationLatLng7;
    private LatLng mDestinationLatLng8;
    private LatLng mDestinationLatLng9;
    private LatLng mDestinationLatLng10;

    private GoogleApiProvider mGooglePrvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    // Para que nos diga que si fue la primera vez que entro al locationCallback
    private boolean mIsFirstTime = true;
    // Para que llame una sola vez a la habilitacion del boton
    private boolean mIsCloseToClient = false;

    double mDistanceInMeter = 1;
    int mMinutes = 0;
    int mSeconds = 0;
    boolean mSecondIsOver = false;
    boolean mRideStar = false;

    boolean mIsFinishBooking = false; // Para saber si la carrera ya finalizo

    Handler mHandler = new Handler();

    Location mPreviusLocation = new Location(""); // PARA SABER LA DISTANCIA QUE TIENE EL CONDUNCTOR

    // PARA RECUPERAR EL VIAJE SI SE CIERRA TOTALMENTE LA APLICACION
    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;

    // PARA CORRER UN TEMPORIZADOR
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mSeconds++;

            if (!mSecondIsOver){
                tvTime.setText(mSeconds + " Seg");
            }
            else {
                tvTime.setText(mMinutes + " Min" + mSeconds);
            }

            if (mSeconds == 59){
                mSeconds = 0;
                mSecondIsOver = true;
                mMinutes ++;
            }
            mHandler.postDelayed(runnable,1000);
        }
    };

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(),location.getLongitude());

                    //Para que no nos duplique
                    if(mMarker != null){
                        mMarker.remove();
                    }

                    if (mRideStar){
                        mDistanceInMeter = mDistanceInMeter + mPreviusLocation.distanceTo(location);
                        Log.d("DISTANCIA", "Distancia recorrida" + mDistanceInMeter );
                    }
                    mPreviusLocation = location; // PARA SABER LA DISCTANCIA


                    // Colocamos una imagen para que salga en el mapa con la localizacion
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(),location.getLongitude())
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
                    // Actualiza la posicion del condutor en tiempo real
                    updateLocation();

                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        // Meotod para obtener los datos del pedido
                        getClientBooking();
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking_route);

        numDelivery = getIntent().getStringExtra("numRoute");

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new BookingProviderRoute();
        mNotificationProvider = new NotificationProvider();


        // Iniciar o detener la ubicacion del usuario
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragmet = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragmet.getMapAsync(this);

        tvClientBooking = findViewById(R.id.tvClientBooking);
        tvPhoneOrigin = findViewById(R.id.tvPhoneOrigin);
        tvOrigin = findViewById(R.id.tvOriginClientBooking);
        tvDestination = findViewById(R.id.tvDestinationClientBooking);
        btnStartBooking = findViewById(R.id.btnStartBooking);
        //btnStartBooking.setEnabled(false);
        btnFinishBooking = findViewById(R.id.btnFinishBooking);
        mImageViewBooking = findViewById(R.id.ivClientBooking);
        tvTime = findViewById(R.id.tv_Time);

        btnCancel = findViewById(R.id.btnCancelBooking);

        mExtraClientId = getIntent().getStringExtra("idClient");

        // Para trazar la ruta del chofer con el cliente
        mGooglePrvider = new GoogleApiProvider(MapDriverBookingRouteActivity.this);

        // Metodo para obtener los datos del cliente
        getClient();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CancelDelivery();
            }
        });

        btnStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCloseToClient){
                    startBooking();
                }
                else {
                    Toast.makeText(MapDriverBookingRouteActivity.this,"Debes estar mas cerca a la posición de recogida",Toast.LENGTH_LONG).show();
                }
            }
        });

        btnFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishBooking();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // SI AUN NO FINALIZA EL VIAJE ENTONCES QUE SE SIGA ACTUALIZANDO LA UBICACION
        if (!mIsFinishBooking){
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService();
    }

    private void stopLocation(){
        if (mLocationCallback != null && mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
    }

    private void startService(){
        stopLocation();
        Intent serviceIntent = new Intent(this, ForegroundSrevice.class);
        ContextCompat.startForegroundService(MapDriverBookingRouteActivity.this,serviceIntent);
    }

    private void stopService(){
        startLocation(); // Inicializa la ubicacion del conductor en tiempo real
        Intent serviceIntent = new Intent(this, ForegroundSrevice.class);
        stopService(serviceIntent);
    }


    private void finishBooking() {
        // Cambiamos el estado del pedido a iniciar
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mIsFinishBooking = true;

                // PODEMOS LIMPIAR TODOS LOS DATOSS ALMACENADOS EN SHARED PREFERENCE
                mEditor.clear().commit();
                // Notificacion push para el cliente
                sendNotification("Viaje finalizado");
                // Para eliminar el nodo de conductor trabajando y dejar de actualizar su ubicacion
                if(mFusedLocation != null){
                    // Ubicacion
                    mFusedLocation.removeLocationUpdates(mLocationCallback);
                }
                // Nodo eliminado
                mGeofireProvider.removeLocation(mAuthProvider.getId());

                if(mHandler != null) {
                    mHandler.removeCallbacks(runnable); // deja de escuchar para que pare el temporizador
                }

                calculateRide();
            }
        }) ;




    }

    private void calculateRide() {

        mClientBookingProvider.updateStatus(mExtraClientId,"finish").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent =  new Intent(MapDriverBookingRouteActivity.this, CalificationClientActivity.class);
                intent.putExtra("idClient",mExtraClientId);
                intent.putExtra("numDelivery",numDelivery);
                startActivity(intent);
                finish();
            }
        });
    }

    private void CancelDelivery(){

        mClientBookingProvider.updateStatus(mExtraClientId,"cancel").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mIsFinishBooking = true;

                mEditor.clear().commit();
                sendNotification("Viaje cancelado");


                if(mFusedLocation != null){
                    // Ubicacion
                    mFusedLocation.removeLocationUpdates(mLocationCallback);
                }
                // Nodo eliminado
                mGeofireProvider.removeLocation(mAuthProvider.getId());

                if(mHandler != null) {
                    mHandler.removeCallbacks(runnable); // deja de escuchar para que pare el temporizador
                }

                Intent intent = new Intent(MapDriverBookingRouteActivity.this, MapDriver.class);
                startActivity(intent);
                finish();

            }
        });

    }

    private void startBooking() {
        if (numDelivery.equals("2")){

            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();
            // Añadir un marcador
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);

            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");

            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro
        }
        else if (numDelivery.equals("3")){

            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();
            // Añadir un marcador
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");

            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro

        }
        else if (numDelivery.equals("4")){

            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();

            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng4).title("Destino4").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();

            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            drawRoute(mDestinationLatLng4);

            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");

            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro
        }
        else if (numDelivery.equals("5")){

            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng4).title("Destino4").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng5).title("Destino5").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();

            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            drawRoute(mDestinationLatLng4);
            drawRoute(mDestinationLatLng5);

            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");

            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro
        }
        else if (numDelivery.equals("6")){

            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng4).title("Destino4").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng5).title("Destino5").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng6).title("Destino6").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();

            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            drawRoute(mDestinationLatLng4);
            drawRoute(mDestinationLatLng5);
            drawRoute(mDestinationLatLng6);


            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");
            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro
        }
        else if (numDelivery.equals("7")){
            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng4).title("Destino4").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng5).title("Destino5").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng6).title("Destino6").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng7).title("Destino7").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();

            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            drawRoute(mDestinationLatLng4);
            drawRoute(mDestinationLatLng5);
            drawRoute(mDestinationLatLng6);
            drawRoute(mDestinationLatLng7);


            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");

            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro

        }
        else if (numDelivery.equals("8")){
            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng4).title("Destino4").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng5).title("Destino5").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng6).title("Destino6").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng7).title("Destino7").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng8).title("Destino8").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();

            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            drawRoute(mDestinationLatLng4);
            drawRoute(mDestinationLatLng5);
            drawRoute(mDestinationLatLng6);
            drawRoute(mDestinationLatLng7);
            drawRoute(mDestinationLatLng8);

            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");

            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro

        }
        else if (numDelivery.equals("9")){
            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng4).title("Destino4").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng5).title("Destino5").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng6).title("Destino6").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng7).title("Destino7").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng8).title("Destino8").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng9).title("Destino9").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();

            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            drawRoute(mDestinationLatLng4);
            drawRoute(mDestinationLatLng5);
            drawRoute(mDestinationLatLng6);
            drawRoute(mDestinationLatLng7);
            drawRoute(mDestinationLatLng8);
            drawRoute(mDestinationLatLng9);

            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");

            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro

        }
        else if (numDelivery.equals("10")){
            mEditor.putString("status","start");
            mEditor.putString("idClient",mExtraClientId);
            mEditor.apply();
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino1").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng2).title("Destino2").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng3).title("Destino3").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng4).title("Destino4").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng5).title("Destino5").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng6).title("Destino6").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng7).title("Destino7").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng8).title("Destino8").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng9).title("Destino9").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
            mMap.addMarker(new MarkerOptions().position(mDestinationLatLng10).title("Destino10").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

            // Cambiamos el estado del pedido a iniciar
            mClientBookingProvider.updateStatus(mExtraClientId,"start");
            btnStartBooking.setVisibility(View.GONE);
            btnFinishBooking.setVisibility(View.VISIBLE);
            // Borramos la ruta y el marcador
            mMap.clear();

            drawRoute(mDestinationLatLng);
            drawRoute(mDestinationLatLng2);
            drawRoute(mDestinationLatLng3);
            drawRoute(mDestinationLatLng4);
            drawRoute(mDestinationLatLng5);
            drawRoute(mDestinationLatLng6);
            drawRoute(mDestinationLatLng7);
            drawRoute(mDestinationLatLng8);
            drawRoute(mDestinationLatLng9);
            drawRoute(mDestinationLatLng10);

            // Viaje iniciado notificacion para el cliente
            sendNotification("Viaje iniciado");
            mRideStar = true;
            mHandler.postDelayed(runnable,1000);// Llamar al cronometro

        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    // Metodo en el cual se va a medir la distancia para verificar que el conductor inicie el pedido
    private double getDisctanceBetween(LatLng clientLatLng, LatLng driverLatLng){
        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLatitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLatitude(driverLatLng.longitude);

        // Distancia que existe entre el cliente y el condutor
        distance = clientLocation.distanceTo(driverLocation);
        return distance;
    }

    private void getClientBooking() {

        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if (numDelivery.equals("2")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();

                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }


                    }
                    else if (numDelivery.equals("3")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);


                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }

                    }
                    else if (numDelivery.equals("4")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        String destination4 = snapshot.child("destination4").getValue().toString();

                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());
                        double destinationLat4 = Double.parseDouble(snapshot.child("destinationLat4").getValue().toString());
                        double destinationLng4 = Double.parseDouble(snapshot.child("destinationLng4").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);
                        mDestinationLatLng4 = new LatLng(destinationLat4,destinationLng4);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }
                    }
                    else if (numDelivery.equals("5")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        String destination4 = snapshot.child("destination4").getValue().toString();
                        String destination5 = snapshot.child("destination5").getValue().toString();

                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());
                        double destinationLat4 = Double.parseDouble(snapshot.child("destinationLat4").getValue().toString());
                        double destinationLng4 = Double.parseDouble(snapshot.child("destinationLng4").getValue().toString());
                        double destinationLat5 = Double.parseDouble(snapshot.child("destinationLat5").getValue().toString());
                        double destinationLng5 = Double.parseDouble(snapshot.child("destinationLng5").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);
                        mDestinationLatLng4 = new LatLng(destinationLat4,destinationLng4);
                        mDestinationLatLng5 = new LatLng(destinationLat5,destinationLng5);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }
                    }
                    else if (numDelivery.equals("6")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        String destination4 = snapshot.child("destination4").getValue().toString();
                        String destination5 = snapshot.child("destination5").getValue().toString();
                        String destination6 = snapshot.child("destination6").getValue().toString();

                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());
                        double destinationLat4 = Double.parseDouble(snapshot.child("destinationLat4").getValue().toString());
                        double destinationLng4 = Double.parseDouble(snapshot.child("destinationLng4").getValue().toString());
                        double destinationLat5 = Double.parseDouble(snapshot.child("destinationLat5").getValue().toString());
                        double destinationLng5 = Double.parseDouble(snapshot.child("destinationLng5").getValue().toString());
                        double destinationLat6 = Double.parseDouble(snapshot.child("destinationLat6").getValue().toString());
                        double destinationLng6 = Double.parseDouble(snapshot.child("destinationLng6").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);
                        mDestinationLatLng4 = new LatLng(destinationLat4,destinationLng4);
                        mDestinationLatLng5 = new LatLng(destinationLat5,destinationLng5);
                        mDestinationLatLng6 = new LatLng(destinationLat6,destinationLng6);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }
                    }
                    else if (numDelivery.equals("7")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        String destination4 = snapshot.child("destination4").getValue().toString();
                        String destination5 = snapshot.child("destination5").getValue().toString();
                        String destination6 = snapshot.child("destination6").getValue().toString();
                        String destination7 = snapshot.child("destination7").getValue().toString();

                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());
                        double destinationLat4 = Double.parseDouble(snapshot.child("destinationLat4").getValue().toString());
                        double destinationLng4 = Double.parseDouble(snapshot.child("destinationLng4").getValue().toString());
                        double destinationLat5 = Double.parseDouble(snapshot.child("destinationLat5").getValue().toString());
                        double destinationLng5 = Double.parseDouble(snapshot.child("destinationLng5").getValue().toString());
                        double destinationLat6 = Double.parseDouble(snapshot.child("destinationLat6").getValue().toString());
                        double destinationLng6 = Double.parseDouble(snapshot.child("destinationLng6").getValue().toString());
                        double destinationLat7 = Double.parseDouble(snapshot.child("destinationLat7").getValue().toString());
                        double destinationLng7 = Double.parseDouble(snapshot.child("destinationLng7").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);
                        mDestinationLatLng4 = new LatLng(destinationLat4,destinationLng4);
                        mDestinationLatLng5 = new LatLng(destinationLat5,destinationLng5);
                        mDestinationLatLng6 = new LatLng(destinationLat6,destinationLng6);
                        mDestinationLatLng7 = new LatLng(destinationLat7,destinationLng7);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }

                    }
                    else if (numDelivery.equals("8")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        String destination4 = snapshot.child("destination4").getValue().toString();
                        String destination5 = snapshot.child("destination5").getValue().toString();
                        String destination6 = snapshot.child("destination6").getValue().toString();
                        String destination7 = snapshot.child("destination7").getValue().toString();
                        String destination8 = snapshot.child("destination8").getValue().toString();
                        double originLat = Double.parseDouble(snapshot.child("originLat1").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng1").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());
                        double destinationLat4 = Double.parseDouble(snapshot.child("destinationLat4").getValue().toString());
                        double destinationLng4 = Double.parseDouble(snapshot.child("destinationLng4").getValue().toString());
                        double destinationLat5 = Double.parseDouble(snapshot.child("destinationLat5").getValue().toString());
                        double destinationLng5 = Double.parseDouble(snapshot.child("destinationLng5").getValue().toString());
                        double destinationLat6 = Double.parseDouble(snapshot.child("destinationLat6").getValue().toString());
                        double destinationLng6 = Double.parseDouble(snapshot.child("destinationLng6").getValue().toString());
                        double destinationLat7 = Double.parseDouble(snapshot.child("destinationLat7").getValue().toString());
                        double destinationLng7 = Double.parseDouble(snapshot.child("destinationLng7").getValue().toString());
                        double destinationLat8 = Double.parseDouble(snapshot.child("destinationLat8").getValue().toString());
                        double destinationLng8 = Double.parseDouble(snapshot.child("destinationLng8").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);
                        mDestinationLatLng4 = new LatLng(destinationLat4,destinationLng4);
                        mDestinationLatLng5 = new LatLng(destinationLat5,destinationLng5);
                        mDestinationLatLng6 = new LatLng(destinationLat6,destinationLng6);
                        mDestinationLatLng7 = new LatLng(destinationLat7,destinationLng7);
                        mDestinationLatLng8 = new LatLng(destinationLat8,destinationLng8);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }
                    }
                    else if (numDelivery.equals("9")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        String destination4 = snapshot.child("destination4").getValue().toString();
                        String destination5 = snapshot.child("destination5").getValue().toString();
                        String destination6 = snapshot.child("destination6").getValue().toString();
                        String destination7 = snapshot.child("destination7").getValue().toString();
                        String destination8 = snapshot.child("destination8").getValue().toString();
                        String destination9 = snapshot.child("destination9").getValue().toString();

                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());
                        double destinationLat4 = Double.parseDouble(snapshot.child("destinationLat4").getValue().toString());
                        double destinationLng4 = Double.parseDouble(snapshot.child("destinationLng4").getValue().toString());
                        double destinationLat5 = Double.parseDouble(snapshot.child("destinationLat5").getValue().toString());
                        double destinationLng5 = Double.parseDouble(snapshot.child("destinationLng5").getValue().toString());
                        double destinationLat6 = Double.parseDouble(snapshot.child("destinationLat6").getValue().toString());
                        double destinationLng6 = Double.parseDouble(snapshot.child("destinationLng6").getValue().toString());
                        double destinationLat7 = Double.parseDouble(snapshot.child("destinationLat7").getValue().toString());
                        double destinationLng7 = Double.parseDouble(snapshot.child("destinationLng7").getValue().toString());
                        double destinationLat8 = Double.parseDouble(snapshot.child("destinationLat8").getValue().toString());
                        double destinationLng8 = Double.parseDouble(snapshot.child("destinationLng8").getValue().toString());
                        double destinationLat9 = Double.parseDouble(snapshot.child("destinationLat9").getValue().toString());
                        double destinationLng9 = Double.parseDouble(snapshot.child("destinationLng9").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);
                        mDestinationLatLng4 = new LatLng(destinationLat4,destinationLng4);
                        mDestinationLatLng5 = new LatLng(destinationLat5,destinationLng5);
                        mDestinationLatLng6 = new LatLng(destinationLat6,destinationLng6);
                        mDestinationLatLng7 = new LatLng(destinationLat7,destinationLng7);
                        mDestinationLatLng8 = new LatLng(destinationLat8,destinationLng8);
                        mDestinationLatLng9 = new LatLng(destinationLat9,destinationLng9);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }
                    }
                    else if (numDelivery.equals("10")){
                        // Destino a donde se dirige el cliente
                        String origin = snapshot.child("origin").getValue().toString();
                        String destination = snapshot.child("destination1").getValue().toString();
                        String destination2 = snapshot.child("destination2").getValue().toString();
                        String destination3 = snapshot.child("destination3").getValue().toString();
                        String destination4 = snapshot.child("destination4").getValue().toString();
                        String destination5 = snapshot.child("destination5").getValue().toString();
                        String destination6 = snapshot.child("destination6").getValue().toString();
                        String destination7 = snapshot.child("destination7").getValue().toString();
                        String destination8 = snapshot.child("destination8").getValue().toString();
                        String destination9 = snapshot.child("destination9").getValue().toString();
                        String destination10 = snapshot.child("destination10").getValue().toString();

                        double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                        double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                        double destinationLat = Double.parseDouble(snapshot.child("destinationLat1").getValue().toString());
                        double destinationLng = Double.parseDouble(snapshot.child("destinationLng1").getValue().toString());
                        double destinationLat2 = Double.parseDouble(snapshot.child("destinationLat2").getValue().toString());
                        double destinationLng2 = Double.parseDouble(snapshot.child("destinationLng2").getValue().toString());
                        double destinationLat3 = Double.parseDouble(snapshot.child("destinationLat3").getValue().toString());
                        double destinationLng3 = Double.parseDouble(snapshot.child("destinationLng3").getValue().toString());
                        double destinationLat4 = Double.parseDouble(snapshot.child("destinationLat4").getValue().toString());
                        double destinationLng4 = Double.parseDouble(snapshot.child("destinationLng4").getValue().toString());
                        double destinationLat5 = Double.parseDouble(snapshot.child("destinationLat5").getValue().toString());
                        double destinationLng5 = Double.parseDouble(snapshot.child("destinationLng5").getValue().toString());
                        double destinationLat6 = Double.parseDouble(snapshot.child("destinationLat6").getValue().toString());
                        double destinationLng6 = Double.parseDouble(snapshot.child("destinationLng6").getValue().toString());
                        double destinationLat7 = Double.parseDouble(snapshot.child("destinationLat7").getValue().toString());
                        double destinationLng7 = Double.parseDouble(snapshot.child("destinationLng7").getValue().toString());
                        double destinationLat8 = Double.parseDouble(snapshot.child("destinationLat8").getValue().toString());
                        double destinationLng8 = Double.parseDouble(snapshot.child("destinationLng8").getValue().toString());
                        double destinationLat9 = Double.parseDouble(snapshot.child("destinationLat9").getValue().toString());
                        double destinationLng9 = Double.parseDouble(snapshot.child("destinationLng9").getValue().toString());
                        double destinationLat10 = Double.parseDouble(snapshot.child("destinationLat10").getValue().toString());
                        double destinationLng10 = Double.parseDouble(snapshot.child("destinationLng10").getValue().toString());

                        mOrigenLatLng = new LatLng(originLat,originLng);
                        mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                        mDestinationLatLng2 = new LatLng(destinationLat2,destinationLng2);
                        mDestinationLatLng3 = new LatLng(destinationLat3,destinationLng3);
                        mDestinationLatLng4 = new LatLng(destinationLat4,destinationLng4);
                        mDestinationLatLng5 = new LatLng(destinationLat5,destinationLng5);
                        mDestinationLatLng6 = new LatLng(destinationLat6,destinationLng6);
                        mDestinationLatLng7 = new LatLng(destinationLat7,destinationLng7);
                        mDestinationLatLng8 = new LatLng(destinationLat8,destinationLng8);
                        mDestinationLatLng9 = new LatLng(destinationLat9,destinationLng9);
                        mDestinationLatLng10 = new LatLng(destinationLat10,destinationLng10);

                        tvOrigin.setText("Recoger en: " + origin);
                        tvDestination.setText("Entregar en: " + destination);

                        mPref = getApplicationContext().getSharedPreferences("RideStatus",MODE_PRIVATE);
                        mEditor = mPref.edit();
                        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
                        String status = mPref.getString("status","");

                        if (status.equals("start")){

                            //String mExtraPrice = getIntent().getStringExtra("price");
                            //double mExtraPriceKm = getIntent().getDoubleExtra("priceKm",0);
                            startBooking(); // Si ya inicio el viaje va a trazar la ruta a la posicion de destino
                            //startBooking();
                        }
                        else {
                            // ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA POR PRIMERA VEZ EL VIAJE
                            mEditor.putString("status","ride");
                            mEditor.putString("idClient",mExtraClientId);
                            mEditor.apply();// guarda informacion en el celular
                            // Añadir un marcador
                            Log.d("OrigenCliente", String.valueOf(mOrigenLatLng));
                            mMap.addMarker(new MarkerOptions().position(mOrigenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                            drawRoute(mOrigenLatLng);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void drawRoute(LatLng latLng){
        mGooglePrvider.getDirection(mCurrentLatLng,latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    // Para obtener la distancia y el tiempo // duration_in_traffic

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    // Se puede obtener la duracion de camino con trafico y sin trafico ----> sin trafico : duration
                    JSONObject duration = leg.getJSONObject("duration_in_traffic");

                    // Obtener en un string la distancia y duracion
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");



                }catch (Exception e){
                    Log.d("Error", "Error encontrado" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void getClient() {
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String phoneOrigin = snapshot.child("phone").getValue().toString();
                    String name = snapshot.child("name").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(MapDriverBookingRouteActivity.this).load(image).into(mImageViewBooking);
                    }

                    tvClientBooking.setText(name);
                    tvPhoneOrigin.setText(phoneOrigin);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Poner los botones de zoom en el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);


        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(1000);
        //mLocationRequest.setFastestInterval(1000);
        // Establecer la prioridad que va a tener el gps en la actualizacion del gps
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        // Si quiero utilizar el punto azul y tendriamos que poner en todos los mFused menos en disconect
                        // PAra obtener el punto exacto
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(false);
                    }
                    else {
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
                                ActivityCompat.requestPermissions(MapDriverBookingRouteActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBookingRouteActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }
    }

    private void updateLocation(){
        if(mAuthProvider.existSession() && mCurrentLatLng !=null){
            mGeofireProvider.saveLocation(mAuthProvider.getId(),mCurrentLatLng);
            if(!mIsCloseToClient){
                if (mOrigenLatLng != null && mCurrentLatLng != null){
                    double distance = getDisctanceBetween(mOrigenLatLng,mCurrentLatLng); // METROS
                    if(distance <= 200){
                        //btnStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this,"Estas cerca a la posición de recogida ",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // Para mandar notificaciones al usuario del estado de su pedido
    private void sendNotification(final String status) {
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Contiene la informacion del nodo de token

                if (snapshot.exists()){

                    String token = snapshot.child("token").getValue().toString();

                    Map<String, String> map = new HashMap<>();
                    map.put("title","ESTADO DE TU VIAJE");
                    map.put("body",
                            "Tu estado del viaje es: " + status
                    );
                    FCMBody fcmBody = new FCMBody(token,"high","4500s",map);

                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                // Condicion que nos avisa si se envio diferente de 1 es que no se envio
                                if(response.body().getSuccess() != 1){
                                    Toast.makeText(MapDriverBookingRouteActivity.this,"No se pudo enviar la notificación",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(MapDriverBookingRouteActivity.this,"No se pudo enviar la notificación",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                            Log.d("Error","Error"+t.getMessage());

                        }
                    });

                }
                else {
                    Toast.makeText(MapDriverBookingRouteActivity.this,"No se pudo enviar la notificación porque el conductor no tiene un token de sesión",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}