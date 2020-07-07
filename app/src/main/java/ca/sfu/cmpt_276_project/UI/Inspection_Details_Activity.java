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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.Model.Violation;
import ca.sfu.cmpt_276_project.R;

public class Inspection_Details_Activity extends AppCompatActivity {

    public static Intent makeIntent(Context context) {
        return new Intent(context, Inspection_Details_Activity.class);
    }


    // dummy violation list
    private List<DummyViolations> restaurantDummyViolationsList = new ArrayList<DummyViolations>();

    private RestaurantManager restaurantManager;
    private List<Violation> violations = new ArrayList<>();
    InspectionData inspection;
    int restaurantNum;
    int inspectionNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection__details);

        getSupportActionBar().setTitle("Inspection Details");

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#31b1c4"));

        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        restaurantManager = RestaurantManager.getInstance();
        Intent intent = getIntent();
        restaurantNum = intent.getIntExtra("position", restaurantNum);
        inspectionNum = intent.getIntExtra("inspection", inspectionNum);
        inspection = restaurantManager.getRestaurantByID(
                restaurantNum).getInspectionDataList().get(inspectionNum);
        violations = inspection.getViolation();

        TextView inspectDate = findViewById(R.id.res_inspect_date);
        inspectDate.setText("" + inspection.getInspectionDate());

        TextView inspectType = findViewById(R.id.res_inspect_type);
        inspectType.setText("" + inspection.getInspectionType());

        TextView numCrit = findViewById(R.id.res_num_critical);
        numCrit.setText("" + inspection.getCriticalViolations());

        TextView numNonCrit = findViewById(R.id.res_num_noncriticial);
        numNonCrit.setText("" + inspection.getNonCriticalViolations());

        //Fill hazard level and change color based on it's value
        TextView hazard = findViewById(R.id.res_hazard_rating);
        ImageView hazardIcon = findViewById(R.id.hazard_icon);
        Hazard hazardLevel = inspection.getHazard();
        if(hazardLevel == Hazard.LOW){
            hazard.setTextColor(Color.rgb(37, 148, 55));
            hazard.setText("Low");
            hazardIcon.setImageResource(R.drawable.hazardlow);
        }
        else if(hazardLevel == Hazard.MEDIUM){
            hazard.setTextColor(Color.MAGENTA);
            hazard.setText("Moderate");
            hazardIcon.setImageResource(R.drawable.hazardyellow);
        }
        else{
            hazard.setTextColor((Color.RED));
            hazard.setText("High");
            hazardIcon.setImageResource(R.drawable.hazardhigh);
        }


        //populateViolationsList();
        populateListView();
        registerClickCallback();

    }
/*
    // add dummy violations data
    private void populateViolationsList() {
        restaurantDummyViolationsList.add(new DummyViolations("101, Plans/construction ",
                "101,Not Critical,Plans/construction/alterations not in accordance with the Regulation [s. 3; s. 4],Not Repeat",
                R.drawable.equipments,R.drawable.hazardlow, "Non Critical"));
        restaurantDummyViolationsList.add(new DummyViolations("208,Foods ",
                "208,Not Critical,Foods obtained from unapproved sources [s. 11],Not Repeat",
                R.drawable.foods,R.drawable.hazardhigh, "Critical"));
        restaurantDummyViolationsList.add(new DummyViolations("304,Premises ",
                "304,Not Critical,Premises not free of pests [s. 26(a)],Not Repeat",
                R.drawable.pest,R.drawable.hazardlow, "Non Critical"));

        restaurantDummyViolationsList.add(new DummyViolations("101, Plans/construction ",
                "101,Not Critical,Plans/construction/alterations not in accordance with the Regulation [s. 3; s. 4],Not Repeat",
                R.drawable.equipments,R.drawable.hazardlow, "Non Critical"));
        restaurantDummyViolationsList.add(new DummyViolations("208,Foods ",
                "208,Not Critical,Foods obtained from unapproved sources [s. 11],Not Repeat",
                R.drawable.foods,R.drawable.hazardhigh, "Critical"));
        restaurantDummyViolationsList.add(new DummyViolations("101, Plans/construction ",
                "101,Not Critical,Plans/construction/alterations not in accordance with the Regulation [s. 3; s. 4],Not Repeat",
                R.drawable.equipments,R.drawable.hazardlow, "Non Critical"));
        restaurantDummyViolationsList.add(new DummyViolations("208,Foods ",
                "208,Not Critical,Foods obtained from unapproved sources [s. 11],Not Repeat",
                R.drawable.foods,R.drawable.hazardhigh, "Critical"));
        restaurantDummyViolationsList.add(new DummyViolations("304,Premises ",
                "304,Not Critical,Premises not free of pests [s. 26(a)],Not Repeat",
                R.drawable.pest,R.drawable.hazardlow, "Non Critical"));

    }*/

    private void populateListView() {
        ArrayAdapter<Violation> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.violationList);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.violationList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Violation clickedViolation = violations.get(position);

                // Toast full detail of the clicked violation
                Toast.makeText(Inspection_Details_Activity.this, clickedViolation.getDescription(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Violation> {

        public MyListAdapter() {
            super(Inspection_Details_Activity.this, R.layout.violation_view, violations);

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            View violationsView = convertView;
            if(violationsView == null){
                violationsView = getLayoutInflater().inflate(R.layout.violation_view,
                        parent,false);
            }

            Violation currentViolation = violations.get(position);

            // Fill short details and change text color based on critical rating
            TextView violationTxt = (TextView)violationsView.findViewById((R.id.violation_txt));
            ImageView violationIcon = (ImageView)violationsView.findViewById(R.id.violation_icon);
            if(!currentViolation.isCritical()){
                violationTxt.setTextColor(Color.rgb(37, 148, 55));
                violationIcon.setImageResource(R.drawable.low_hazard);
            }
            else{
                violationTxt.setTextColor(Color.RED);
                violationIcon.setImageResource(R.drawable.high_hazard);
            }
            //TODO: Have a way to differentiate between short and long description
            violationTxt.setText((currentViolation.getDescription()));



            ImageView violationLevelImage = (ImageView)violationsView.findViewById((R.id.violation_level));
            //TODO: set the violation icon to match violation type
            violationLevelImage.setImageResource(R.drawable.foods);


            return violationsView;
        }
    }
}