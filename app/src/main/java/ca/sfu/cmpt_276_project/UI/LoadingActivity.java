/*Loading Activity loads data into RestaurantManager.
 * */

package ca.sfu.cmpt_276_project.UI;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import ca.sfu.cmpt_276_project.WebScraper.CSVDownloader;
import ca.sfu.cmpt_276_project.WebScraper.DataManager;
import ca.sfu.cmpt_276_project.WebScraper.DataStatus;
import ca.sfu.cmpt_276_project.WebScraper.RunMode;
import ca.sfu.cmpt_276_project.WebScraper.WebScraper;

public class LoadingActivity extends AppCompatActivity {

    private DataManager dataManager;
    private DataStatus dataStatus;
    private RunMode runMode;
    private RestaurantManager restaurantManager;
    private boolean alertVisible = false;
    private static final String TAG = "LoadingActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //READ/WRITE PERMISSION CODE: https://stackoverflow.com/a/51374641/8860660
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        setContentView(R.layout.activity_loading);
        getSupportActionBar().setTitle(R.string.surreyTitle);

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
            Toast.makeText(this,R.string.noNetwork,
                    Toast.LENGTH_LONG).show();

            if (dataManager.checkFileExistence(dataManager.getRestaurant_filename()) &&
                    dataManager.checkFileExistence(dataManager.getInspection_filename()))
                runMode = RunMode.LOCAL;
            else
                runMode = RunMode.DEFAULT;
        }
        else{
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

    public void setRestaurantManager(RunMode selectedRunMode){
        List<Restaurant> restaurantList = new ArrayList<>();
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
        try {
            if (selectedRunMode == RunMode.LOCAL) {
                System.out.println("LOADING LOCAL DATA");
                restaurantImport.readRestaurantList(this, dataManager.getDirectory_path()+dataManager.getRestaurant_filename(), 1);
                restaurantList = restaurantImport.getRestaurantList();
            } else if (selectedRunMode == RunMode.DEFAULT){
                System.out.println("LOADING DEFAULT DATA");
                restaurantImport.readRestaurantList(this,null, 0);
                restaurantList = restaurantImport.getRestaurantList();
            }else if (selectedRunMode == RunMode.UPDATE){
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
            if (selectedRunMode== RunMode.LOCAL) {
                System.out.println("LOADING LOCAL DATA");
                inspectionDataImport.readInspectionData(this, dataManager.getDirectory_path()+dataManager.getInspection_filename(), 1);
            } else if (selectedRunMode == RunMode.DEFAULT){
                System.out.println("LOADING DEFAULT DATA");
                inspectionDataImport.readInspectionData(this, null, 0);
            }else if (selectedRunMode == RunMode.UPDATE){
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
                    case "Download":
                        try {
                            dataManager.downloadAll(LoadingActivity.this);
                        } catch (ExecutionException | InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                        count = 2;
                        break;
                    case "Load":
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
        }
    }

    public void showDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.newUpdate)
                .setCancelable(false)
                .setMessage(R.string.downloadUpdate)
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CSVDownloader res_downloader = new CSVDownloader(dataManager.getRestaurant_filename(),LoadingActivity.this);
                        CSVDownloader ins_downloader = new CSVDownloader(dataManager.getInspection_filename(),LoadingActivity.this);
                        String[] res_csv = new String[2];
                        String[] ins_csv = new String[2];
                        try {
                            res_csv = new WebScraper().execute(dataManager.getRestaurant_url()).get();
                            ins_csv = new WebScraper().execute(dataManager.getInspection_url()).get();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        res_downloader.execute(res_csv[0]);
                        ins_downloader.execute(ins_csv[0]);
                        Date currentTimeStamp = new Date();
                        currentTimeStamp.getTime();
                        try {
                            dataManager.updateLocalDate("inspectionreports_date_local.txt",currentTimeStamp.toString());
                            dataManager.updateLocalDate("restaurants_date_local.txt",currentTimeStamp.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        backgroundTask loading_task = new backgroundTask();
                        loading_task.execute("Load");
                        backgroundTask startMap_task = new backgroundTask();
                        startMap_task.execute("startMapActivity");
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
