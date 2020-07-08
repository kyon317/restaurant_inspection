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
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.Model.Violation;
import ca.sfu.cmpt_276_project.R;

public class Inspection_Details_Activity extends AppCompatActivity {

    public static Intent makeIntent(Context context, int position, int restaurantPosition) {
        Intent intent= new Intent(context, Inspection_Details_Activity.class);
        intent.putExtra(EXTRA_RES_NUM, restaurantPosition);
        intent.putExtra(EXTRA_INSPECTION_NUM,position);
        return intent;
    }

    private static final String EXTRA_RES_NUM = "ca.sfu.cmpt_276_project.UI.extraResNum";
    private static final String EXTRA_INSPECTION_NUM = "ca.sfu.cmpt_276_project.UI.extraInspectionNum";

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
        restaurantNum = intent.getIntExtra(EXTRA_RES_NUM, 0);
        inspectionNum = intent.getIntExtra(EXTRA_INSPECTION_NUM, 0);

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

        populateListView();
        registerClickCallback();

    }

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

    // Fill violation_view with violations data
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

            if(inspection.getNonCriticalViolations() == 0 && inspection.getCriticalViolations() == 0){
                return violationsView;
            }

            Violation currentViolation = violations.get(position);

            // Fill short details and change text color based on critical rating
            TextView violationTxt = (TextView)violationsView.findViewById((R.id.violation_txt));
            ImageView violationIcon = (ImageView)violationsView.findViewById(R.id.violation_icon);
            if(!currentViolation.isCritical()){
                violationTxt.setTextColor(Color.rgb(37, 148, 55));
                violationIcon.setImageResource(R.drawable.icon_warning);
            }
            else{
                violationTxt.setTextColor(Color.RED);
                violationIcon.setImageResource(R.drawable.icon_critical);
            }

            ImageView violationLevelImage = (ImageView)violationsView.findViewById((R.id.violation_level));

            // Short description and icon based on the number of violation
            int violationNum = Integer.valueOf(currentViolation.getViolationNumber());
            switch (violationNum){
                case 101:
                    violationTxt.setText("Plans not in regulation.");
                    violationLevelImage.setImageResource(R.drawable.icon_plans);
                    break;
                case 102:
                    violationTxt.setText("Unapproved food premises.");
                    violationLevelImage.setImageResource(R.drawable.icon_premises);
                    break;
                case 103:
                case 104:
                    violationTxt.setText("Invalid permits.");
                    violationLevelImage.setImageResource(R.drawable.icon_plans);
                    break;
                case 201:
                case 202:
                case 203:
                case 204:
                case 205:
                case 206:
                case 207:
                case 208:
                case 209:
                case 210:
                case 211:
                case 212:
                case 306:
                    violationTxt.setText("Unsafe foods.");
                    violationLevelImage.setImageResource(R.drawable.foods);
                    break;
                case 301:
                case 302:
                case 303:
                    violationTxt.setText("Equipment is not clean.");
                    violationLevelImage.setImageResource(R.drawable.equipments);
                    break;
                case 307:
                case 308:
                case 309:
                case 310:
                case 315:
                    violationTxt.setText("Unsafe equipment");
                    violationLevelImage.setImageResource(R.drawable.equipments);
                    break;
                case 311:
                case 312:
                case 313:
                case 314:
                    violationTxt.setText("Premises not maintain well.");
                    violationLevelImage.setImageResource(R.drawable.icon_premises);
                    break;
                case 304:
                case 305:
                    violationTxt.setText("Pests control failed.");
                    violationLevelImage.setImageResource(R.drawable.pest);
                    break;
                case 401:
                case 402:
                case 403:
                case 404:
                case 501:
                case 502:
                    violationTxt.setText("Employee safety failed.");
                    violationLevelImage.setImageResource(R.drawable.icon_employee);
                    break;
                default:

                    break;
            }

            return violationsView;
        }
    }
}