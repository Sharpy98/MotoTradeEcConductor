package com.henryalmeida.mototradeecconductor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.henryalmeida.mototradeecconductor.adapters.HistoryBookingDriverAdapter;
import com.henryalmeida.mototradeecconductor.includes.MyToolbar;
import com.henryalmeida.mototradeecconductor.models.HistoryBooking;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;

public class HistoryBookingDriverActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HistoryBookingDriverAdapter mAdapter;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_driver);

        MyToolbar.show1(HistoryBookingDriverActivity.this,"Historial de viajes",true);

        mRecyclerView = findViewById(R.id.recyclerViewHistoryBooking);
        // Para que nos muestre la tarjeta
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Consulta a la base de datos
        mAuthProvider = new AuthProvider();
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("HistoryBooking")
                .orderByChild("idDriver")
                .equalTo(mAuthProvider.getId());
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query,HistoryBooking.class)
                .build();
        mAdapter = new HistoryBookingDriverAdapter(options,HistoryBookingDriverActivity.this);

        mRecyclerView.setAdapter(mAdapter);
        // Empieze a escuchar los cambios que se realicen en firebase
        mAdapter.startListening();
    }

    // Para cuando la minimice la aplicacion deje de escuhar lo del firebase
    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}