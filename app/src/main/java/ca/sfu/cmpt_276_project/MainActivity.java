package ca.sfu.cmpt_276_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;
import java.text.ParseException;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.ViolationTXTIngester;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        RestaurantCSVIngester test_importer = new RestaurantCSVIngester();
//        Restaurant dummy_restaurant = new Restaurant();
//        try {
//            test_importer.readRestaurantList(this);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        InspectionDataCSVIngester test = new InspectionDataCSVIngester();

//        try {
//            test.readInspectionData(this);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        ViolationTXTIngester test = new ViolationTXTIngester();

        try {
            test.readViolationData(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}