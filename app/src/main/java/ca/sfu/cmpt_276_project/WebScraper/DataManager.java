package ca.sfu.cmpt_276_project.WebScraper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class DataManager extends Activity {
    private final static String restaurant_url = "http://data.surrey.ca/api/3/action/package_show?id=restaurants";
    private final static String inspection_url = "http://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";
    private final static String restaurant_filename = "restaurants_itr1.csv";
    private final static String restaurant_update_date_local = "restaurants_date_local.txt";
    private final static String inspection_filename = "inspectionreports_itr2.csv";
    private final static String inspection_update_date_local = "inspectionreports_date_local.txt";
    private final static String directory_path = android.os.Environment.getExternalStorageDirectory() + "/Download/";
    private String restaurant_csv_url = "";
    private String inspection_csv_url = "";
    private Date restaurant_latest_update = null;
    private Date inspection_latest_update = null;

    public boolean checkForUpdates(Context context) throws ExecutionException, InterruptedException, ParseException, IOException {
        boolean isUpdate = false;
        if (!(checkFileExistence(restaurant_filename) && checkFileExistence(inspection_filename))) {
            isUpdate = createDialog(context, "No local data found", "Data");
        } else {
            restaurant_latest_update = readLocalDate(restaurant_update_date_local);
            inspection_latest_update = readLocalDate(inspection_update_date_local);
            WebScraper restaurantData = new WebScraper();
            String fetched_res_date = restaurantData.execute(restaurant_url).get()[1];
            Date restaurant_date_on_server;
            restaurant_date_on_server = dateParser(fetched_res_date);
            System.out.println("restaurant_date_on_server:" + restaurant_date_on_server);

            WebScraper inspectionData = new WebScraper();
            String fetched_ins_date = inspectionData.execute(inspection_url).get()[1];
            Date inspection_date_on_server;
            inspection_date_on_server = dateParser(fetched_ins_date);
            System.out.println("inspection_latest_update:" + inspection_date_on_server);

            //To test it with older data on server, change before to after
            if (restaurant_latest_update.before(restaurant_date_on_server)&&inspection_latest_update.before(inspection_date_on_server)){
                isUpdate = createDialog(context, "A new update found", "Data");
            }else {
                if (restaurant_latest_update.before(restaurant_date_on_server)) {
                    isUpdate = createDialog(context, "A new update found", "Restaurant List");
                }
                if (inspection_latest_update.before(inspection_date_on_server)) {
                    isUpdate = createDialog(context, "A new update found", "Inspection Data");
                }
            }
        }
        return isUpdate;
    }

    public boolean createDialog(Context context, String title, String msg) {
        final boolean[] isUpdate = {true};
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage("Download " + msg + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (msg.equals("Restaurant List")) {
                            try {
                                downloadList(context, restaurant_url, restaurant_filename, restaurant_csv_url);
                            } catch (ExecutionException | InterruptedException | IOException e) {
                                e.printStackTrace();
                            }
                        }else if (msg.equals("Inspection Data")){
                            try {
                                downloadList(context,inspection_url,inspection_filename,inspection_csv_url);
                            } catch (ExecutionException | InterruptedException | IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                downloadAll(context);
                            } catch (ExecutionException | InterruptedException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isUpdate[0] = false;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return isUpdate[0];
    }

    public boolean checkFileExistence(String filename){
        File dummyFile = new File(directory_path+filename);
        return dummyFile.exists();
    }

    public void downloadAll(Context context) throws ExecutionException, InterruptedException, IOException {
        downloadList(context,restaurant_url,restaurant_filename,restaurant_csv_url);
        downloadList(context,inspection_url,inspection_filename,inspection_csv_url);
    }

    public void downloadList(Context context,String url,String filename,String csv_url) throws ExecutionException, InterruptedException, IOException {
        WebScraper web_data = new WebScraper();
        String[] result = web_data.execute(url).get();
        csv_url = result[0];
        System.out.println("csvurl:" + url);
        System.out.println("downloading " + filename);
        CSVDownloader restaurantData_downloader = new CSVDownloader(filename, context);
        restaurantData_downloader.execute(csv_url);
        Date currentTimeStamp = new Date();
        currentTimeStamp.getTime();
        if (filename.equals(restaurant_filename)){
            updateLocalDate(restaurant_update_date_local,currentTimeStamp.toString());
        }else{
            updateLocalDate(inspection_update_date_local,currentTimeStamp.toString());
        }
    }

    public void updateLocalDate(String filename,String data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(directory_path+filename));
        writer.write(data);
        writer.close();
        System.out.println("current_time_stamp_saved: "+ data);
    }

    public Date dateParser(String date) throws ParseException {
        date = date.replace("T"," ");
        String[] dummydate = date.split("\\." );
        Date result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dummydate[0]);
        return result;
    }

    public Date readLocalDate(String filename) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader(directory_path + filename));
        String input = reader.readLine();
        Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(input);
        System.out.println(input);
        System.out.println(date);
        reader.close();
        return date;
    }

    /**
     * Getters
     */

    public String getRestaurant_filename() {
        return directory_path + restaurant_filename;
    }

    public String getInspection_filename() {
        return directory_path + inspection_filename;
    }

    public Date getRestaurant_latest_update() {
        return restaurant_latest_update;
    }

    public Date getInspection_latest_update() {
        return inspection_latest_update;
    }

    public String getDirectory_path() {
        return directory_path;
    }
}
