package ca.sfu.cmpt_276_project.WebScraper;


import android.app.ProgressDialog;
import android.content.Context;
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

public class WebScraper extends AsyncTask<String,String,String>{
    private ProgressDialog pd;
    private final static String reportCsvName = "Fraser Health Restaurant Inspection Reports";
    public void setPd(Context context){
        pd = new ProgressDialog(context);
    }
    public void executeUrlRequest(String url){
        new WebScraper().execute(url);
    }

    //Show progress
    protected void onPreExecute(Context context){
        super.onPreExecute();
        pd.setMessage("Please Wait");
        pd.setCancelable(false);
        pd.show();
    }
    //Background data fetching
    @Override
    protected String doInBackground(String... params) {
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
            while ((line = reader.readLine())!=null){
                buffer.append(line+"\n");
                //Log.d("Response: ",">"+line); //Whole response
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connection!=null){
                connection.disconnect();
            }
            if (reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (pd.isShowing()){
            pd.dismiss();
        }
        try {
            JSONObject rawJsonObj = returnJSONObject(result).getJSONObject("result");
            JSONArray resources = rawJsonObj.getJSONArray("resources");
            returnCsvUrl(resources,reportCsvName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public String returnCsvUrl(JSONArray jsonArray,String urlNameToFind) throws JSONException {
        String csvUrl = "";
        for (int i = 0;i<jsonArray.length();i++){
            if (urlNameToFind.equals(jsonArray.getJSONObject(i).getString("name")) && jsonArray.getJSONObject(i).getString("format").equals("CSV")){
                csvUrl = jsonArray.getJSONObject(i).getString("url");
            }
        }
        csvUrl.replaceAll("/","");//clear format
        System.out.println("csvURL: "+csvUrl);
        return csvUrl;
    }
    public JSONObject returnJSONObject(String result) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject;
    }
}
