package com.henryalmeida.mototradeecconductor.providiers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henryalmeida.mototradeecconductor.models.Client;

import java.util.HashMap;
import java.util.Map;

public class ClientProvider {
    DatabaseReference mDatabase;

    public ClientProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child("Clients");
    }
    
    public Task<Void> create(Client client){
        Map<String,Object> map = new HashMap<>();
        map.put("name",client.getName());
        map.put("phone",client.getPhone());
        map.put("email",client.getEmail());
        map.put("password",client.getPassword1());
        return mDatabase.child(client.getId()).setValue(map);
    }

    public DatabaseReference getClient(String idClient){
        return mDatabase.child(idClient);
    }
}
