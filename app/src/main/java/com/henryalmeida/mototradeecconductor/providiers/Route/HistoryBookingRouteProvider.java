package com.henryalmeida.mototradeecconductor.providiers.Route;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henryalmeida.mototradeecconductor.models.HistoryBooking;
import com.henryalmeida.mototradeecconductor.models.route.HistoryBookingRoute;

import java.util.HashMap;
import java.util.Map;

public class HistoryBookingRouteProvider {
    private DatabaseReference mDatabase;

    public HistoryBookingRouteProvider() {
        // Para que en Firebase se cree un nuevo nodo
        mDatabase = FirebaseDatabase.getInstance().getReference().child("HistoryBookingRoute");
    }

    public Task<Void> create(HistoryBookingRoute historyBookingRoute){
        // Me va a crear un nodo con el idHistory con el metodo historyBooking.getIdHistoyBooking
        return mDatabase.child(historyBookingRoute.getIdHistoryBooking()).setValue(historyBookingRoute);
    }

    public Task<Void> updateCalificationClient(String idHistoryBooking, float calificationClient){
        Map<String, Object> map = new HashMap<>();
        map.put("calificationClient",calificationClient);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    public Task<Void> updateCalificationDriver(String idHistoryBooking, float calificationClient){
        Map<String, Object> map = new HashMap<>();
        map.put("calificationDriver",calificationClient);
        return mDatabase.child(idHistoryBooking).updateChildren(map);
    }

    // Metodo para saber si se creo o no
    public DatabaseReference getHistoryBooking(String idHistoryBooking){
        return mDatabase.child(idHistoryBooking);
    }
}
