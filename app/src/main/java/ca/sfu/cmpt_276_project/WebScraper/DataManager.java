package ca.sfu.cmpt_276_project.WebScraper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;

public class DataManager extends Activity {

    private final static String restaurant_url = "http://data.surrey.ca/api/3/action/package_show?id=restaurants";
    private final static String inspection_url = "http://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";
    private final static String restaurant_filename = "restaurants_itr1.csv";
    private final static String inspection_filename = "inspectionreports_itr2.csv";
    private String restaurant_csv_url = "";
    private String inspection_csv_url = "";


    public void downloadAll(Context context) throws ExecutionException, InterruptedException {
        WebScraper restaurantData = new WebScraper();
        restaurant_csv_url = restaurantData.execute(restaurant_url).get();
        System.out.println("csvurl:" + restaurant_csv_url);
        WebScraper inspectionData = new WebScraper();
        inspection_csv_url = inspectionData.execute(inspection_url).get();
        System.out.println("csvurl:" + inspection_csv_url);

        System.out.println("downloading " + restaurant_filename);
        CSVDownloader restaurantData_downloader = new CSVDownloader(restaurant_filename, context);
        restaurantData_downloader.execute(restaurant_csv_url);
        System.out.println("downloading " + inspection_filename);
        CSVDownloader inspectionData_downloader = new CSVDownloader(inspection_filename, context); // This process takes a while
        inspectionData_downloader.execute(inspection_csv_url);
    }
    /**
     * Use ACCESS_CODE to access different files
     * 0: restaurant_list
     * 1: inspection_list
     * */
    public InputStream returnStream(Context context, int ACCESS_CODE) throws FileNotFoundException {
        InputStream inputStream = null;
        File inputFile = null;
        if (ACCESS_CODE == 0) {
            inputFile = new File(android.os.Environment.getExternalStorageDirectory() + "/Download/"+restaurant_filename);
        }
        if (ACCESS_CODE == 1){
            inputFile = new File(android.os.Environment.getExternalStorageDirectory() + "/Download/"+inspection_filename);
        }
        if (inputFile!=null){
            inputStream = new FileInputStream(inputFile);
        }else{
            Log.wtf("Reading from External Storage","Fatal Error: File Doesn't exist!");
        }
        return inputStream;
    }

    public void updateDataBase(Context context) throws IOException, ParseException {
        InputStream restaurantInputStream = returnStream(context,0);
        InputStream inspectionInputStream = returnStream(context,1);
        List<Restaurant> restaurantList;

        RestaurantCSVIngester restaurantCSVIngester = new RestaurantCSVIngester();
        restaurantCSVIngester.readRestaurantList(context, restaurantInputStream, 1);

        restaurantList = restaurantCSVIngester.getRestaurantList();

        System.out.println("size of list: "+restaurantList.size());
        System.out.println("print last restaurant: ");
        restaurantList.get(restaurantList.size()-1).Display();

        InspectionDataCSVIngester inspectionDataCSVIngester = new InspectionDataCSVIngester();
        inspectionDataCSVIngester.readInspectionData(context,inspectionInputStream,1);
        for (Restaurant res:restaurantList
             ) {
            //TODO: get data from inspectionDataCSVIngester, the InputStream is provided above.
            //res.setInspectionDataList(inspectionDataCSVIngester.returnInspectionByID(res.getTrackNumber()));
        }
//        List<InspectionData>inspectionDataList =  inspectionDataCSVIngester.returnInspectionByID("SWOD-APSP3X");
//        inspectionDataList.get(0).Display();
//        System.out.println("print last restaurant with inspections: ");
//        restaurantList.get(restaurantList.size()-1).Display();
    }
}
