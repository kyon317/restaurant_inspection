package ca.sfu.cmpt_276_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;

public class MainActivity extends AppCompatActivity {

    private RestaurantManager restaurantManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restaurantManager = RestaurantManager.getInstance();
        initializeRestaurantList();//method necessary to initialize instance
        launchTestingActivity();
    }

    public void initializeRestaurantList(){
        //get Restaurants from CSV
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
        List<Restaurant> restaurantList = new ArrayList<>();
        try {
            restaurantImport.readRestaurantList(this);
            restaurantList = restaurantImport.getRestaurantList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get Inspection Data of Restaurants from CSV
        InspectionDataCSVIngester inspectionDataImport = new InspectionDataCSVIngester();
        try {
            inspectionDataImport.readInspectionData(this);

            //Sort inspection data into proper Restaurant objects
            if (!restaurantList.isEmpty()) {
                for (Restaurant restaurant : restaurantList) {
                    restaurant.setInspectionDataList(inspectionDataImport.returnInspectionByID
                            (restaurant.getTrackNumber()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Update existing Restaurant Manager obj instance
        restaurantManager.setRestaurants(restaurantList);

        /* Debugging Pretty Printer, uncomment to test further
        if (!restaurantManager.getRestaurants().isEmpty()) {
            int inspection_count = 0;
            for (Restaurant restaurant : restaurantManager.getRestaurants()) {
                restaurant.Display();
                //System.out.println(restaurant.getRestaurantName()); sort check
                inspection_count += restaurant.getInspectionDataList().size();
            }
            System.out.println("Restaurant Count: "+restaurantList.size());
            System.out.println("Inspection Count: "+inspection_count);
        }*/
    }
    public void makeDummyChanges(){
        restaurantManager.getRestaurants().remove(0);
    }
    public void launchTestingActivity(){
        makeDummyChanges();         //make changes on instance to test data consistency
        Intent intent = new Intent(this,TestingActivity.class);
        startActivity(intent);
    }
}