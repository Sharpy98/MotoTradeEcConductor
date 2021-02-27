package com.henryalmeida.mototradeecconductor.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.activities.HistoryBookingDetailDriverActivity;
import com.henryalmeida.mototradeecconductor.models.HistoryBooking;
import com.henryalmeida.mototradeecconductor.providiers.ClientProvider;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryBookingDriverAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingDriverAdapter.ViewHolder> {

    private ClientProvider mClientProvider;
    private Context mContext;

    public HistoryBookingDriverAdapter(FirebaseRecyclerOptions<HistoryBooking>options, Context context){
        super(options);

        mClientProvider = new ClientProvider();
        mContext= context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull HistoryBooking historyBooking) {
        // Establecer los valores que establecimos en las tarjeta
        // Holder nos permite ver cualquier campo establecido

        final String id = getRef(position).getKey();

        // Historial de viajes de un cliente
        holder.tvOrigin.setText(historyBooking.getOrigin());
        holder.tvDestination.setText(historyBooking.getDestination());
        holder.tvCalification.setText(String.valueOf(historyBooking.getCalificationDriver()));

        // Para obtener el nombre del conductor
        mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    holder.tvName.setText(name);
                    // Preguntamos si tiene la imagen
                    if (snapshot.hasChild("image")){
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.with(mContext).load(image).into(holder.imageViewHistoryBooking);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // hacer el click en la tarjeta
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, HistoryBookingDetailDriverActivity.class);
                intent.putExtra("idHistoryBooking",id);
                mContext.startActivity(intent);
            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Se instancia el layout que se va a utilizar

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView; // Para poder hacer click y ver el detalle del pedido haciendo click en cualquier parte
        private TextView tvName;
        private TextView tvOrigin;
        private TextView tvDestination;
        private TextView tvCalification;
        private ImageView imageViewHistoryBooking;

        public ViewHolder(View view){
            super(view);
            // Se va a instanciar cada vista que tenemos en la tarjeta

            mView = view;
            tvName = view.findViewById(R.id.tvNameUser);
            tvOrigin = view.findViewById(R.id.tvOrigin);
            tvDestination = view.findViewById(R.id.tvDestination);
            tvCalification = view.findViewById(R.id.tvCalification);
            imageViewHistoryBooking = view.findViewById(R.id.imageViewHistoryBooking);
        }
    }
}
