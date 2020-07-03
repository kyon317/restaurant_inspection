package ca.sfu.cmpt_276_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button restaurant_btn = (Button)findViewById(R.id.button);
        restaurant_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SingleRestaurantActivity.makeIntent(MainActivity.this);
                startActivity(intent);
            }
        });
    }
}