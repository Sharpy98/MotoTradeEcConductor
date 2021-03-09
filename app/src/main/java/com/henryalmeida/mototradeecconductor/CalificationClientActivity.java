package com.henryalmeida.mototradeecconductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.henryalmeida.mototradeecconductor.activities.MapDriver;
import com.henryalmeida.mototradeecconductor.models.ClienttBooking;
import com.henryalmeida.mototradeecconductor.models.HistoryBooking;
import com.henryalmeida.mototradeecconductor.models.route.BookingRoute;
import com.henryalmeida.mototradeecconductor.models.route.HistoryBookingRoute;
import com.henryalmeida.mototradeecconductor.providiers.ClientBookingProvider;
import com.henryalmeida.mototradeecconductor.providiers.HistoryBookingProvider;
import com.henryalmeida.mototradeecconductor.providiers.Route.BookingProviderRoute;
import com.henryalmeida.mototradeecconductor.providiers.Route.HistoryBookingRouteProvider;

import java.text.DecimalFormat;
import java.util.Date;

public class CalificationClientActivity extends AppCompatActivity {

    private String numDelivery = "1";
    private TextView tvOrigin;
    private TextView tvDestination;
    private RatingBar mRatingBar;
    private Button btnCalification;

    private HistoryBooking mHistoryBooking;
    private HistoryBookingRoute mHistoryBookingRoute;

    private ClientBookingProvider mClientBookingProvider;
    private BookingProviderRoute mBookingProviderRoute;

    private HistoryBookingProvider mHistoryBookingProvider;
    private HistoryBookingRouteProvider mHistoryBookingRouteProvider;

    private  String mExtraClientId;

