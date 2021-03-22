package com.henryalmeida.mototradeecconductor.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.henryalmeida.mototradeecconductor.HistoryBookingDriverActivity;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.adapters.HistoryBookingDriverAdapter;
import com.henryalmeida.mototradeecconductor.adapters.StackOrderAdapter;
import com.henryalmeida.mototradeecconductor.includes.MyToolbar;
import com.henryalmeida.mototradeecconductor.models.ClienttBooking;
import com.henryalmeida.mototradeecconductor.models.HistoryBooking;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;

public class StackOrderActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private StackOrderAdapter mAdapter;

    private CircleImageView mCircleImageBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack_order);

        mRecyclerView = findViewById(R.id.recyclerViewStackOrder);
        mCircleImageBack= findViewById(R.id.circleImageBack);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StackOrderActivity.this,MapDriver.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("ClientBooking")
                .orderByChild("status")
                .equalTo("create");
        FirebaseRecyclerOptions<ClienttBooking> options = new FirebaseRecyclerOptions.Builder<ClienttBooking>()
                .setQuery(query,ClienttBooking.class)
                .build();
        mAdapter = new StackOrderAdapter(options,StackOrderActivity.this);

        mRecyclerView.setAdapter(mAdapter);
        // Empieze a escuchar los cambios que se realicen en firebase
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}