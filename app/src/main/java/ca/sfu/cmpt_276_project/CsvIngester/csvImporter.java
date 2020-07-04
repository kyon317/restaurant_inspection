package ca.sfu.cmpt_276_project.CsvIngester;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.MainActivity;
import ca.sfu.cmpt_276_project.Model.*;
import ca.sfu.cmpt_276_project.R;

public class csvImporter  {
    private List<Restaurant> restaurantList = new ArrayList<>();

    public void readRestaurantList(Context context) throws IOException {
        InputStream restaurantDataInput = context.getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(new InputStreamReader(restaurantDataInput, Charset.forName("UTF-8")));
        String inputLine = "";
        try{
            reader.readLine();
            while((inputLine = reader.readLine())!=null){
                String[] tokens = inputLine.split(",");
                System.out.println("tokens: "+tokens[0]);
                System.out.println("tokens: "+tokens[1]);
                Restaurant dummy_restaurant = new Restaurant();
                dummy_restaurant.setTrackNumber(tokens[0]);
                dummy_restaurant.setRestaurantName(tokens[1]);
                dummy_restaurant.setPhysicalAddress(tokens[2]);
                dummy_restaurant.setPhysicalCity(tokens[3]);
                dummy_restaurant.setFacType(tokens[4]);
                System.out.println("tokens: "+tokens[5]);
                System.out.println("tokens: "+tokens[6]);
                //dummy_restaurant.setLatitude(Double.parseDouble(tokens[5]));
                //dummy_restaurant.setLongitude(Double.parseDouble(tokens[6]));
                //dummy_restaurant.setInspectionData(null);
                //dummy_restaurant.Display();
                restaurantList.add(dummy_restaurant);
            }
        }catch (IOException e){
            Log.wtf("Reading Activity","Fatal Error when reading file on line" +inputLine,e);
            e.printStackTrace();
        }
    }

}
