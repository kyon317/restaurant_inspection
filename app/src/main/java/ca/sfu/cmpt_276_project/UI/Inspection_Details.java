package ca.sfu.cmpt_276_project.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

public class Inspection_Details extends AppCompatActivity {

    public static Intent makeIntent(Context context) {
        return new Intent(context, Inspection_Details.class);
    }


    // dummy violation list
    private List<Violations> restaurantViolationsList = new ArrayList<Violations>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection__details);


        // dummy single inspection data;
        Inspections PizzaHut = new Inspections("March 20 2019",
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
            hazard.setTextColor(Color.GREEN);
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
        restaurantViolationsList.add(new Violations("101, Plans/construction ",
                "101,Not Critical,Plans/construction/alterations not in accordance with the Regulation [s. 3; s. 4],Not Repeat",
                R.drawable.equipments,R.drawable.hazardlow, "Non Critical"));
        restaurantViolationsList.add(new Violations("208,Foods ",
                "208,Not Critical,Foods obtained from unapproved sources [s. 11],Not Repeat",
                R.drawable.foods,R.drawable.hazardhigh, "Critical"));
        restaurantViolationsList.add(new Violations("304,Premises ",
                "304,Not Critical,Premises not free of pests [s. 26(a)],Not Repeat",
                R.drawable.pest,R.drawable.hazardlow, "Non Critical"));
    }

    private void populateListView() {
        ArrayAdapter<Violations> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.violationList);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.violationList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Violations clickedViolation = restaurantViolationsList.get(position);

                // Toast full detail of the clicked violation
                Toast.makeText(Inspection_Details.this, clickedViolation.getLongDetail(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Violations> {

        public MyListAdapter() {
            super(Inspection_Details.this, R.layout.violation_view, restaurantViolationsList);

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            View violationsView = convertView;
            if(violationsView == null){
                violationsView = getLayoutInflater().inflate(R.layout.violation_view,
                        parent,false);
            }

            Violations currentViolation = restaurantViolationsList.get(position);

            TextView violationTxt = (TextView)violationsView.findViewById((R.id.violation_txt));
            violationTxt.setText((currentViolation.getShortDetail()));

            ImageView violationIcon = (ImageView)violationsView.findViewById(R.id.violation_icon);
            violationIcon.setImageResource(currentViolation.getViolationICon());

            ImageView violationLevelImage = (ImageView)violationsView.findViewById((R.id.violation_level));
            violationLevelImage.setImageResource(currentViolation.getViolationLevelIcon());

            return violationsView;
        }
    }
}