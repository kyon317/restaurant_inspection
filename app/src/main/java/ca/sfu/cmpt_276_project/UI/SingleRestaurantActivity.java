/*
 * Activity: Main Activity
 *
 * Activity description: Give a list of inspections of selected restaurant.
 *
 * */
package ca.sfu.cmpt_276_project.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;


public class SingleRestaurantActivity extends AppCompatActivity {

    private List<InspectionData> inspections = new ArrayList<>();
    private RestaurantManager restaurantManager;

    private int restaurantPosition;
    private Restaurant restaurant;

    private static final String EXTRA_RES_NUM = "ca.sfu.cmpt_276_project.UI.extraResNum";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_restaurant);

        getSupportActionBar().setTitle("Surrey Restaurant Inspections");

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#31b1c4"));

        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Todo: fix actionbar back btn so it returns to last activity.


        Intent intent = getIntent();
        restaurantPosition = intent.getIntExtra(EXTRA_RES_NUM, 0);

        //give the view the restaurant info
        restaurantManager = RestaurantManager.getInstance();
        restaurant = restaurantManager.getRestaurantByID(restaurantPosition);
        inspections = restaurant.getInspectionDataList();

        populatateView();
        populateInspectionsList();
        registerOnClick();

    }

    private void populatateView() {
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

                Intent intent = MapsActivity.makeLaunchIntent(SingleRestaurantActivity.this,
                       restaurant.getTrackNumber(), true);
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
    public static Intent makeIntent(Context context, int restaurantPosition) {
        Intent intent =  new Intent(context, SingleRestaurantActivity.class);
        intent.putExtra(EXTRA_RES_NUM, restaurantPosition);
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