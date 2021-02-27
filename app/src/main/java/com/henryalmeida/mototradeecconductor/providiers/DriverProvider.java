package com.henryalmeida.mototradeecconductor.providiers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henryalmeida.mototradeecconductor.models.Client;
import com.henryalmeida.mototradeecconductor.models.Drivers;

import java.util.HashMap;
import java.util.Map;

public class DriverProvider {
    DatabaseReference mDatabase;

    public DriverProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child("Drivers");
    }

    public Task<Void> create(Drivers driver){

            Map<String,Object> map = new HashMap<>();
            map.put("name",driver.getName());
            map.put("phone",driver.getPhone());
            map.put("VehBrand",driver.getVehiculoBrand());
            map.put("VehPlate",driver.getVehiculePlate());
            map.put("email",driver.getEmail());
            map.put("password",driver.getPassword1());

        return mDatabase.child(driver.getId()).setValue(map);
    }

    public Task<Void> update(Drivers driver){
        Map<String,Object> map = new HashMap<>();
        map.put("VehBrand",driver.getVehiculoBrand());
        map.put("VehPlate",driver.getVehiculePlate());
        map.put("image",driver.getImage());
        return mDatabase.child(driver.getId()).updateChildren(map);
    }

    public DatabaseReference getDriver(String idDriver){
        return mDatabase.child(idDriver);
    }

}
