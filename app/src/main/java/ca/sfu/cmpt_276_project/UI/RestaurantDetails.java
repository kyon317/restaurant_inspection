package ca.sfu.cmpt_276_project.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ca.sfu.cmpt_276_project.R;

// dummy Restaurant Details activity
public class RestaurantDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        inspectDetailsButton();
    }

    private void inspectDetailsButton() {
        Button btn = (Button) findViewById(R.id.goto_inspect_details);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RestaurantDetails.this,Inspection_Details.class);
                startActivity(intent);
            }
        });
    }



}