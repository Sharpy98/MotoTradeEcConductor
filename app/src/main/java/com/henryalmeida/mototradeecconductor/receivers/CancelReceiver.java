package com.henryalmeida.mototradeecconductor.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.henryalmeida.mototradeecconductor.providiers.AuthProvider;
import com.henryalmeida.mototradeecconductor.providiers.ClientBookingProvider;
import com.henryalmeida.mototradeecconductor.providiers.DriversFoundProvider;


public class CancelReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;
    private DriversFoundProvider mDriversFoundProvider;
    private AuthProvider mAuthProvider;

    @Override
    public void onReceive(Context context, Intent intent) {

        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new ClientBookingProvider();
        mDriversFoundProvider = new DriversFoundProvider();
        mAuthProvider = new AuthProvider();

        // En la base de datos cambiamos el estado del pedido
        //mClientBookingProvider.updateStatus(idClient,"cancel");
        mDriversFoundProvider.delete(mAuthProvider.getId());

        // Para que cuando acepte el pedido desaparezca la notificacion
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);


    }
}
