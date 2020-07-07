package ca.sfu.cmpt_276_project.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;

/*
Shows the details of a single restaurant.Shows restaurant name, address, and coordinates.
Has a scroll-able list that shows all it's inspections by recency.
Each inspection on the list shows: #critical issues, #non-critical issues, how long
since inspection, and an icon with hazard level and colour. When clicked, an inspection will take
the user to IndividualInspectionActivity.
 */
/*class Inspection{
    private int numCritIssues;
    private int numNonCritIssues;
    private String timeSinceInspection;
    private int icon;
    private String hazardLevel;

    public Inspection(int numCritIssues,
                      int numNonCritIssues,
                      String timeSinceInspection,
                      int icon,
                      String hazardLevel){
        this.numCritIssues = numCritIssues;
        this.numNonCritIssues = numNonCritIssues;
        this.timeSinceInspection = timeSinceInspection;
        this.icon = icon;
        this.hazardLevel = hazardLevel;
    }

    public int getNumCritIssues() {
        return numCritIssues;
    }

    public int getNumNonCritIssues() {
        return numNonCritIssues;
    }

    public String getTimeSinceInspection() {
        return timeSinceInspection;
    }

    public int getIcon() {
        return icon;
    }

    public String getHazardLevel() {
        return hazardLevel;
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

    public int getIcon() {
        return icon;
    }

    public String getAddress() {
        return address;
    }

    public int getLatitude() {
        return latitude;
    }

    public int getLongitude() {
        return longitude;
    }

}*/

public class SingleRestaurantActivity extends AppCompatActivity {

    private List<InspectionData> inspections = new ArrayList<>();
    private RestaurantManager restaurantManager;

    private int position;
    private Restaurant restaurant;

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

        Intent intent = getIntent();
        position = intent.getIntExtra("position", position);

        //give the view the restaurant info
        restaurantManager = RestaurantManager.getInstance();
        restaurant = restaurantManager.getRestaurantByID(position);
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
                Intent intent = Inspection_Details_Activity.makeIntent(
                        SingleRestaurantActivity.this);
                intent.putExtra("position", position);
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
    public static Intent makeIntent(Context context) {
        return new Intent(context, SingleRestaurantActivity.class);
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
            inspectionDateText.setText(""+ currentInspection.getInspectionDate());



            return itemView;
        }
    }
}