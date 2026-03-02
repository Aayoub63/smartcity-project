package com.example.demo.model;

public class StationData {
    
    private String stationId;
    private int bikesAvailable;
    private int docksAvailable;
    private String name;
    private String address;  
    // Constructeur par defaut (necessaire pour Jackson/Spring)
    public StationData() {
    }
    
    // Constructeur avec tous les champs
    public StationData(String stationId, int bikesAvailable, int docksAvailable, String name) {
        this.stationId = stationId;
        this.bikesAvailable = bikesAvailable;
        this.docksAvailable = docksAvailable;
        this.name = name;
    }
    
    // Getters et Setters
    public String getStationId() {
        return stationId;
    }
    
    public void setStationId(String stationId) {
        this.stationId = stationId;
    }
    
    public int getBikesAvailable() {
        return bikesAvailable;
    }
    
    public void setBikesAvailable(int bikesAvailable) {
        this.bikesAvailable = bikesAvailable;
    }
    
    public int getDocksAvailable() {
        return docksAvailable;
    }
    
    public void setDocksAvailable(int docksAvailable) {
        this.docksAvailable = docksAvailable;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    @Override
    public String toString() {
        return "StationData{" +
                "stationId='" + stationId + '\'' +
                ", name='" + name + '\'' +
                ", bikesAvailable=" + bikesAvailable +
                ", docksAvailable=" + docksAvailable +
                '}';
    }
}