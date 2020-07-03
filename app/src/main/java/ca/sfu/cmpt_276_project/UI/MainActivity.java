package ca.sfu.cmpt_276_project.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.R;
import ca.sfu.cmpt_276_project.model.Restaurants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        readCSVData();

        // Sort array list
        // Collections.sort(surreyRestaurants);

        populateRestaurantList();
    }

    private List<Restaurants> surreyRestaurants = new ArrayList<>();

    private void readCSVData() {
        InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";

        try{
            while ( (line = reader.readLine()) != null){

                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                Restaurants tempRestaurant = new Restaurants();
                tempRestaurant.setTrackingNum(tokens[0]);
                tempRestaurant.setName(tokens[1]);
                tempRestaurant.setStreetAddress(tokens[2]);
                tempRestaurant.setCityAddress(tokens[3]);
                tempRestaurant.setIcon(tempRestaurant.getLogo());
                tempRestaurant.setLatitude(tokens[5]);
                tempRestaurant.setLongitude(tokens[6]);

                surreyRestaurants.add(tempRestaurant);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file on line" + line, e);
            e.printStackTrace();
        }

    }

    private void populateRestaurantList() {
        ArrayAdapter<Restaurants> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);

    }

    private class MyListAdapter extends ArrayAdapter<Restaurants> {
        public MyListAdapter(){
            super(MainActivity.this, R.layout.restaurants_view,surreyRestaurants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.restaurants_view,parent,false);

            }

            Restaurants currentRestaurant = surreyRestaurants.get(position);

            ImageView imageView = (ImageView)itemView.findViewById(R.id.icon_view);
            imageView.setImageResource(currentRestaurant.getIcon());

            return itemView;
        }
    }

}
