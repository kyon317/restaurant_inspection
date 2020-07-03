package ca.sfu.cmpt_276_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

/*
Shows the details of a single restaurant.Shows restaurant name, address, and coordinates.
Has a scroll-able list that show all it's inspections by recency.
Each inspection on the list should show: #critical issues, #non-critical issues, how long
since inspection, and an icon with hazard level and colour.
 */
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

    }
    public static Intent makeIntent(Context context) {
        return new Intent(context, SingleRestaurantActivity.class);
    }
}