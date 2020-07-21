/*
 * Activity: Main Activity
 *
 * Activity description: Give a list of restaurant and brief information.
 *
 * */
package ca.sfu.cmpt_276_project.UI;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;
import ca.sfu.cmpt_276_project.TestingActivity;
import ca.sfu.cmpt_276_project.WebScraper.DataManager;
import ca.sfu.cmpt_276_project.WebScraper.DataStatus;
import ca.sfu.cmpt_276_project.WebScraper.RunMode;

public class MainActivity extends AppCompatActivity {
    private final static boolean DEBUG = false; // Access for debugging mode
    private RestaurantManager restaurantManager;
    private int[] restaurantIcons;
    private List<Restaurant> restaurants;
    private DataManager dataManager = new DataManager();
    private DataStatus dataStatus;
    private RunMode runMode = RunMode.DEFAULT;  //TODO: FIX BUG ON FIRST RUN
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Surrey Restaurant Inspections");

        try {
            dataStatus = dataManager.checkForUpdates();
            System.out.println("returned update info: " + dataStatus);
        } catch (ExecutionException | InterruptedException | IOException | ParseException e) {
            e.printStackTrace();
        }

        if (dataStatus != DataStatus.UP_TO_DATE)
            runMode = createDownloadDialog("New Updates Found", "updates", dataStatus);
        else runMode = RunMode.LOCAL;
        outputRunmode();

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#31b1c4"));

        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        restaurantManager = RestaurantManager.getInstance();

        initializeRestaurantList();//method necessary to initialize instance
        populateRestaurantIcons();
        populateListView();
        registerClickCallback();

