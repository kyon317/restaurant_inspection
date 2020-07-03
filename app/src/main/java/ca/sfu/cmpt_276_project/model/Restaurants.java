package ca.sfu.cmpt_276_project.model;

import java.util.ArrayList;
import java.util.Iterator;

import ca.sfu.cmpt_276_project.R;

public class Restaurants {

    private String name;
    private String streetAddress;
    private String cityAddress;
    private String trackingNum;

    private String latitude;
    private String longitude;

    private int icon;

    private int issuesNum;

    private double mostRecentDate;

    public Restaurants(){

    }

    public Restaurants(String name, String streetAddress, String cityAddress,
                      String trackingNum, String latitude, String longitude,
                      int icon, int criticalViolationCount, double mostRecentDate) {
        this.name = name;
        this.streetAddress = streetAddress;
        this.cityAddress = cityAddress;
        this.trackingNum = trackingNum;
       this.latitude = latitude;
        this.longitude = longitude;
        this.icon = getLogo();
        this.issuesNum = issuesNum;
    }

    public int getLogo() {
        name = this.getName();

        if(name.matches("(A&W).*")){
            return R.drawable.aw;
        }
        else
            return R.drawable.pizza;
    }


    public String getName() {
        return name;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getCityAddress() {
        return cityAddress;
    }

    public String getTrackingNum() {
        return trackingNum;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public int getIssuesNum() {
        return issuesNum;
    }


    public int getIcon() {
        return icon;
    }


    public double getMostRecentDate() {
        return mostRecentDate;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public void setCityAddress(String cityAddress) {
        this.cityAddress = cityAddress;
    }

    public void setTrackingNum(String trackingNum) {
        this.trackingNum = trackingNum;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setIssuesNum(int issuesNum) {
        this.issuesNum = issuesNum;
    }

    public void setMostRecentDate(double mostRecentDate) {
        this.mostRecentDate = mostRecentDate;
    }

}
