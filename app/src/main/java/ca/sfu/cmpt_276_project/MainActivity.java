package ca.sfu.cmpt_276_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.ViolationTXTIngester;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data_mitigate_test();
//        RestaurantCSVIngester test_restaurant_ingester = new RestaurantCSVIngester();
//        try {
//            test_restaurant_ingester.readRestaurantList(this);
//        } catch (IOException e) {
//           e.printStackTrace();
//        }

//        InspectionDataCSVIngester test_inspection_ingester = new InspectionDataCSVIngester();
//
//        try {
//           test_inspection_ingester.readInspectionData(this);
//       } catch (IOException e) {
//           e.printStackTrace();
//        } catch (ParseException e) {
//           e.printStackTrace();
//       }


//        ViolationTXTIngester test = new ViolationTXTIngester();

//        try {
//            test.readViolationData(this);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    public void data_mitigate_test(){
        RestaurantCSVIngester test_importer = new RestaurantCSVIngester();
        List<Restaurant> restaurantList = new ArrayList<>();
        try {
            test_importer.readRestaurantList(this);
            restaurantList = test_importer.getRestaurantList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InspectionDataCSVIngester test_inspection_ingester = new InspectionDataCSVIngester();

        try {
            test_inspection_ingester.readInspectionData(this);
            if (!restaurantList.isEmpty())
                for (Restaurant restaurant:restaurantList
                ) {
                    restaurant.setInspectionDataList(test_inspection_ingester.returnInspectionByID(restaurant.getTrackNumber()));
                }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (!restaurantList.isEmpty())
            for (Restaurant restaurant:restaurantList
            ) {
                restaurant.Display();
            }
    }
}