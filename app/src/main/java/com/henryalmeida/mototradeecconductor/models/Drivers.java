package com.henryalmeida.mototradeecconductor.models;

public class Drivers {
    String id,
            name,
            phone,
            email,
            password1,
    vehiculoBrand,
    vehiculePlate;
    String image;

    public Drivers() {
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getVehiculoBrand() {
        return vehiculoBrand;
    }

    public void setVehiculoBrand(String vehiculoBrand) {
        this.vehiculoBrand = vehiculoBrand;
    }

    public String getVehiculePlate() {
        return vehiculePlate;
    }

    public void setVehiculePlate(String vehiculePlate) {
        this.vehiculePlate = vehiculePlate;
    }

    public Drivers(String id, String name, String phone, String email, String password1, String vehiculoBrand, String vehiculePlate) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password1 = password1;
        this.vehiculoBrand = vehiculoBrand;
        this.vehiculePlate = vehiculePlate;
    }
}
