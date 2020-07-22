package ca.sfu.cmpt_276_project.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;
import ca.sfu.cmpt_276_project.WebScraper.DataManager;
import ca.sfu.cmpt_276_project.WebScraper.DataStatus;
import ca.sfu.cmpt_276_project.WebScraper.RunMode;

public class LoadingActivity extends AppCompatActivity {

    private DataManager dataManager;
    private DataStatus dataStatus;
    private RunMode runMode = RunMode.UPDATE;  //TODO: FIX BUG ON FIRST RUN
    private AlertDialog alertDialog;
    private RestaurantManager restaurantManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        dataManager = new DataManager();
        System.out.println("NETWORK"+checkNetwork());

        //selectRunMode();
        //setRestaurantManager();
        Handler handler = new Handler();
        handler.post(this::test);
    }


    public void test(){
        Log.d("test","local time");
    }
    public RunMode selectRunMode(){
        if (!checkNetwork()){
            if (networkDialog()){
                if (dataManager.checkFileExistence(dataManager.getRestaurant_filename())&&dataManager.checkFileExistence(dataManager.getInspection_filename()))
                    runMode = RunMode.LOCAL;
                else
                    runMode = RunMode.DEFAULT;
            }
        }else{
            try {
                dataStatus = dataManager.checkForUpdates();
                System.out.println("returned update info: " + dataStatus);
            } catch (ExecutionException | InterruptedException | IOException | ParseException e) {
                e.printStackTrace();
            }

            if (dataStatus != DataStatus.UP_TO_DATE)
                runMode = createDownloadDialog("New Updates Found", "updates", dataStatus);
            else runMode = RunMode.LOCAL;
        }
        return runMode;
    }

    public boolean networkDialog(){
        final boolean[] result = {false};
        new AlertDialog.Builder(this)
                .setTitle("No network connection")
                .setMessage("Load local data?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result[0]=true;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return result[0];
    }

    public boolean checkNetwork(){
        boolean isConnected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            isConnected = true;
        }
        else
            isConnected = false;
        return isConnected;
    }
    public boolean setRestaurantManager(){
        boolean result = true;
        List<Restaurant> restaurantList = new ArrayList<>();
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
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
                restaurantImport.readRestaurantList(this, dataManager.getDirectory_path()+dataManager.getRestaurant_filename(), 1);
                restaurantList = restaurantImport.getRestaurantList();
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            result = false;
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
            result = false;
        }

        //Update existing Restaurant Manager obj instance
        restaurantManager = RestaurantManager.getInstance();
        restaurantManager.setRestaurants(restaurantList);

        System.out.println("current selected mode: "+runMode);
        return true;
    }

    public void start(){
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    public RunMode createDownloadDialog(String title, String msg, DataStatus status){
        RunMode[] result = new RunMode[1];

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Download " + msg + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result[0] = RunMode.UPDATE;
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("CANCEL");
                        if (status==DataStatus.NOT_EXIST) result[0] = RunMode.DEFAULT;
                        else result[0]=RunMode.LOCAL;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return result[0];
    }

    public static class backgroundTask extends AsyncTask<Runnable,Integer,Integer>{

        @Override
        protected Integer doInBackground(Runnable... runnables) {
            int count = 0;
            System.out.println(runnables.length);
            for (Runnable runnable : runnables) {
                System.out.println("current: "+runnable);
//                runnable.run();
//                count++;
//                publishProgress(count);
            }
            return count;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            System.out.println("Progress: "+values);
        }
    }

}