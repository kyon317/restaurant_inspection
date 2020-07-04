package ca.sfu.cmpt_276_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.InputStream;

import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.Violation;
import ca.sfu.cmpt_276_project.CsvIngester.csvImporter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        csvImporter test_importer = new csvImporter();
        Restaurant dummy_restaurant = new Restaurant();
        test_importer.readRestaurantList(this);
    }
}