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

import ca.sfu.cmpt_276_project.R;

public class Inspection_Details_Activity extends AppCompatActivity {

    public static Intent makeIntent(Context context) {
        return new Intent(context, Inspection_Details_Activity.class);
    }


    // dummy violation list
    private List<DummyViolations> restaurantDummyViolationsList = new ArrayList<DummyViolations>();

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


        // dummy single inspection data;
        DummyInspections PizzaHut = new DummyInspections("March 20 2019",
                "Routine", 1, 2,"Low",R.drawable.hazardlow);

        TextView inspectDate = findViewById(R.id.res_inspect_date);
        inspectDate.setText(PizzaHut.getInspectDate());

        TextView inspectType = findViewById(R.id.res_inspect_type);
        inspectType.setText(PizzaHut.getInspectType());

        TextView numCrit = findViewById(R.id.res_num_critical);
        numCrit.setText("" + PizzaHut.getNumCritical());

        TextView numNonCrit = findViewById(R.id.res_num_noncriticial);
        numNonCrit.setText("" + PizzaHut.getNumNonCritical());

        //Fill hazard level and change color based on it's value
        TextView hazard = findViewById(R.id.res_hazard_rating);
        if(PizzaHut.getHazard().equals("Low")){
            hazard.setTextColor(Color.rgb(37, 148, 55));
        }
        else if(PizzaHut.getHazard().equals("Moderate")){
            hazard.setTextColor(Color.MAGENTA);
        }
        else{
            hazard.setTextColor((Color.RED));
        }
        hazard.setText(PizzaHut.getHazard());

        ImageView hazardIcon = findViewById(R.id.hazard_icon);
        hazardIcon.setImageResource((PizzaHut.getHazardIcon()));

        populateViolationsList();
        populateListView();
        registerClickCallback();

    }

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

    }

    private void populateListView() {
        ArrayAdapter<DummyViolations> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.violationList);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.violationList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                DummyViolations clickedViolation = restaurantDummyViolationsList.get(position);

                // Toast full detail of the clicked violation
                Toast.makeText(Inspection_Details_Activity.this, clickedViolation.getLongDetail(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<DummyViolations> {

        public MyListAdapter() {
            super(Inspection_Details_Activity.this, R.layout.violation_view, restaurantDummyViolationsList);

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            View violationsView = convertView;
            if(violationsView == null){
                violationsView = getLayoutInflater().inflate(R.layout.violation_view,
                        parent,false);
            }

            DummyViolations currentViolation = restaurantDummyViolationsList.get(position);

            // Fill short details and change text color based on critical rating
            TextView violationTxt = (TextView)violationsView.findViewById((R.id.violation_txt));
            if(currentViolation.getIsCritical() == "Critical"){
                violationTxt.setTextColor(Color.rgb(37, 148, 55));
            }
            else{
                violationTxt.setTextColor(Color.RED);
            }
            violationTxt.setText((currentViolation.getShortDetail()));

            ImageView violationIcon = (ImageView)violationsView.findViewById(R.id.violation_icon);
            violationIcon.setImageResource(currentViolation.getViolationICon());

            ImageView violationLevelImage = (ImageView)violationsView.findViewById((R.id.violation_level));
            violationLevelImage.setImageResource(currentViolation.getViolationLevelIcon());

            return violationsView;
        }
    }
}