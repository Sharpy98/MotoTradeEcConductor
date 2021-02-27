package com.henryalmeida.mototradeecconductor.models;

public class ClienttBooking {

    String idHistoryBooking;
    String idClient;
    String idDriver;
    String destination;
    String origin;
    String phoneDestination;
    String phoneOrigin;
    String pack;
    String time;
    String km;
    String status;
    double originLat;
    double originLng;
    double DestinationLat;
    double DestinationLng;
    double Price;

    public ClienttBooking(){

    }

    public ClienttBooking(String idHistoryBooking, String idClient, String idDriver, String destination, String origin, String phoneDestination, String phoneOrigin, String pack, String time, String km, String status, double originLat, double originLng, double destinationLat, double destinationLng, double price) {
        this.idHistoryBooking = idHistoryBooking;
        this.idClient = idClient;
        this.idDriver = idDriver;
        this.destination = destination;
        this.origin = origin;
        this.phoneDestination = phoneDestination;
        this.phoneOrigin = phoneOrigin;
        this.pack = pack;
        this.time = time;
        this.km = km;
        this.status = status;
        this.originLat = originLat;
        this.originLng = originLng;
        DestinationLat = destinationLat;
        DestinationLng = destinationLng;
        Price = price;
    }

    public ClienttBooking(String idClient, String idDriver, String destination, String origin, String phoneDestination, String phoneOrigin, String pack, String time, String km, String status, double originLat, double originLng, double destinationLat, double destinationLng) {
        this.idClient = idClient;
        this.idDriver = idDriver;
        this.destination = destination;
        this.origin = origin;
        this.phoneDestination = phoneDestination;
        this.phoneOrigin = phoneOrigin;
        this.pack = pack;
        this.time = time;
        this.km = km;
        this.status = status;
        this.originLat = originLat;
        this.originLng = originLng;
        DestinationLat = destinationLat;
        DestinationLng = destinationLng;
    }

    public ClienttBooking(String idHistoryBooking, String idClient, String idDriver, String destination, String origin, String phoneDestination, String phoneOrigin, String pack, String time, String km, String status, double originLat, double originLng, double destinationLat, double destinationLng) {

        this.idHistoryBooking = idHistoryBooking;
        this.idClient = idClient;
        this.idDriver = idDriver;
        this.destination = destination;
        this.origin = origin;
        this.phoneDestination = phoneDestination;
        this.phoneOrigin = phoneOrigin;
        this.pack = pack;
        this.time = time;
        this.km = km;
        this.status = status;
        this.originLat = originLat;
        this.originLng = originLng;
        DestinationLat = destinationLat;
        DestinationLng = destinationLng;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getIdDriver() {
        return idDriver;
    }

    public void setIdDriver(String idDriver) {
        this.idDriver = idDriver;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getPhoneDestination() {
        return phoneDestination;
    }

    public void setPhoneDestination(String phoneDestination) {
        this.phoneDestination = phoneDestination;
    }

    public String getPhoneOrigin() {
        return phoneOrigin;
    }

    public void setPhoneOrigin(String phoneOrigin) {
        this.phoneOrigin = phoneOrigin;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public double getOriginLng() {
        return originLng;
    }

    public void setOriginLng(double originLng) {
        this.originLng = originLng;
    }

    public double getDestinationLat() {
        return DestinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        DestinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return DestinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        DestinationLng = destinationLng;
    }

    public String getIdHistoryBooking() {
        return idHistoryBooking;
    }

    public void setIdHistoryBooking(String idHistoryBooking) {
        this.idHistoryBooking = idHistoryBooking;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }
}
