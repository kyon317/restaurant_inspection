/*
* Class: Restaurant
*
* Data descriptions:
* 1. trackNumber: An integer indicates its unique tracking number
* 2. data: An Inspection Data type, which stores all the details of a restaurant object
*
* Functions:
* 1. Getters
* 2. Setters
* 3. Default & Non-Default Constructor
* 4. Display
*
* */
package ca.sfu.cmpt_276_project.Model;

public class Restaurant {
    private int trackNumber;
    private InspectionData data;

    //Getters
    public int getTrackNumber() {
        return trackNumber;
    }
    public InspectionData getData() {
        return data;
    }

    //Setters
    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }
    public void setData(InspectionData data) {
        this.data = data;
    }

    //Default Constructor
    public Restaurant() {
        this.trackNumber = 0;
        this.data = null;
    }

    //Non-Default Constructor
    public Restaurant(int trackNumber, InspectionData data) {
        this.trackNumber = trackNumber;
        this.data = data;
    }

    //Display
    public void Display(){
        System.out.println("Tracking Number: "+this.getTrackNumber()
                +"\nInspection Data: ");
        this.getData().Display();
    }
}
