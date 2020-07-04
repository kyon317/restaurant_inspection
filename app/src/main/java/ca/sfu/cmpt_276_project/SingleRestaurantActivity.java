package ca.sfu.cmpt_276_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/*
Shows the details of a single restaurant.Shows restaurant name, address, and coordinates.
Has a scroll-able list that show all it's inspections by recency.
Each inspection on the list should show: #critical issues, #non-critical issues, how long
since inspection, and an icon with hazard level and colour.
 */
class Inspection{
    private int numCritIssues;
    private int numNonCritIssues;
    private String timeSinceInspection;
    private int icon;

    public Inspection(int numCritIssues,
                      int numNonCritIssues,
                      String timeSinceInspection,
                      int icon){
        this.numCritIssues = numCritIssues;
        this.numNonCritIssues = numNonCritIssues;
        this.timeSinceInspection = timeSinceInspection;
        this.icon = icon;
    }

    public int getNumCritIssues() {
        return numCritIssues;
    }

    public void setNumCritIssues(int numCritIssues) {
        this.numCritIssues = numCritIssues;
    }

    public int getNumNonCritIssues() {
        return numNonCritIssues;
    }

    public void setNumNonCritIssues(int numNonCritIssues) {
        this.numNonCritIssues = numNonCritIssues;
    }

    public String getTimeSinceInspection() {
        return timeSinceInspection;
    }

    public void setTimeSinceInspection(String timeSinceInspection) {
        this.timeSinceInspection = timeSinceInspection;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
class Restaurant{
    //temporary restaurant class, will be replaced with model package.
    private String name;
    private int icon;
    private String address;
    private int latitude;
    private int longitude;

    public Restaurant(String name, int icon, String address, int latitude, int longitude){
        this.name = name;
        this.icon = icon;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }
}

public class SingleRestaurantActivity extends AppCompatActivity {
    static Restaurant restaurant = new Restaurant("Denny's",
            0,
            "1234 Main St.",
            50,
            -123
            );
    static List<Inspection> inspections = new ArrayList<Inspection>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_restaurant);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView restaurantName_textview = (TextView)findViewById(R.id.Restaurant_name);
        restaurantName_textview.setText(restaurant.getName());
        TextView addressText_textview = (TextView)findViewById(R.id.addressText);
        addressText_textview.setText(restaurant.getAddress());
        TextView coords_textView = (TextView)findViewById(R.id.coordinatesText);
        coords_textView.setText(restaurant.getLatitude() + ", " + restaurant.getLongitude());
        inspections.add(new Inspection(2,
                1,
                "20 days",
                0));
        inspections.add(new Inspection(0,
                0,
                "1 year",
                0));
    }
    public static Intent makeIntent(Context context) {
        return new Intent(context, SingleRestaurantActivity.class);
    }
}