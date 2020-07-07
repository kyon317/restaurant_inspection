package ca.sfu.cmpt_276_project.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.R;
import ca.sfu.cmpt_276_project.TestingActivity;

public class MainActivity extends AppCompatActivity {

    private List<DummyRestaurants> surreyRestaurants = new ArrayList<DummyRestaurants>();//dummy var list for UI
    private RestaurantManager restaurantManager;//actual list you wanna use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Surrey Restaurant Inspections");

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#31b1c4"));

        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        
        populateRestaurantList();
        populateListView();
        registerClickCallback();
        restaurantManager = RestaurantManager.getInstance();
        initializeRestaurantList();//method necessary to initialize instance
        //launchTestingActivity();
    }

    public void initializeRestaurantList(){
        //get Restaurants from CSV
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
        List<Restaurant> restaurantList = new ArrayList<>();

        try {
            restaurantImport.readRestaurantList(this);
            restaurantList = restaurantImport.getRestaurantList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get Inspection Data of Restaurants from CSV
        InspectionDataCSVIngester inspectionDataImport = new InspectionDataCSVIngester();
        try {
            inspectionDataImport.readInspectionData(this);
            //Sort inspection data into proper Restaurant objects
            if (!restaurantList.isEmpty()) {
                for (Restaurant restaurant : restaurantList) {
                    restaurant.setInspectionDataList(inspectionDataImport.returnInspectionByID
                            (restaurant.getTrackNumber()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Update existing Restaurant Manager obj instance
        restaurantManager.setRestaurants(restaurantList);

        /* Debugging Pretty Printer, uncomment to test further
        if (!restaurantManager.getRestaurants().isEmpty()) {
            int inspection_count = 0;
            for (Restaurant restaurant : restaurantManager.getRestaurants()) {
                restaurant.Display();
                //System.out.println(restaurant.getRestaurantName()); sort check
                inspection_count += restaurant.getInspectionDataList().size();
            }
            System.out.println("Restaurant Count: "+restaurantList.size());
            System.out.println("Inspection Count: "+inspection_count);
        }*/
    }

    /**
     * Vincent testing code below
     */
    public void makeDummyChanges(){
        restaurantManager.getRestaurants().remove(0);
    }
    public void launchTestingActivity(){
        makeDummyChanges();         //make changes on instance to test data consistency
        Intent intent = new Intent(this, TestingActivity.class);
        startActivity(intent);
    }
    //end of testing code


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        populateListView();
        registerClickCallback();
    }

    // add dummy restaurants data
    private void populateRestaurantList() {
        surreyRestaurants.add(new DummyRestaurants("A&W Restaurant", R.drawable.aw, R.drawable.hazardlow, "Low", 2, "Mar 20"));
        surreyRestaurants.add(new DummyRestaurants("Lee Yuen Restaurant", R.drawable.dimsum, R.drawable.hazardyellow, "Moderate",1, "Dec 2018"));
        surreyRestaurants.add(new DummyRestaurants("Pizza Hut Restaurant", R.drawable.pizza, R.drawable.hazardhigh,"High",3, "20 days"));

    }

    private void populateListView() {
        ArrayAdapter<DummyRestaurants> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);
    }


    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                DummyRestaurants clickedRestaurant = surreyRestaurants.get(position);

                // Launch dummy restaurant details menu
                Intent  intent = SingleRestaurantActivity.makeIntent(MainActivity.this);
                startActivity(intent);
            }
        });
    }


    private class MyListAdapter extends ArrayAdapter<DummyRestaurants> {

        public MyListAdapter() {
            super(MainActivity.this, R.layout.restaurants_view, surreyRestaurants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View restaurantView = convertView;
            if(restaurantView == null){
                restaurantView = getLayoutInflater().inflate(R.layout.restaurants_view, parent, false);

            }

            DummyRestaurants currentRestaurant = surreyRestaurants.get(position);

            // Fill restaurant image
            ImageView resImageView = (ImageView)restaurantView.findViewById(R.id.restaurant_icon);
            resImageView.setImageResource(currentRestaurant.getIcon());

            // Fill hazard icon
            ImageView hazardIconView = (ImageView)restaurantView.findViewById(R.id.restaurant_hazardicon);
            hazardIconView.setImageResource(currentRestaurant.getHazardIcon());

            //Fill hazard level with color
            TextView hazardLevelView = (TextView)restaurantView.findViewById(R.id.hazard_level);

            if(currentRestaurant.getHazard().equals("Low")){
                hazardLevelView.setTextColor(Color.rgb(37, 148, 55));
            }
            else if(currentRestaurant.getHazard().equals("Moderate")){
                hazardLevelView.setTextColor(Color.MAGENTA);
            }
            else{
                hazardLevelView.setTextColor((Color.RED));
            }

            hazardLevelView.setText(currentRestaurant.getHazard());

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