    private float mCalification = 0;
    private TextView tvPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_client);

        tvOrigin = findViewById(R.id.tvOriginCalification);
        tvDestination = findViewById(R.id.tvDestinationCalification);
        mRatingBar = findViewById(R.id.ratingBarCalification);
        btnCalification = findViewById(R.id.btnCalification);
        tvPrice = findViewById(R.id.tvPrice);


        mClientBookingProvider = new ClientBookingProvider();
        mBookingProviderRoute = new BookingProviderRoute();

        mHistoryBookingProvider = new HistoryBookingProvider();
        mHistoryBookingRouteProvider = new HistoryBookingRouteProvider();

        mExtraClientId = getIntent().getStringExtra("idClient");
        numDelivery = getIntent().getStringExtra("numDelivery");



        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean fromUser) {
                mCalification = calification;
            }
        });
        
        btnCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numDelivery.equals("1")){
                    calification();
                    Log.d("Calification","Entrobien");
                }
                else {
                    calificationRoute();
                }
            }
        });

        if (numDelivery.equals("1")){
            getclientBooking();
            Log.d("Calification","Entrobien2");

        }
        else {
            getclientBookingRoute();
        }
    }

    private void calificationRoute() {

        if (mCalification > 0 ){
            mHistoryBookingRoute.setCalificationClient(mCalification);
            // Para guardar la fecha y la hora
            mHistoryBookingRoute.setTimeStamp(new Date().getTime());
            mHistoryBookingRouteProvider.getHistoryBooking(mHistoryBookingRoute.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        mHistoryBookingRouteProvider.updateCalificationClient(mHistoryBookingRoute.getIdHistoryBooking(),mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this,"La calificación se guardo correctamente",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapDriver.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else {
                        // Ese listener es para ver si se realizo correctamente la calificacion , Se sobreescribe el pedido pero con el id para tener el historial
                        // Si esta informacion fue creada se actualiza, osino se crea
                        mHistoryBookingRouteProvider.create(mHistoryBookingRoute).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this,"La calificación se guardo correctamente",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapDriver.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            Toast.makeText(CalificationClientActivity.this, "Debes ingresar la calificación", Toast.LENGTH_SHORT).show();
        }

    }
    private  void getclientBookingRoute(){

        mBookingProviderRoute.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    if (numDelivery.equals("2")){
                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2()
                        );
                    }
                    else if(numDelivery.equals("4")){
                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3(),
                                clienttBooking.getDestination4(),
                                clienttBooking.getPhoneDestination4(),
                                clienttBooking.getDestinationLat4(),
                                clienttBooking.getDestinationLng4(),
                                clienttBooking.getPack4()
                        );
                    }
                    else if(numDelivery.equals("5")){

                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3(),
                                clienttBooking.getDestination4(),
                                clienttBooking.getPhoneDestination4(),
                                clienttBooking.getDestinationLat4(),
                                clienttBooking.getDestinationLng4(),
                                clienttBooking.getPack4(),
                                clienttBooking.getDestination5(),
                                clienttBooking.getPhoneDestination5(),
                                clienttBooking.getDestinationLat5(),
                                clienttBooking.getDestinationLng5(),
                                clienttBooking.getPack5()
                        );
                    }
                    else if(numDelivery.equals("6")){

                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3(),
                                clienttBooking.getDestination4(),
                                clienttBooking.getPhoneDestination4(),
                                clienttBooking.getDestinationLat4(),
                                clienttBooking.getDestinationLng4(),
                                clienttBooking.getPack4(),
                                clienttBooking.getDestination5(),
                                clienttBooking.getPhoneDestination5(),
                                clienttBooking.getDestinationLat5(),
                                clienttBooking.getDestinationLng5(),
                                clienttBooking.getPack5(),
                                clienttBooking.getDestination6(),
                                clienttBooking.getPhoneDestination6(),
                                clienttBooking.getDestinationLat6(),
                                clienttBooking.getDestinationLng6(),
                                clienttBooking.getPack6()
                        );
                    }
                    else if(numDelivery.equals("7")){

                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3(),
                                clienttBooking.getDestination4(),
                                clienttBooking.getPhoneDestination4(),
                                clienttBooking.getDestinationLat4(),
                                clienttBooking.getDestinationLng4(),
                                clienttBooking.getPack4(),
                                clienttBooking.getDestination5(),
                                clienttBooking.getPhoneDestination5(),
                                clienttBooking.getDestinationLat5(),
                                clienttBooking.getDestinationLng5(),
                                clienttBooking.getPack5(),
                                clienttBooking.getDestination6(),
                                clienttBooking.getPhoneDestination6(),
                                clienttBooking.getDestinationLat6(),
                                clienttBooking.getDestinationLng6(),
                                clienttBooking.getPack6(),
                                clienttBooking.getDestination7(),
                                clienttBooking.getPhoneDestination7(),
                                clienttBooking.getDestinationLat7(),
                                clienttBooking.getDestinationLng7(),
                                clienttBooking.getPack7()
                        );
                    }
                    else if(numDelivery.equals("8")){
                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3(),
                                clienttBooking.getDestination4(),
                                clienttBooking.getPhoneDestination4(),
                                clienttBooking.getDestinationLat4(),
                                clienttBooking.getDestinationLng4(),
                                clienttBooking.getPack4(),
                                clienttBooking.getDestination5(),
                                clienttBooking.getPhoneDestination5(),
                                clienttBooking.getDestinationLat5(),
                                clienttBooking.getDestinationLng5(),
                                clienttBooking.getPack5(),
                                clienttBooking.getDestination6(),
                                clienttBooking.getPhoneDestination6(),
                                clienttBooking.getDestinationLat6(),
                                clienttBooking.getDestinationLng6(),
                                clienttBooking.getPack6(),
                                clienttBooking.getDestination7(),
                                clienttBooking.getPhoneDestination7(),
                                clienttBooking.getDestinationLat7(),
                                clienttBooking.getDestinationLng7(),
                                clienttBooking.getPack7(),
                                clienttBooking.getDestination8(),
                                clienttBooking.getPhoneDestination8(),
                                clienttBooking.getDestinationLat8(),
                                clienttBooking.getDestinationLng8(),
                                clienttBooking.getPack8()
                        );

                    }
                    else if(numDelivery.equals("9")){

                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3(),
                                clienttBooking.getDestination4(),
                                clienttBooking.getPhoneDestination4(),
                                clienttBooking.getDestinationLat4(),
                                clienttBooking.getDestinationLng4(),
                                clienttBooking.getPack4(),
                                clienttBooking.getDestination5(),
                                clienttBooking.getPhoneDestination5(),
                                clienttBooking.getDestinationLat5(),
                                clienttBooking.getDestinationLng5(),
                                clienttBooking.getPack5(),
                                clienttBooking.getDestination6(),
                                clienttBooking.getPhoneDestination6(),
                                clienttBooking.getDestinationLat6(),
                                clienttBooking.getDestinationLng6(),
                                clienttBooking.getPack6(),
                                clienttBooking.getDestination7(),
                                clienttBooking.getPhoneDestination7(),
                                clienttBooking.getDestinationLat7(),
                                clienttBooking.getDestinationLng7(),
                                clienttBooking.getPack7(),
                                clienttBooking.getDestination8(),
                                clienttBooking.getPhoneDestination8(),
                                clienttBooking.getDestinationLat8(),
                                clienttBooking.getDestinationLng8(),
                                clienttBooking.getPack8(),
                                clienttBooking.getDestination9(),
                                clienttBooking.getPhoneDestination9(),
                                clienttBooking.getDestinationLat9(),
                                clienttBooking.getDestinationLng9(),
                                clienttBooking.getPack9()
                        );

                    }
                    else if(numDelivery.equals("10")){

                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3(),
                                clienttBooking.getDestination4(),
                                clienttBooking.getPhoneDestination4(),
                                clienttBooking.getDestinationLat4(),
                                clienttBooking.getDestinationLng4(),
                                clienttBooking.getPack4(),
                                clienttBooking.getDestination5(),
                                clienttBooking.getPhoneDestination5(),
                                clienttBooking.getDestinationLat5(),
                                clienttBooking.getDestinationLng5(),
                                clienttBooking.getPack5(),
                                clienttBooking.getDestination6(),
                                clienttBooking.getPhoneDestination6(),
                                clienttBooking.getDestinationLat6(),
                                clienttBooking.getDestinationLng6(),
                                clienttBooking.getPack6(),
                                clienttBooking.getDestination7(),
                                clienttBooking.getPhoneDestination7(),
                                clienttBooking.getDestinationLat7(),
                                clienttBooking.getDestinationLng7(),
                                clienttBooking.getPack7(),
                                clienttBooking.getDestination8(),
                                clienttBooking.getPhoneDestination8(),
                                clienttBooking.getDestinationLat8(),
                                clienttBooking.getDestinationLng8(),
                                clienttBooking.getPack8(),
                                clienttBooking.getDestination9(),
                                clienttBooking.getPhoneDestination9(),
                                clienttBooking.getDestinationLat9(),
                                clienttBooking.getDestinationLng9(),
                                clienttBooking.getPack9(),
                                clienttBooking.getDestination10(),
                                clienttBooking.getPhoneDestination10(),
                                clienttBooking.getDestinationLat10(),
                                clienttBooking.getDestinationLng10(),
                                clienttBooking.getPack10()
                        );

                    }
                    else if(numDelivery.equals("3")){

                        // Para obtener toda la informacion de una sola
                        BookingRoute clienttBooking = snapshot.getValue(BookingRoute.class);
                        tvOrigin.setText(clienttBooking.getOrigin());
                        tvDestination.setText("Ruta");
                        mHistoryBookingRoute = new HistoryBookingRoute(
                                clienttBooking.getIdHistoryBooking(),
                                clienttBooking.getIdClient(),
                                clienttBooking.getIdDriver(),
                                clienttBooking.getStatus(),
                                clienttBooking.getOrigin(),
                                clienttBooking.getOriginLat(),
                                clienttBooking.getOriginLng(),
                                clienttBooking.getPhoneOrigin(),
                                clienttBooking.getDestination1(),
                                clienttBooking.getPhoneDestination1(),
                                clienttBooking.getDestinationLat1(),
                                clienttBooking.getDestinationLng1(),
                                clienttBooking.getPack1(),
                                clienttBooking.getDestination2(),
                                clienttBooking.getPhoneDestination2(),
                                clienttBooking.getDestinationLat2(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack2(),
                                clienttBooking.getDestination3(),
                                clienttBooking.getPhoneDestination3(),
                                clienttBooking.getDestinationLat3(),
                                clienttBooking.getDestinationLng2(),
                                clienttBooking.getPack3()
                        );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // Nos trae la informacion
    private  void getclientBooking(){

        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    // Para obtener toda la informacion de una sola
                    DecimalFormat df = new DecimalFormat("#.00");
                    ClienttBooking clienttBooking = snapshot.getValue(ClienttBooking.class);
                    tvOrigin.setText(clienttBooking.getOrigin());
                    tvDestination.setText(clienttBooking.getDestination());
                    double price = Double.parseDouble(df.format(clienttBooking.getPrice()));
                    tvPrice.setText("$ " + price);

                    mHistoryBooking = new HistoryBooking(
                            clienttBooking.getIdHistoryBooking(),
                            clienttBooking.getIdClient(),
                            clienttBooking.getIdDriver(),
                            clienttBooking.getDestination(),
                            clienttBooking.getOrigin(),
                            clienttBooking.getPhoneDestination(),
                            clienttBooking.getPhoneOrigin(),
                            clienttBooking.getPack(),
                            clienttBooking.getTime(),
                            clienttBooking.getKm(),
                            clienttBooking.getStatus(),
                            clienttBooking.getOriginLat(),
                            clienttBooking.getOriginLng(),
                            clienttBooking.getDestinationLat(),
                            clienttBooking.getDestinationLng()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void calification() {
        if (mCalification > 0 ){
            mHistoryBooking.setCalificationClient(mCalification);
            // Para guardar la fecha y la hora
            mHistoryBooking.setTimeStamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        mHistoryBookingProvider.updateCalificationClient(mHistoryBooking.getIdHistoryBooking(),mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this,"La calificación se guardo correctamente",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapDriver.class);
                                intent.putExtra("connect",true);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else {
                        // Ese listener es para ver si se realizo correctamente la calificacion , Se sobreescribe el pedido pero con el id para tener el historial
                        // Si esta informacion fue creada se actualiza, osino se crea
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this,"La calificación se guardo correctamente",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapDriver.class);
                                intent.putExtra("connect",true);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            Toast.makeText(CalificationClientActivity.this, "Debes ingresar la calificación", Toast.LENGTH_SHORT).show();
        }
    }
}