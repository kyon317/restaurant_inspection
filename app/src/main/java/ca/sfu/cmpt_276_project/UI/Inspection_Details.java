package ca.sfu.cmpt_276_project.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import ca.sfu.cmpt_276_project.R;

public class Inspection_Details extends AppCompatActivity {

    public static Intent makeIntent(Context context) {
        return new Intent(context, Inspection_Details.class);
    }

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

        TextView hazard = findViewById(R.id.res_hazard_rating);
        hazard.setText(PizzaHut.getHazard());

        ImageView hazardIcon = findViewById(R.id.hazard_icon);
        hazardIcon.setImageResource((PizzaHut.getHazardIcon()));


    }



}