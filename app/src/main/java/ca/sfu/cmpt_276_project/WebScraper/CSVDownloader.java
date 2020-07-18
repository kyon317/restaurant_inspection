package ca.sfu.cmpt_276_project.WebScraper;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;

public class CSVDownloader extends AsyncTask<String, String, String> {
    public static final int progress_bar_type = 0;
    WebScraper webScraper = new WebScraper();
    private ProgressDialog pDialog;
    private String filename = "";
    private String file_url = "https://data.surrey.ca/dataset/3c8cb648-0e80-4659-9078-ef4917b90ffb/resource/0e5d04a2-be9b-40fe-8de2-e88362ea916b/download/restaurants.csv";

    public void setFilename(String filename) {
        this.filename = filename;
    }
    //TODO:initialize pDialog and make modifications on progress bar
    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //this.showDialog(progress_bar_type);
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = connection.getContentLength();
            System.out.println("length: " + lenghtOfFile);
            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8139);
            System.out.println("input: " + input);
            System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
            System.out.println(Arrays.toString(Environment.getExternalStorageDirectory().listFiles()));
            // Output stream
            OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Download"
                    + "/" + filename);
            System.out.println("000");
            byte[] data = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        System.out.println("Progress: "+Integer.parseInt(progress[0]));
        //pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {
        System.out.println("DONE");
        // dismiss the dialog after the file was downloaded
        //dismissDialog(progress_bar_type);
    }
}