        if (DEBUG) {
            RunDebugMode();
        }
    }

    public void outputRunmode(){
        System.out.println("Selected: "+runMode);
    }

    public RunMode createDownloadDialog(String title, String msg, DataStatus status){
        RunMode[] result = new RunMode[1];
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Download " + msg + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result[0] = RunMode.UPDATE;
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("CANCEL");
                        if (status==DataStatus.NOT_EXIST) result[0] = RunMode.DEFAULT;
                        else result[0]=RunMode.LOCAL;
                        dialogInterface.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return result[0];
    }

    /**
     * Debug Mode
     */
    private void RunDebugMode() {
        Intent intent = new Intent(this, TestingActivity.class);
        startActivity(intent);
    }

    private void populateRestaurantIcons() {
        restaurantIcons = new int[8];
        restaurantIcons[0] = R.drawable.icon_sushi;
        restaurantIcons[1] = R.drawable.icon_dimsum;
        restaurantIcons[2] = R.drawable.icon_dimsum;
        restaurantIcons[3] = R.drawable.icon_aw;
        restaurantIcons[4] = R.drawable.icon_beer;
        restaurantIcons[5] = R.drawable.icon_pizza;
        restaurantIcons[6] = R.drawable.icon_pizza;
        restaurantIcons[7] = R.drawable.icon_chicken;
    }

    public void initializeRestaurantList() {
        //get Restaurants from CSV
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
        List<Restaurant> restaurantList = new ArrayList<>();

        // TODO:  refresh after download complete
        try {
            if (runMode == RunMode.LOCAL) {
                System.out.println("LOADING LOCAL DATA");
                restaurantImport.readRestaurantList(this, dataManager.getDirectory_path()+dataManager.getRestaurant_filename(), 1);
                restaurantList = restaurantImport.getRestaurantList();
            } else if (runMode == RunMode.DEFAULT){
                System.out.println("LOADING DEFAULT DATA");
                restaurantImport.readRestaurantList(this,null, 0);
                restaurantList = restaurantImport.getRestaurantList();
            }else {
                System.out.println("DOWNLOADING DATA");
                dataManager.downloadList(this,dataManager.getRestaurant_url(),dataManager.getRestaurant_filename(),dataManager.getRestaurant_csv_url());
                recreate();
                restaurantImport.readRestaurantList(this, dataManager.getDirectory_path()+dataManager.getRestaurant_filename(), 1);
                restaurantList = restaurantImport.getRestaurantList();
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //get Inspection Data of Restaurants from CSV
        InspectionDataCSVIngester inspectionDataImport = new InspectionDataCSVIngester();
        try {
            if (runMode== RunMode.LOCAL) {
                System.out.println("LOADING LOCAL DATA");
                inspectionDataImport.readInspectionData(this, dataManager.getDirectory_path()+dataManager.getInspection_filename(), 1);
            } else if (runMode == RunMode.DEFAULT){
                System.out.println("LOADING DEFAULT DATA");
                inspectionDataImport.readInspectionData(this, null, 0);
            }else{
                System.out.println("DOWNLOADING DATA");
                dataManager.downloadList(this,dataManager.getInspection_url(),dataManager.getInspection_filename(),dataManager.getInspection_csv_url());
                recreate();
                inspectionDataImport.readInspectionData(this, dataManager.getDirectory_path()+dataManager.getInspection_filename(), 1);
            }
            //Sort inspection data into proper Restaurant objects
            if (!restaurantList.isEmpty()) {
                for (Restaurant restaurant : restaurantList) {
                    restaurant.setInspectionDataList(inspectionDataImport.returnInspectionByID
                            (restaurant.getTrackNumber()));
                }
            }
        } catch (IOException | ParseException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //Update existing Restaurant Manager obj instance
        restaurantManager.setRestaurants(restaurantList);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateListView();
        registerClickCallback();
    }

    private void populateListView() {
        restaurants = restaurantManager.getRestaurants();
        ArrayAdapter<Restaurant> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);
    }


    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Restaurant clickedRestaurant = restaurantManager.getRestaurantByID(position);

                // pass clicked restaurant's position to SingleRestaurantActivity
                Intent intent = SingleRestaurantActivity.makeIntent(MainActivity.this, position);
                startActivity(intent);
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Restaurant> {

        public MyListAdapter() {
            super(MainActivity.this, R.layout.restaurants_view, restaurants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View restaurantView = convertView;
            if (restaurantView == null) {
                restaurantView = getLayoutInflater().inflate(R.layout.restaurants_view, parent, false);

            }

            Restaurant currentRestaurant = restaurantManager.getRestaurantByID(position);

            // Fill restaurant image
            ImageView resImageView = (ImageView) restaurantView.findViewById(R.id.restaurant_icon);
            currentRestaurant.setIcon(restaurantIcons[(position % 8)]);
            resImageView.setImageResource(currentRestaurant.getIcon());

            // Fill hazard icon
            ImageView hazardIconView = (ImageView) restaurantView.findViewById(
                    R.id.restaurant_hazardicon);


            //Fill hazard level with color
            TextView hazardLevelView = (TextView) restaurantView.findViewById(R.id.hazard_level);
            if (currentRestaurant.getInspectionDataList().isEmpty()) {
                hazardLevelView.setText("None");
            } else {
                Hazard hazard = currentRestaurant.getInspectionDataList().get(0).getHazard();
                if (hazard == Hazard.LOW) {
                    hazardLevelView.setTextColor(Color.rgb(37, 148, 55));
                    hazardIconView.setImageResource(R.drawable.hazardlow);
                    hazardLevelView.setText("LOW");
                } else if (hazard == Hazard.MEDIUM) {
                    hazardLevelView.setTextColor(Color.MAGENTA);
                    hazardIconView.setImageResource(R.drawable.hazardyellow);
                    hazardLevelView.setText("MEDIUM");
                } else {
                    hazardLevelView.setTextColor((Color.RED));
                    hazardIconView.setImageResource(R.drawable.hazardhigh);
                    hazardLevelView.setText("HIGH");
                }


            }

            // Fill name
            TextView nameText = (TextView) restaurantView.findViewById(R.id.restaurant_txtName);
            nameText.setText(currentRestaurant.getRestaurantName());

            //Fill the most recent inspection date
            TextView dateText = (TextView) restaurantView.findViewById(R.id.restaurant_txtDate);

            if (currentRestaurant.getInspectionDataList().isEmpty()) {
                dateText.setText("");
            } else {
                // Get the most recent inspection date
                Date recentInspectDate = currentRestaurant.getInspectionDataList().get(0).getInspectionDate();

                long date = currentRestaurant.getInspectionDataList().get(0).timeSinceInspection();
                if (date < 30) {
                    dateText.setText(String.valueOf(date));
                } else if (date < 365) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd");
                    String strDate = formatter.format(recentInspectDate);
                    dateText.setText(strDate);
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM YYYY");
                    String strDate = formatter.format(recentInspectDate);
                    dateText.setText(strDate);
                }

            }

            // Fill # issues
            TextView numIssuesText = (TextView) restaurantView.findViewById(R.id.restaurant_txtIssues);

            if (currentRestaurant.getInspectionDataList().isEmpty()) {
                numIssuesText.setText("");
            } else {
                int currentCritical = currentRestaurant.getInspectionDataList().get(0).getCriticalViolations();
                int currentNonCritical = currentRestaurant.getInspectionDataList().get(0).getNonCriticalViolations();
                int numIssues = currentCritical + currentNonCritical;

                numIssuesText.setText("" + numIssues);
            }

            return restaurantView;
        }
    }
}