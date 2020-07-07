package ca.sfu.cmpt_276_project.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

/*
Shows the details of a single restaurant.Shows restaurant name, address, and coordinates.
Has a scroll-able list that show all it's inspections by recency.
Each inspection on the list should show: #critical issues, #non-critical issues, how long
since inspection, and an icon with hazard level and colour. When clicked, an inspection will take
the user to IndividualInspectionActivity.
 */
class Inspection{
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

}

public class SingleRestaurantActivity extends AppCompatActivity {
    static Restaurant restaurant = new Restaurant("Denny's",
            0,
            "1234 Main St.",
            50,
            -123
            );
    private List<Inspection> inspections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_restaurant);

        getSupportActionBar().setTitle("Surrey Restaurant Inspections");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView restaurantName_textview = findViewById(R.id.Restaurant_name);
        restaurantName_textview.setText(restaurant.getName());
        TextView addressText_textview = findViewById(R.id.addressText);
        addressText_textview.setText(restaurant.getAddress());
        TextView coords_textView = findViewById(R.id.coordinatesText);
        coords_textView.setText(restaurant.getLatitude() + ", " + restaurant.getLongitude());

        inspections.add(new Inspection(2,
                1,
                "20 days",
                R.drawable.high_hazard,
                "high"));
        inspections.add(new Inspection(0,
                0,
                "1 year",
                R.drawable.low_hazard,
                "low"));
        populateInspectionsList();
        registerOnClick();


    }

    @Override
    protected void onResume(){
        super.onResume();
        populateInspectionsList();
        registerOnClick();
    }


    private void registerOnClick() {
        ListView list = findViewById(R.id.inspectionsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = Inspection_Details.makeIntent(
                        SingleRestaurantActivity.this);
                startActivity(intent);

            }
        });
    }

    private void populateInspectionsList() {
        ArrayAdapter<Inspection> adapter = new MyListAdapter();
        ListView list = findViewById(R.id.inspectionsListView);
        list.setAdapter(adapter);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, SingleRestaurantActivity.class);
    }

    private class MyListAdapter extends ArrayAdapter<Inspection> {
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
            Inspection currentInspection = inspections.get(position);

            ImageView imageView = itemView.findViewById(R.id.hazardicon);
            imageView.setImageResource(currentInspection.getIcon());

            TextView numCritIssueText = itemView.findViewById(R.id.numCritIssuesValue);
            numCritIssueText.setText("" + currentInspection.getNumCritIssues());

            TextView numNonCritIssueText = itemView.findViewById(R.id.numNonCritVal);
            numNonCritIssueText.setText("" + currentInspection.getNumNonCritIssues());

            TextView inspectionDateText = itemView.findViewById(R.id.inspectionDateValue);
            inspectionDateText.setText(currentInspection.getTimeSinceInspection());

            if(currentInspection.getHazardLevel().equals("low")){
                itemView.setBackgroundColor(Color.GREEN);
            }
            else if(currentInspection.getHazardLevel().equals("medium")){
                itemView.setBackgroundColor(Color.YELLOW);
            }
            else{
                itemView.setBackgroundColor(Color.RED);
            }


            //TODO set onclick
            return itemView;
        }
    }
}