/*
 * A generic web scraper, given an URL it will decode the json file, return a csv URL for csv data
 * */
package ca.sfu.cmpt_276_project.WebScraper;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WebScraper extends AsyncTask<String, String, String[]> {
    private final static String INSPECTION_LIST_NAME = "Fraser Health Restaurant Inspection Reports";
    private final static String RESTAURANT_LIST_NAME = "Restaurants";
    private static String CSV_url = "";

    //Background data fetching
    @Override
    protected String[] doInBackground(String... params) {
        String[] result = new String[2];
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                //Log.d("Response: ",">"+line); //Whole response
            }
            JSONObject rawJsonObj = returnJSONObject(buffer.toString()).getJSONObject("result");
            JSONArray resources = rawJsonObj.getJSONArray("resources");
            setCSV_url(returnCsvUrl(resources));
            String date = returnLastModifiedDate(resources);
            Log.d("WebScraper","result_csv:"+CSV_url);
            result[0] = CSV_url;
            result[1] = date;
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        //System.out.println("Result: "+result);
    }

    public String returnCsvUrl(JSONArray jsonArray) throws JSONException {

        String csvUrl = "";
        for (int i = 0; i < jsonArray.length(); i++) {
            if (RESTAURANT_LIST_NAME.equals(jsonArray.getJSONObject(i).getString("name")) || INSPECTION_LIST_NAME.equals(jsonArray.getJSONObject(i).getString("name"))) {
                if (jsonArray.getJSONObject(i).getString("format").equals("CSV")) {
                    csvUrl = jsonArray.getJSONObject(i).getString("url");
                }
            }
        }
        csvUrl.replaceAll("/", "");//clear format
        //System.out.println("csvURL: "+csvUrl);  // For testing
        setCSV_url(csvUrl);
        return csvUrl;
    }

    public String returnLastModifiedDate(JSONArray jsonArray) throws JSONException {
        String date = "";
        for (int i = 0; i < jsonArray.length(); i++) {
            if (RESTAURANT_LIST_NAME.equals(jsonArray.getJSONObject(i).getString("name")) || INSPECTION_LIST_NAME.equals(jsonArray.getJSONObject(i).getString("name"))) {
                if (jsonArray.getJSONObject(i).getString("format").equals("CSV")) {
                    date = jsonArray.getJSONObject(i).getString("last_modified");
                }
            }
        }

        //System.out.println("date: "+date);  // For testing
        return date;
    }
    public JSONObject returnJSONObject(String result) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject;
    }

    public String getCSV_url() {
        return CSV_url;
    }

    public static void setCSV_url(String url) {
        CSV_url = url;
    }

}
