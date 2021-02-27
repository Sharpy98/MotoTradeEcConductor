package com.henryalmeida.mototradeecconductor.models;

public class Client {
    String id,
            name,
            phone,
            email,
            password1;

    public Client() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword1() {
        return password1;
    }

    public Client(String id, String name, String phone, String email, String password1) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password1 = password1;
    }
}
