/*
* Class: Restaurant
*
* Data descriptions:
* 1. trackNumber: An integer indicates its unique tracking number
* 2. restaurantName: A String Data type shows the name of the restaurant
* 3. physicalAddress: A String Data type shows the physical address of the restaurant
* 4. physicalCity: A String Data type shows the physical city of the restaurant
* 5. facType: A String Data type shows the type of the restaurant
* 6. latitude: A double Data type shows the latitude of the restaurant
* 7. longitude: A double Data type shows the longitude of the restaurant
* 8. data: An Inspection Data type, which stores all the details of a restaurant object
*
* Functions:
* 1. Getters
* 2. Setters
* 3. Default & Non-Default Constructor
* 4. Display
*TRACKINGNUMBER,NAME,PHYSICALADDRESS,PHYSICALCITY,FACTYPE,LATITUDE,LONGITUDE
* */
package ca.sfu.cmpt_276_project.Model;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private String trackNumber;
    private String restaurantName;
    private String physicalAddress;
    private String physicalCity;
    private String facType;
    private double latitude;
    private double longitude;
    private List<InspectionData> inspectionDataList = new ArrayList<>();

    //Getters
    public String getTrackNumber() {
        return trackNumber;
    }
    public List<InspectionData> getInspectionDataList() {
        return inspectionDataList;
    }
    public String getRestaurantName() {
        return restaurantName;
    }
    public String getPhysicalAddress() {
        return physicalAddress;
    }
    public String getPhysicalCity() {
        return physicalCity;
    }
    public String getFacType() {
        return facType;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }

    //Setters
    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }
    public void setInspectionDataList(List<InspectionData> inspectionDataList) {
        this.inspectionDataList = inspectionDataList;
    }
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    public void setPhysicalAddress(String physicalAddress) {
        this.physicalAddress = physicalAddress;
    }
    public void setPhysicalCity(String physicalCity) {
        this.physicalCity = physicalCity;
    }
    public void setFacType(String facType) {
        this.facType = facType;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    //Default Constructor
    public Restaurant() {
        this.trackNumber = null;
        this.restaurantName = null;
        this.physicalAddress = null;
        this.physicalCity = null;
        this.facType = null;
        this.latitude = 0;
        this.longitude = 0;
    }

    //Non-Default Constructor
    public Restaurant(String trackNumber, String restaurantName, String physicalAddress,
                      String physicalCity, String facType, double latitude, double longitude,
                      List<InspectionData> inspectionDataList) {
        this.trackNumber = trackNumber;
        this.restaurantName = restaurantName;
        this.physicalAddress = physicalAddress;
        this.physicalCity = physicalCity;
        this.facType = facType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.inspectionDataList = inspectionDataList;
    }

    //Display
    public void Display(){
        System.out.println
                ("\n--------------------------------------------------------\nRestaurant Tracking Number: "+this.getTrackNumber()
                +"\nRestaurant Name: "+this.getRestaurantName()
                +"\nPhysical Address: "+this.getPhysicalAddress()
                +"\nPhysical City: "+this.getPhysicalCity()
                +"\nFac Type: "+this.getFacType()
                +"\nLatitude: "+this.getLatitude()
                +"\nLongitude: "+this.getLongitude()
                +"\nInspection Data: \n=========================================================\n");
        for(InspectionData inspectionData : this.inspectionDataList){
            inspectionData.Display();
        }
    }
}
