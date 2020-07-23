/*
 * Activity: Main Activity
 *
 * Activity description: Give a list of restaurant and brief information.
 *
 * */
package ca.sfu.cmpt_276_project.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;

public class RestaurantListActivity extends AppCompatActivity {

    private RestaurantManager restaurantManager;
    private int[] restaurantIcons;
    private List<Restaurant> restaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        getSupportActionBar().setTitle("Surrey Restaurant Inspections");

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#31b1c4"));

        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);


        restaurantManager = RestaurantManager.getInstance();
        //initializeRestaurantList();//method necessary to initialize instance

        populateRestaurantIcons();
        populateListView();
        registerClickCallback();

        init();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.finishAffinity();
        super.onBackPressed();
    }

    @Override
    public void onDestroy()
    {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    private void populateRestaurantIcons() {
        restaurantIcons = new int[8];
        restaurantIcons[0] = R.drawable.icon_sushi;
        restaurantIcons[1] = R.drawable.icon_dimsum;
        restaurantIcons[2] = R.drawable.icon_dimsum;
        restaurantIcons[3] = R.drawable.icon_aw;
        restaurantIcons[4] = R.drawable.icon_beer;
        restaurantIcons[5] = R.drawable.icon_pizza;
        restaurantIcons[6] = R.drawable.icon_pizza;
        restaurantIcons[7] = R.drawable.icon_chicken;
    }

    // start Maps activity
    private void init(){
        Button btnMaps = (Button) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = MapsActivity.makeIntent(RestaurantListActivity.this);
                startActivity(intent);
            }
        });
    }

    // allows MainActivity to be accessed
    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, RestaurantListActivity.class);
        return intent;
    }

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

    private void populateListView() {
        restaurants = restaurantManager.getRestaurants();
        ArrayAdapter<Restaurant> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);
    }


    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Restaurant clickedRestaurant = restaurantManager.getRestaurantByID(position);

                // pass clicked restaurant's position to SingleRestaurantActivity
                Intent intent = SingleRestaurantActivity.makeIntent(RestaurantListActivity.this, position, false);
                startActivity(intent);
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Restaurant> {

        public MyListAdapter() {
            super(RestaurantListActivity.this, R.layout.restaurants_view, restaurants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View restaurantView = convertView;
            if(restaurantView == null){
                restaurantView = getLayoutInflater().inflate(R.layout.restaurants_view, parent, false);

            }

            Restaurant currentRestaurant = restaurantManager.getRestaurantByID(position);

            // Fill restaurant image
            ImageView resImageView = (ImageView)restaurantView.findViewById(R.id.restaurant_icon);
            currentRestaurant.setIcon(restaurantIcons[(position % 8)]);
            resImageView.setImageResource(currentRestaurant.getIcon());

            // Fill hazard icon
            ImageView hazardIconView = (ImageView)restaurantView.findViewById(
                    R.id.restaurant_hazardicon);


            //Fill hazard level with color
            TextView hazardLevelView = (TextView)restaurantView.findViewById(R.id.hazard_level);
            if(currentRestaurant.getInspectionDataList().isEmpty()){
                hazardLevelView.setText("None");
            }
            else{
                Hazard hazard = currentRestaurant.getInspectionDataList().get(0).getHazard();
                if(hazard == Hazard.LOW){
                    hazardLevelView.setTextColor(Color.rgb(37, 148, 55));
                    hazardIconView.setImageResource(R.drawable.hazardlow);
                    hazardLevelView.setText("LOW");
                }
                else if(hazard == Hazard.MEDIUM){
                    hazardLevelView.setTextColor(Color.MAGENTA);
                    hazardIconView.setImageResource(R.drawable.hazardyellow);
                    hazardLevelView.setText("MEDIUM");
                }
                else{
                    hazardLevelView.setTextColor((Color.RED));
                    hazardIconView.setImageResource(R.drawable.hazardhigh);
                    hazardLevelView.setText("HIGH");
                }


            }

            // Fill name
            TextView nameText = (TextView)restaurantView.findViewById(R.id.restaurant_txtName);
            nameText.setText(currentRestaurant.getRestaurantName());

            //Fill the most recent inspection date
            TextView dateText = (TextView)restaurantView.findViewById(R.id.restaurant_txtDate);

            if(currentRestaurant.getInspectionDataList().isEmpty()){
                dateText.setText("");
            }
            else{
                // Get the most recent inspection date
                Date recentInspectDate = currentRestaurant.getInspectionDataList().get(0).getInspectionDate();

                long date = currentRestaurant.getInspectionDataList().get(0).timeSinceInspection();
                if(date < 30){
                    dateText.setText(String.valueOf(date));
                }
                else if (date < 365){
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd");
                    String strDate = formatter.format(recentInspectDate);
                    dateText.setText(strDate);
                }
                else{
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM YYYY");
                    String strDate = formatter.format(recentInspectDate);
                    dateText.setText(strDate);
                }

            }

            // Fill # issues
            TextView numIssuesText = (TextView)restaurantView.findViewById(R.id.restaurant_txtIssues);

            if(currentRestaurant.getInspectionDataList().isEmpty()){
                numIssuesText.setText("");
            }
            else {
                int currentCritical = currentRestaurant.getInspectionDataList().get(0).getCriticalViolations();
                int currentNonCritical = currentRestaurant.getInspectionDataList().get(0).getNonCriticalViolations();
                int numIssues = currentCritical + currentNonCritical;

                numIssuesText.setText("" + numIssues);
            }

            return restaurantView;
        }
    }
}