package com.henryalmeida.mototradeecconductor.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.henryalmeida.mototradeecconductor.R;
import com.henryalmeida.mototradeecconductor.activities.HistoryBookingDetailDriverActivity;
import com.henryalmeida.mototradeecconductor.activities.NotificationBookingActivity;
import com.henryalmeida.mototradeecconductor.models.ClienttBooking;
import com.henryalmeida.mototradeecconductor.providiers.ClientProvider;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StackOrderAdapter extends FirebaseRecyclerAdapter<ClienttBooking, StackOrderAdapter.ViewHolder> {

    private ClienttBooking mClientBoking;
    private Context mContext;
    private ClientProvider mClientProvider;

    public StackOrderAdapter(FirebaseRecyclerOptions<ClienttBooking> options, Context context) {
        super(options);

        mClientBoking = new ClienttBooking();
        mContext = context;
        mClientProvider = new ClientProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull StackOrderAdapter.ViewHolder holder, int position, @NonNull ClienttBooking clientBooking) {
        // Establecer los valores que establecimos en las tarjeta
        // Holder nos permite ver cualquier campo establecido

        final String id = getRef(position).getKey();
        String idClient = clientBooking.getIdClient();
        holder.tvOrigin.setText(clientBooking.getOrigin());
        holder.tvDestination.setText(clientBooking.getDestination());
        holder.tvTypeOrder.setText("Express");
        mClientProvider.getClient(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    holder.tvName.setText(name);
                    // Preguntamos si tiene la imagen
                    if (snapshot.hasChild("image")){
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.with(mContext).load(image).into(holder.imageClient);
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
                Intent intent = new Intent(mContext, NotificationBookingActivity.class);
                intent.putExtra("idClient", idClient);
                intent.putExtra("origin", clientBooking.getOrigin());
                intent.putExtra("destination", clientBooking.getDestination());
                intent.putExtra("min", "0");
                intent.putExtra("distance", clientBooking.getKm());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(intent);
                Toast.makeText(mContext, "Funciono brohter", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public StackOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Se instancia el layout que se va a utilizar

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_stack_order,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder  extends RecyclerView.ViewHolder {

        private View mView; // Para poder hacer click y ver el detalle del pedido haciendo click en cualquier parte
        private TextView tvName;
        private TextView tvOrigin;
        private TextView tvDestination;
        private TextView tvPrice;
        private TextView tvTypeOrder;
        private ImageView imageClient;

        public ViewHolder(@NonNull View view) {
            super(view);
            // Se va a instanciar cada vista que tenemos en la tarjeta

            mView = view;
            tvName = view.findViewById(R.id.tvNameUser);
            tvOrigin = view.findViewById(R.id.tvOrigin);
            tvDestination = view.findViewById(R.id.tvDestination);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvTypeOrder = view.findViewById(R.id.tvTypeOrder);
            imageClient = view.findViewById(R.id.imageClient);
        }
    }
}
