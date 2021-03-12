package com.henryalmeida.mototradeecconductor.providiers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henryalmeida.mototradeecconductor.models.ClienttBooking;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingProvider {

    private DatabaseReference mDatabase;

    public ClientBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    public Task<Void> create(ClienttBooking clienttBooking){
        return mDatabase.child(clienttBooking.getIdClient()).setValue(clienttBooking);
    }

    // Metodo que nos permita cambiar el estado del pedido

    public Task<Void> updateStatus(String idClientBooking, String status){

        Map<String,Object> map = new HashMap<>();
        map.put("status",status);
        return  mDatabase.child(idClientBooking).updateChildren(map);
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

    public DatabaseReference getStatus(String idClientBooking) {
        return mDatabase.child(idClientBooking).child("status");
    }

    public DatabaseReference getPack(String idClientBooking) {
        return mDatabase.child(idClientBooking).child("pack");
    }

    public DatabaseReference getClientBooking(String idClientBooking) {
        return mDatabase.child(idClientBooking);
    }

    public Task<Void> delete(String idClientBooking) {
        return mDatabase.child(idClientBooking).removeValue();
    }
}
