/*
Receives a restaurant from an instance of a restaurant manager from MapsActivity or
RestaurantListActivity. Displays the name, address coordinates of the given restaurant and displays
a listview of the inspections the restaurant has had, based on recency. The inspections are given a
color and icon based on the hazard rating, and have a different format to display the time since
inspection based if the date of the inspection was withing 30 days, 1 year, or longer. If an
inspection is selected from the list an intent to launch Inspection_Details_Activity will happen.
Hitting the android back button or the action bar back navigation button will take the user to the
previous screen (MapsActivity or RestaurantListActivity). Selecting the coordinates will take the
user to the location of the restaurant on the MapActivity with the info of the restaurant displayed.
 */
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

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.DBAdapter;
import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;


public class SingleRestaurantActivity extends AppCompatActivity {

    private List<InspectionData> inspections = new ArrayList<>();
    private RestaurantManager restaurantManager;

    private int restaurantPosition;
    private String trackNum;
    private Restaurant restaurant;
    private Boolean fromMap;
    private DBAdapter dbAdapter;

    private static final String EXTRA_RES_NUM = "ca.sfu.cmpt_276_project.UI.extraResNum";
    private static final String EXTRA_BOOL_FROM = "ca.sfu.cmpt_276_project.UI.exraBoolFrom";
    private static final String EXTRA_RES_TRACK_NUM = "extraTrackNum";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_restaurant);

        getSupportActionBar().setTitle(R.string.surreyTitle);

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#31b1c4"));

        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        fromMap = intent.getBooleanExtra(EXTRA_BOOL_FROM, true);

        restaurantPosition = intent.getIntExtra(EXTRA_RES_NUM, 0);
        trackNum = intent.getStringExtra(EXTRA_RES_TRACK_NUM);

        //give the view the restaurant info
        restaurantManager = RestaurantManager.getInstance();
        restaurant = restaurantManager.getRestaurantByTrackingNumber(trackNum);
        inspections = restaurant.getInspectionDataList();
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        populatateView();
        populateInspectionsList();
        registerOnClick();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(!fromMap){
            Intent intent = RestaurantListActivity.makeIntent(SingleRestaurantActivity.this);
            startActivity(intent);
        }
        else {
            Intent intent = MapsActivity.makeIntent(SingleRestaurantActivity.this);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void populatateView() {
        TextView favouriteText = findViewById(R.id.btnAddFav);
        if(dbAdapter.checkRestaurant(restaurantPosition)){
            favouriteText.setText(R.string.unfavourite);
        }
        Button favBtn = (Button) findViewById(R.id.btnAddFav);
        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(restaurant.getFavourite()){
                    dbAdapter.deleteRestaurant(restaurantPosition);
                    dbAdapter.print();
                    restaurant.setFavourite(false);
                    favouriteText.setText(R.string.favourite);
                }
                else {
                    dbAdapter.addRestaurant(restaurant, restaurantPosition);
                    dbAdapter.print();
                    restaurant.setFavourite(true);
                    favouriteText.setText(R.string.unfavourite);
                }
            }
        });
        TextView restaurantName_textview = findViewById(R.id.Restaurant_name);
        restaurantName_textview.setText(restaurant.getRestaurantName());
        TextView addressText_textview = findViewById(R.id.addressText);
        addressText_textview.setText(restaurant.getPhysicalAddress()
                +" " + restaurant.getPhysicalCity());
        TextView coords_textView = findViewById(R.id.coordinatesText);
        coords_textView.setText(restaurant.getLatitude() + ", " + restaurant.getLongitude());
        coords_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = MapsActivity.makeIntent(SingleRestaurantActivity.this,
                        restaurant.getRestaurantName(),
                       restaurant.getLatitude(),
                        restaurant.getLongitude(),
                        true);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        populatateView();
        populateInspectionsList();
        registerOnClick();
    }


    private void registerOnClick() {
        ListView list = findViewById(R.id.inspectionsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // pass positions of clicked inspection and current restaurant to Inspection_Details_Activity
                Intent intent = Inspection_Details_Activity.makeIntent(
                        SingleRestaurantActivity.this, position, restaurantPosition);
                startActivity(intent);

            }
        });
    }
    //creates a ListView and an ArrayAdapter to fill inspectionsListView
    private void populateInspectionsList() {
        ArrayAdapter<InspectionData> adapter = new MyListAdapter();
        ListView list = findViewById(R.id.inspectionsListView);
        list.setAdapter(adapter);
    }

    //allows SingleRestaurantActivity to be accessed.
    public static Intent makeIntent(Context context, int restaurantPosition, boolean fromMap,String trackNum) {
        Intent intent =  new Intent(context, SingleRestaurantActivity.class);
        intent.putExtra(EXTRA_RES_NUM, restaurantPosition);
        intent.putExtra(EXTRA_BOOL_FROM, fromMap);
        intent.putExtra(EXTRA_RES_TRACK_NUM,trackNum);
        return intent;
    }

    //fills inspectionListView with data of each of the restaurants inspections.
    private class MyListAdapter extends ArrayAdapter<InspectionData> {
        public MyListAdapter(){
            super(SingleRestaurantActivity.this,
                    R.layout.inspection_listview,
                    inspections);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.inspection_listview,
                        parent,
                        false);
            }

            InspectionData currentInspection = inspections.get(position);

            ImageView imageView = itemView.findViewById(R.id.hazardicon);
            Hazard hazard = inspections.get(position).getHazard();
            if(hazard == Hazard.LOW){
                imageView.setImageResource(R.drawable.low_hazard);
                itemView.setBackgroundColor(Color.rgb(152, 255, 156));
            }
            else if(hazard == Hazard.MEDIUM){
                imageView.setImageResource(R.drawable.moderate_hazard);
                itemView.setBackgroundColor(Color.rgb(255, 202, 125));
            }
            else {
                imageView.setImageResource(R.drawable.high_hazard);
                itemView.setBackgroundColor(Color.rgb(250, 143, 110));
            }


            TextView numCritIssueText = itemView.findViewById(R.id.numCritIssuesValue);
            numCritIssueText.setText("" + currentInspection.getCriticalViolations());

            TextView numNonCritIssueText = itemView.findViewById(R.id.numNonCritVal);
            numNonCritIssueText.setText("" + currentInspection.getNonCriticalViolations());

            TextView inspectionDateText = itemView.findViewById(R.id.inspectionDateValue);

            long date = currentInspection.timeSinceInspection();
            if(date < 30){
                inspectionDateText.setText(String.valueOf(date));
            }
            else if(date < 365){
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd");
                String strDate = formatter.format(currentInspection.getInspectionDate());
                inspectionDateText.setText(strDate);
            }
            else{
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM YYYY");
                String strDate = formatter.format(currentInspection.getInspectionDate());
                inspectionDateText.setText(strDate);
            }


            return itemView;
        }
    }
}