/*Loading Activity loads data into RestaurantManager.
* */

package ca.sfu.cmpt_276_project.UI;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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
    private RunMode runMode;
    private RestaurantManager restaurantManager;
    private boolean alertVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        getSupportActionBar().setTitle("Surrey Restaurant Inspections");
        dataManager = new DataManager();
        try {
            dataStatus = dataManager.checkForUpdates();
        } catch (ExecutionException | InterruptedException | ParseException | IOException e) {
            e.printStackTrace();
        }
        backgroundTask task = new backgroundTask();
        try {
            task.execute("selectRunMode").get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (alertVisible){
            showDialog();
        }else{
            startMapActivity();
        }
    }

    public void selectRunMode() throws InterruptedException {
        if (!checkNetwork()){
            Toast.makeText(this,"No Network Connection, loading from local directory",Toast.LENGTH_LONG).show();
                if (dataManager.checkFileExistence(dataManager.getRestaurant_filename())&&dataManager.checkFileExistence(dataManager.getInspection_filename()))
                    runMode = RunMode.LOCAL;
                else
                    runMode = RunMode.DEFAULT;
        }else{
            if (dataStatus != DataStatus.UP_TO_DATE){
                alertVisible = true;
            }
            else runMode = RunMode.LOCAL;
        }
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
    public void setRestaurantManager(RunMode selectedRunmMode){
        List<Restaurant> restaurantList = new ArrayList<>();
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
        try {
            if (selectedRunmMode == RunMode.LOCAL) {
                System.out.println("LOADING LOCAL DATA");
                restaurantImport.readRestaurantList(this, dataManager.getDirectory_path()+dataManager.getRestaurant_filename(), 1);
                restaurantList = restaurantImport.getRestaurantList();
            } else if (selectedRunmMode == RunMode.DEFAULT){
                System.out.println("LOADING DEFAULT DATA");
                restaurantImport.readRestaurantList(this,null, 0);
                restaurantList = restaurantImport.getRestaurantList();
            }else if (selectedRunmMode == RunMode.UPDATE){
                System.out.println("DOWNLOADING DATA");
                dataManager.downloadList(this,dataManager.getRestaurant_url(),dataManager.getRestaurant_filename(),dataManager.getRestaurant_csv_url());
                restaurantImport.readRestaurantList(this, dataManager.getDirectory_path()+dataManager.getRestaurant_filename(), 1);
                restaurantList = restaurantImport.getRestaurantList();
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();

        }

        //get Inspection Data of Restaurants from CSV
        InspectionDataCSVIngester inspectionDataImport = new InspectionDataCSVIngester();
        try {
            if (selectedRunmMode== RunMode.LOCAL) {
                System.out.println("LOADING LOCAL DATA");
                inspectionDataImport.readInspectionData(this, dataManager.getDirectory_path()+dataManager.getInspection_filename(), 1);
            } else if (selectedRunmMode == RunMode.DEFAULT){
                System.out.println("LOADING DEFAULT DATA");
                inspectionDataImport.readInspectionData(this, null, 0);
            }else if (selectedRunmMode == RunMode.UPDATE){
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
        }

        //Update existing Restaurant Manager obj instance
        restaurantManager = RestaurantManager.getInstance();
        restaurantManager.setRestaurants(restaurantList);
    }

    public void startMapActivity(){
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }


    public class backgroundTask extends AsyncTask<String,Integer,Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("WrongThread")
        @Override
        protected Integer doInBackground(String... params) {
            int count = 0;
            for (int i =0; i<params.length;i++){
                System.out.println("instructions: "+params[i]);

                switch (params[i]){
                    case "selectRunMode":
                        try {
                            LoadingActivity.this.selectRunMode();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        count = 0;
                        break;
                    case "startMapActivity":
                        LoadingActivity.this.startMapActivity();
                        count = 1;
                        break;
                    case "setRestaurantManager":
                        setRestaurantManager(RunMode.LOCAL);
                        break;
                    default:
                        Log.wtf("Loading Activity","Invalid instruction name");
                        break;
                }
                publishProgress(count);
            }
            System.out.println("current runmode: "+runMode);
            return count;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            System.out.println("Progress: "+values[0].toString());

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer==1) setRestaurantManager(RunMode.LOCAL);
            if (integer==0&&runMode!=RunMode.UPDATE) setRestaurantManager(runMode);
            //if (values[0]==2) startMapActivity();
        }
    }
    public void showDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("A NEW UPDATE FOUND")
                .setCancelable(false)
                .setMessage("Download Update?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO: Try if you can find a way to schedule setRestaurantManager(RunMode.UPDATE)&startMapActivity(),
                        // they have to run in a serial
                        setRestaurantManager(RunMode.UPDATE);
                        while (true){

                            File DummyFile = new File(dataManager.getDirectory_path()+dataManager.getInspection_filename());
                            if (DummyFile.length()>=2289280){
                                break;
                            }
                        }
                        setRestaurantManager(RunMode.LOCAL);
                        startMapActivity();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dataStatus==DataStatus.NOT_EXIST)setRestaurantManager(RunMode.DEFAULT);
                        else setRestaurantManager(RunMode.LOCAL);
                        startMapActivity();
                    }
                });
        alertDialog.show();
        alertVisible = false;
    }

}
