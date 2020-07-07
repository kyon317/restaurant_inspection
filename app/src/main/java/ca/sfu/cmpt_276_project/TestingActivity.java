/*
* This is a testing activity to test singleton class and data consistency
*
* */

package ca.sfu.cmpt_276_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.List;

import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;

public class TestingActivity extends AppCompatActivity {
    private RestaurantManager restaurantManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        restaurantManager = RestaurantManager.getInstance();
        SingletonTest();
    }

    public void SingletonTest(){
        List<Restaurant> restaurantList = restaurantManager.getRestaurants();
        if (!restaurantManager.getRestaurants().isEmpty()) {
            int inspection_count = 0;
            for (Restaurant restaurant : restaurantManager.getRestaurants()) {
                restaurant.Display();
                if (restaurant.getInspectionDataList().size()!=0){
                    for (InspectionData inspection:restaurant.getInspectionDataList()
                         ) {
                        System.out.println("Days from inspection: "+ inspection.timeSinceInspection());
                    }
                }
                inspection_count += restaurant.getInspectionDataList().size();
            }
            System.out.println("Restaurant Count: "+restaurantList.size());
            System.out.println("Inspection Count: "+inspection_count);
        }
    }
}