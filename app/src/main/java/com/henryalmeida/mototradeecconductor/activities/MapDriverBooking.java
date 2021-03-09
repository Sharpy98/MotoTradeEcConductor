package com.henryalmeida.mototradeecconductor.activities;

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
import android.app.NotificationManager;
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
import com.henryalmeida.mototradeecconductor.models.ClienttBooking;
import com.henryalmeida.mototradeecconductor.models.FCMBody;
import com.henryalmeida.mototradeecconductor.models.FCMResponse;
import com.henryalmeida.mototradeecconductor.models.Info;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientBookingProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientProvider;
import com.henryalmeida.mototradeecconductor.providiers.DriversFoundProvider;
import com.henryalmeida.mototradeecconductor.providiers.GeofireProvider;
import com.henryalmeida.mototradeecconductor.providiers.GoogleApiProvider;
import com.henryalmeida.mototradeecconductor.providiers.InfoProvider;
import com.henryalmeida.mototradeecconductor.providiers.NotificationProvider;
import com.henryalmeida.mototradeecconductor.providiers.TokenProvider;
import com.henryalmeida.mototradeecconductor.services.ForegroundSrevice;
import com.henryalmeida.mototradeecconductor.utils.DecodePoints;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDriverBooking extends AppCompatActivity implements OnMapReadyCallback {

    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClientProvider mClientProvider;
    private Info mInfo;
    private ClientBookingProvider mClientBookingProvider;
    private NotificationProvider mNotificationProvider;
    private InfoProvider mInfoProvider;

    private String mExtraDistance;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragmet;

    // BANDERA PARA PERMISO DE UBICACION
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    // Para cambiar el icono en el mapa
    private Marker mMarker;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private DriversFoundProvider mDriversFoundProvider;

    private LatLng mCurrentLatLng;

    private TextView tvClientBooking;
    private TextView tvPhoneOrigin;
    private TextView tvOrigin;
    private TextView tvDestination;
    private TextView tvTime;
    private TextView tvPrice;
    private TextView tvCollectMoney;
    private Button btnCancel;

    private Button btnStartBooking;
    private Button btnFinishBooking;

    private ImageView mImageViewBooking;

    private String mExtraClientId;


    // Para trazar la ruta del conductor hacia el cliente
    private LatLng mOrigenLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGooglePrvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    // Para que nos diga que si fue la primera vez que entro al locationCallback
    private boolean mIsFirstTime = true;
    // Para que llame una sola vez a la habilitacion del boton
    private boolean mIsCloseToClient = false;

    // Para el tiempo y el precio
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

    SharedPreferences mPrefPrice;
    SharedPreferences.Editor mEditorPrice;

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
        setContentView(R.layout.activity_map_driver_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mNotificationProvider = new NotificationProvider();

        mInfoProvider = new InfoProvider();
        mDriversFoundProvider = new DriversFoundProvider();


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
        tvPrice = findViewById(R.id.tv_Price);
        tvCollectMoney = findViewById(R.id.tvCollectMoney);

        btnCancel = findViewById(R.id.btnCancelBooking);

        mExtraDistance= getIntent().getStringExtra("km");
        mExtraClientId = getIntent().getStringExtra("idClient");

        // Para trazar la ruta del chofer con el cliente
        mGooglePrvider = new GoogleApiProvider(MapDriverBooking.this);

        // Metodo para obtener los datos del cliente
        getClient();

        //Log.d("DISTANCES",mExtraDistance);
        //Toast.makeText(this, mExtraDistance, Toast.LENGTH_SHORT).show();

        getInfo();
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
                    Toast.makeText(MapDriverBooking.this,"Debes estar mas cerca a la posición de recogida",Toast.LENGTH_LONG).show();
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
        ContextCompat.startForegroundService(MapDriverBooking.this,serviceIntent);
    }

    private void stopService(){
        startLocation(); // Inicializa la ubicacion del conductor en tiempo real
        Intent serviceIntent = new Intent(this, ForegroundSrevice.class);
        stopService(serviceIntent);
    }

    private void getInfo() {
        // Obtener informacion del precio
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    mInfo = snapshot.getValue(Info.class);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        });
    }

    private void calculateRide(){

        mPrefPrice = getApplicationContext().getSharedPreferences("pricePreference",MODE_PRIVATE);
        mEditorPrice = mPref.edit();
        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
        String distancePrice = mPrefPrice.getString("km","");

        Log.d("DISTANCEE",distancePrice);

        /*String[] distanceAndKm = distancePrice.split(" "); // Para partir el string con un espacio y se guarde cada uno en una posicion
        double distanceValue = Double.parseDouble(distanceAndKm[0]);

        double pricekm = distanceValue * mInfo.getKm();*/

        final double[] pricekm = new double[1];
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    // Para obtener toda la informacion de una sola
                    DecimalFormat df = new DecimalFormat("#.00");
                    ClienttBooking clienttBooking = snapshot.getValue(ClienttBooking.class);
                    double price = Double.parseDouble(df.format(clienttBooking.getPrice()));
                    pricekm[0] = price;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Log.d("VALORES","Precio Total: " + pricekm);

        // Para asegurarnos que primero se gurde dicha informacion en la base de datos
        mClientBookingProvider.updateStatus(mExtraClientId,"finish").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent =  new Intent(MapDriverBooking.this, CalificationClientActivity.class);
                intent.putExtra("idClient",mExtraClientId);
                intent.putExtra("numDelivery","1");
                intent.putExtra("price", pricekm[0]);
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

                Intent intent = new Intent(MapDriverBooking.this,MapDriver.class);
                startActivity(intent);
                finish();

            }
        });

    }


    private void startBooking() {


        mPrefPrice = getApplicationContext().getSharedPreferences("pricePreference",MODE_PRIVATE);
        mEditorPrice = mPref.edit();
        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
        String distancePrice = mPrefPrice.getString("km","");

        Log.d("DISTANCES",distancePrice);

        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String mPrice = snapshot.child("price").getValue().toString();
                //String[] distanceAndKm = distancePrice.split(" "); // Para partir el string con un espacio y se guarde cada uno en una posicion
                //double distanceValue = Double.parseDouble(distanceAndKm[0]);

                double distanceValue = Double.parseDouble(mPrice);
                double pricekm = 0;
                String price = String.valueOf(distanceValue);

                /*DecimalFormat df = new DecimalFormat("#.00");
                if(distanceValue > 8){
                    pricekm = distanceValue * mInfo.getKm();
                    price = String.valueOf(String.format("%.2f",pricekm));
                }else {
                    price = "1.50";
                }*/


                mEditor.putString("status","start");
                mEditor.putString("idClient",mExtraClientId);
                mEditor.putFloat("priceKm", (float) pricekm);
                mEditor.putString("price",price);
                mEditor.apply();

                // Cambiamos el estado del pedido a iniciar
                mClientBookingProvider.updateStatus(mExtraClientId,"start");
                btnStartBooking.setVisibility(View.GONE);
                btnFinishBooking.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.GONE);
                tvPrice.setText(price);
                // Borramos la ruta y el marcador
                mMap.clear();
                // Añadir un marcador
                mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
                drawRoute(mDestinationLatLng);
                // Viaje iniciado notificacion para el cliente
                sendNotification("Viaje iniciado");
                mRideStar = true;
                mHandler.postDelayed(runnable,1000);// Llamar al cronometro
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                    // Destino a donde se dirige el cliente
                    String destination = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                    String collectMoney = snapshot.child("collectMoney").getValue().toString();

                    mOrigenLatLng = new LatLng(originLat,originLng);
                    mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                    tvOrigin.setText("Recoger en: " + origin);
                    tvDestination.setText("Entregar en: " + destination);
                    tvCollectMoney.setText(collectMoney);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void startBooking(String mExtraPrice, double mExtraPriceKm) {

        mPrefPrice = getApplicationContext().getSharedPreferences("pricePreference",MODE_PRIVATE);
        mEditorPrice = mPref.edit();
        // ES OBTENER EL ULTIMO ESTADO ALMACENDO EN EL SHARED PREFERENCE
        String distancePrice = mPrefPrice.getString("km","");

        Log.d("DISTANCES",distancePrice);

        double pricekm = mExtraPriceKm;
        String price = mExtraPrice;

        mEditor.putString("status","start");
        mEditor.putString("idClient",mExtraClientId);
        mEditor.putFloat("priceKm", (float) pricekm);
        mEditor.putString("price",price);
        mEditor.apply();

        // Cambiamos el estado del pedido a iniciar
        mClientBookingProvider.updateStatus(mExtraClientId,"start");
        btnStartBooking.setVisibility(View.GONE);
        btnFinishBooking.setVisibility(View.VISIBLE);
        tvPrice.setText("Cobrar: $ "+price);
        // Borramos la ruta y el marcador
        mMap.clear();
        // Añadir un marcador
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
        drawRoute(mDestinationLatLng);
        // Viaje iniciado notificacion para el cliente
        sendNotification("Viaje iniciado");
        mRideStar = true;
        mHandler.postDelayed(runnable,1000);// Llamar al cronometro
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
                        Picasso.with(MapDriverBooking.this).load(image).into(mImageViewBooking);
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

    // Inicializaba la localicazion en timpor real
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
                                ActivityCompat.requestPermissions(MapDriverBooking.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBooking.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
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
                                    Toast.makeText(MapDriverBooking.this,"No se pudo enviar la notificación",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(MapDriverBooking.this,"No se pudo enviar la notificación",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                            Log.d("Error","Error"+t.getMessage());

                        }
                    });

                }
                else {
                    Toast.makeText(MapDriverBooking.this,"No se pudo enviar la notificación porque el conductor no tiene un token de sesión",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}