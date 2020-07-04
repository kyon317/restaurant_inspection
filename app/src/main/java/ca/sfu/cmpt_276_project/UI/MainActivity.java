package ca.sfu.cmpt_276_project.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.R;

public class MainActivity extends AppCompatActivity {

    private List<Restaurants> surreyRestaurants = new ArrayList<Restaurants>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        populateRestaurantList();
        populateListView();
        registerClickCallback();
    }

    // add dummy restaurants data
    private void populateRestaurantList() {
        surreyRestaurants.add(new Restaurants("A&W Restaurant", R.drawable.aw, R.drawable.hazardlow, 2, "Mar 20"));
        surreyRestaurants.add(new Restaurants("Lee Yuen Restaurant", R.drawable.dimsum, R.drawable.hazardyellow,1, "Dec 2018"));
        surreyRestaurants.add(new Restaurants("Pizza Hut Restaurant", R.drawable.pizza, R.drawable.hazardhigh,3, "20 days"));

    }

    private void populateListView() {
        ArrayAdapter<Restaurants> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);
    }


    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Restaurants clickedRestaurant = surreyRestaurants.get(position);

                // Launch dummy restaurant details menu
                Intent  intent = new Intent(MainActivity.this, RestaurantDetails.class);
                startActivity(intent);
            }
        });
    }


    private class MyListAdapter extends ArrayAdapter<Restaurants> {

        public MyListAdapter() {
            super(MainActivity.this,R.layout.restaurants_view, surreyRestaurants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View restaurantView = convertView;
            if(restaurantView == null){
                restaurantView = getLayoutInflater().inflate(R.layout.restaurants_view, parent, false);

            }

            Restaurants currentRestaurant = surreyRestaurants.get(position);

            // Fill restaurant image
            ImageView resImageView = (ImageView)restaurantView.findViewById(R.id.restaurant_icon);
            resImageView.setImageResource(currentRestaurant.getIcon());

            // Fill restaurant image
            ImageView hazardIconView = (ImageView)restaurantView.findViewById(R.id.restaurant_hazardicon);
            hazardIconView.setImageResource(currentRestaurant.getHazardIcon());

            // Fill name
            TextView nameText = (TextView)restaurantView.findViewById(R.id.restaurant_txtName);
            nameText.setText(currentRestaurant.getName());

            // Fill inspection date
            TextView dateText = (TextView)restaurantView.findViewById(R.id.restaurant_txtDate);
            dateText.setText(currentRestaurant.getMostRecentDate());

            // Fill # issues
            TextView numIssuesText = (TextView)restaurantView.findViewById(R.id.restaurant_txtIssues);
            numIssuesText.setText("" + currentRestaurant.getCriticalViolationCount());

            return restaurantView;
        }
    }
}