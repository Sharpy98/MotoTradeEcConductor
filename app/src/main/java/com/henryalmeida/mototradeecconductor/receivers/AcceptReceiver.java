package com.henryalmeida.mototradeecconductor.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.henryalmeida.mototradeecconductor.activities.MapDriver;
import com.henryalmeida.mototradeecconductor.activities.MapDriverBooking;
import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientBookingProvider;
import com.henryalmeida.mototradeecconductor.providiers.GeofireProvider;

import androidx.annotation.NonNull;


public class AcceptReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Para que cuando acepte el conductor el pedido desaparezca del nodo de condutores activos
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getId());

        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new ClientBookingProvider();

        checkIfClientBookingWasAccept(idClient,context);


        // Para que cuando acepte el pedido desaparezca la notificacion
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);




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
}
