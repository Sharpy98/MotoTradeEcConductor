package com.henryalmeida.mototradeecconductor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.models.HistoryBooking;
import com.henryalmeida.mototradeecconductor.providiers.ClientProvider;
import com.henryalmeida.mototradeecconductor.providiers.HistoryBookingProvider;
import com.squareup.picasso.Picasso;

public class HistoryBookingDetailDriverActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvOrigin;
    private TextView tvDestination;
    private TextView tvYourCalification;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;


    private String mExtraid;

    private HistoryBookingProvider mHistoryBookingProvider;
    private ClientProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_driver);

        tvName = findViewById(R.id.tvNameBookingDetail);
        tvOrigin = findViewById(R.id.tvOriginClientBookingDetail);
        tvDestination = findViewById(R.id.tvDestinationClientBookingDetail);
        tvYourCalification = findViewById(R.id.tvCalificationClientBookingDetail);
        mRatingBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage= findViewById(R.id.circleImageHistoryBookingDetail);
        mCircleImageBack= findViewById(R.id.circleImageBack);


        // Historial de viaje
        mExtraid = getIntent().getStringExtra("idHistoryBooking");

        mHistoryBookingProvider = new HistoryBookingProvider();
        mClientProvider = new ClientProvider();

        getHistoryBooking();

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    tvOrigin.setText(historyBooking.getOrigin());
                    tvDestination.setText(historyBooking.getDestination());
                    tvYourCalification.setText("Tu calificación es: " + historyBooking.getCalificationDriver());
                    if (snapshot.hasChild("calificationClient")){
                        mRatingBarCalification.setRating((float) historyBooking.getCalificationClient());
                    }
                    // Para obtener el nombre del conductor
                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String name = snapshot.child("name").getValue().toString();
                                tvName.setText(name.toUpperCase());                                // Preguntamos si tiene la imagen
                                if (snapshot.hasChild("image")){
                                    String image = snapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailDriverActivity.this ).load(image).into(mCircleImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}