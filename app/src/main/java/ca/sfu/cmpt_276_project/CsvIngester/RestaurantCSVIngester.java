/*
* Class: RestaurantCSVIngester
*
* Class Description: The csvImporter Class contains a csv reader that fetches data from the provided restaurants_itr1.csv file.
*
* Function:
* 1. readRestaurantList(Context context): Given the context, the function fetches data from restaurants_itr1.csv and interprets it into a List of Restaurants.[Done]
*
* */

package ca.sfu.cmpt_276_project.CsvIngester;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.R;

public class RestaurantCSVIngester {

    private List<Restaurant> restaurantList = new ArrayList<>();


    public void readRestaurantList(Context context, InputStream inputStream, int updateCode) throws IOException {
        InputStream restaurantDataInput = context.getResources().openRawResource
                (R.raw.restaurants_itr1);
        if (updateCode == 1) {
            restaurantDataInput = inputStream;
        }


        BufferedReader reader = new BufferedReader(new InputStreamReader(restaurantDataInput,
                StandardCharsets.UTF_8));
        String inputLine = "";

        //reading and storing CSV data
        try {
            reader.readLine();
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.contains(", ")) {
                    inputLine = inputLine.replaceAll(", ", "000");
                    System.out.println(inputLine);
                    System.out.println("found false regex");
                }
                String[] tokens = inputLine.split(",");
                Restaurant dummy_restaurant = new Restaurant();
                dummy_restaurant.setTrackNumber(tokens[0]);
                if (tokens[1].contains("000")) {
                    tokens[1] = tokens[1].replace("000", ", ");
                }
                dummy_restaurant.setRestaurantName(tokens[1]);
                dummy_restaurant.setPhysicalAddress(tokens[2]);
                dummy_restaurant.setPhysicalCity(tokens[3]);
                dummy_restaurant.setFacType(tokens[4]);
                dummy_restaurant.setLatitude(Double.parseDouble(tokens[5]));
                dummy_restaurant.setLongitude(Double.parseDouble(tokens[6]));

                restaurantList.add(dummy_restaurant);
            }
        }catch (IOException e){
            Log.wtf("Reading Activity","Fatal Error when reading file on line" +inputLine,e);
            e.printStackTrace();
        }

/*        //Display restaurant list
        for (Restaurant res:restaurantList
             ) {
            res.Display();
        }
        System.out.println(this.restaurantList.size()); //Debugging purposes*/

    }//end of function

    public List<Restaurant> getRestaurantList() {
        return restaurantList;
    }
}
