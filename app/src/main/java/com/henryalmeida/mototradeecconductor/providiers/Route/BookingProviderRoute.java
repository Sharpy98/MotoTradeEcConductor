package com.henryalmeida.mototradeecconductor.providiers.Route;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henryalmeida.mototradeecconductor.models.route.BookingRoute;

import java.util.HashMap;
import java.util.Map;

public class BookingProviderRoute {

    private DatabaseReference mDatabase;

    public BookingProviderRoute() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBookingRoute");
    }

    public Task<Void> create(BookingRoute bookingRoute){
        return mDatabase.child(bookingRoute.getIdClient()).setValue(bookingRoute);
    }

    // Metodo que nos permita cambiar el estado del pedido

    public Task<Void> updateStatus(String idClientBooking, String status){

        Map<String,Object> map = new HashMap<>();
        map.put("status",status);
        return  mDatabase.child(idClientBooking).updateChildren(map);
    }

    public DatabaseReference getStatus(String idClientBooking){
        return mDatabase.child(idClientBooking).child("status");
    }

    public DatabaseReference getClientBooking(String idClientBooking) {
        return mDatabase.child(idClientBooking);
    }

    public Task<Void> updateStatusAndIdDriver(String idClientBooking, String status, String idDriver) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("idDriver", idDriver);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    // Para crear un id al pedido
    public Task<Void> updateIdHistoryBooking(String idClientBooking) {
        // Para crear un identificador unico
        String idPush = mDatabase.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idHistoryBooking", idPush);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    public Task<Void> updatePrice(String idClientBooking, double price) {
        Map<String, Object> map = new HashMap<>();
        map.put("price", price);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    // Para eliminar un pedido cancelado
    public Task<Void> delete(String idClientBooking) {
        return mDatabase.child(idClientBooking).removeValue();
    }

}
