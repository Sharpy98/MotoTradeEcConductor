package com.henryalmeida.mototradeecconductor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientBookingProvider;
import com.henryalmeida.mototradeecconductor.providiers.DriversFoundProvider;
import com.henryalmeida.mototradeecconductor.providiers.GeofireProvider;
import com.henryalmeida.mototradeecconductor.providiers.Route.BookingProviderRoute;

public class NotificationBookingActivity extends AppCompatActivity {

    private TextView tvDestination;
    private TextView tvOrigin;
    private TextView tvMin;
    private TextView tvDistance;
    private TextView tvCounter;

    private Button btnAccept;
    private Button btnCancel;

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    private String mExtraIdClient;
    private String mExtraOrigin;
    private String mExtraDestination;
    private String mExtraMin;
    private String mExtraDistance;


    SharedPreferences mPrefPrice;
    SharedPreferences.Editor mEditorPrice;

    private DriversFoundProvider mDriversFoundProvider;

    private ValueEventListener mListener;

    private MediaPlayer mMediaPlayer;
    private int mCounter = 20;
    // Para el temporizador
    private Handler mHandelr;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCounter = mCounter-1;
            tvCounter.setText(String.valueOf(mCounter));
            if (mCounter > 0){
                iniTimer();
            }
            else {
                cancelBooking();
            }
        }
    };

    private void iniTimer() {
        mHandelr = new Handler();
        mHandelr.postDelayed(runnable,1000);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        mPrefPrice = getSharedPreferences("pricePreference",Context.MODE_PRIVATE);
        mEditorPrice = mPrefPrice.edit();

        tvDestination = findViewById(R.id.tvDestination);
        tvOrigin = findViewById(R.id.tvOrigin);
        tvMin = findViewById(R.id.tvMin);
        tvDistance = findViewById(R.id.tvDistance);
        tvCounter = findViewById(R.id.tvCounter);


        btnAccept = findViewById(R.id.btnAcceptBooking);
        btnCancel = findViewById(R.id.btnCancelBooking);

        mAuthProvider = new AuthProvider();
        mDriversFoundProvider = new DriversFoundProvider();

        mExtraIdClient = getIntent().getStringExtra("idClient");
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraMin = getIntent().getStringExtra("min");
        mExtraDistance= getIntent().getStringExtra("distance");

        tvOrigin.setText(mExtraOrigin);
        tvDestination.setText(mExtraDestination);
        tvMin.setText(mExtraMin);
        tvDistance.setText(mExtraDistance);

        // Tono para la notificacion del chofer
        mMediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mMediaPlayer.setLooping(true);

        mClientBookingProvider = new ClientBookingProvider();

        // Para que se muestre auque este bloqueado
        getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );


        iniTimer();

        checkIfClientCancelBooking();

        //Log.d("DISTANCES",mExtraDistance);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptBooking();
            }
        });
        
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBooking();
            }
        });
    }

    private void checkIfClientBookingWasAccept(final String idClient, final Context context) {

        mClientBookingProvider.getClientBooking(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("idDriver") && snapshot.hasChild("status")) {
                        String status = snapshot.child("status").getValue().toString();
                        String idDriver = snapshot.child("idDriver").getValue().toString();

                        if (status.equals("create") && idDriver.equals("")) {
                            // En la base de datos cambiamos el estado del pedido
                            mClientBookingProvider.updateStatusAndIdDriver(idClient,"accept",mAuthProvider.getId());

                            Intent intent1 = new Intent(context,MapDriverBooking.class);
                            // Para que el conductor ya pueda volver a la pantalla anterior
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.setAction(Intent.ACTION_RUN);
                            intent1.putExtra("idClient",idClient);
                            intent1.putExtra("km",mExtraDistance);
                            mEditorPrice.putString("km",mExtraDistance);
                            mEditorPrice.apply();
                            context.startActivity(intent1);
                        }
                        else {
                            goToMapDriverActivity(context);
                        }
                    }
                    else {
                        goToMapDriverActivity(context);
                    }
                }
                else {
                    goToMapDriverActivity(context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void goToMapDriverActivity(Context context) {
        Toast.makeText(context, "Otro conductor ya acepto el viaje", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(context, MapDriver.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        context.startActivity(intent1);
    }

    // Para saber en timpo real si existe el pedido en la base de datos
    private void checkIfClientCancelBooking(){

            mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        goToMapDriverActivity();
                    }
                    //  SIGNIFICA QUE EL CLIENT BOOKING EXISTE
                    else if (snapshot.hasChild("idDriver") && snapshot.hasChild("status")){
                        String idDriver = snapshot.child("idDriver").getValue().toString();
                        String status = snapshot.child("status").getValue().toString();

                        if ((status.equals("accept") || status.equals("cancel")) && !idDriver.equals(mAuthProvider.getId())){
                            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                            manager.cancel(2);
                            goToMapDriverActivity();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void goToMapDriverActivity(){
        Toast.makeText(NotificationBookingActivity.this, "El cliente ya no esta disponible", Toast.LENGTH_LONG).show();
        if (mHandelr != null) mHandelr.removeCallbacks(runnable);

        Intent intent = new Intent(NotificationBookingActivity.this, MapDriver.class);
        startActivity(intent);
        finish();
    }

    private void cancelBooking() {

            // Para que el contador no siga corriendo
            if(mHandelr != null)mHandelr.removeCallbacks(runnable);
            // En la base de datos cambiamos el estado del pedido
            //mClientBookingProvider.updateStatus(mExtraIdClient,"cancel");

            mDriversFoundProvider.delete(mAuthProvider.getId());

            // Para que cuando acepte el pedido desaparezca la notificacion
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(2);

            Intent intent = new Intent(NotificationBookingActivity.this,MapDriver.class);
            startActivity(intent);
            finish();
    }

    private void acceptBooking() {

            if(mHandelr != null)mHandelr.removeCallbacks(runnable);
            // Para que cuando acepte el conductor el pedido desaparezca del nodo de condutores activos

            mGeofireProvider = new GeofireProvider("active_drivers");
            mGeofireProvider.removeLocation(mAuthProvider.getId());

            mClientBookingProvider = new ClientBookingProvider();

            // Para que cuando acepte el pedido desaparezca la notificacion
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(2);

            checkIfClientBookingWasAccept(mExtraIdClient,NotificationBookingActivity.this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
        }
    }

    // Eso es cuando minimizamos la app
    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.release();
            }
        }
    }

    // Cuando la actividad ya a sido creada
    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null){
            if(!mMediaPlayer.isPlaying()){
                mMediaPlayer.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandelr != null) mHandelr.removeCallbacks(runnable);
        if (mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
        }
        if (mListener != null){
            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);
        }
    }
}