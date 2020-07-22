package ca.sfu.cmpt_276_project.WebScraper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;

import ca.sfu.cmpt_276_project.R;

public class CSVDownloader extends AsyncTask<String, String, String> {
    private ProgressBar progressBar;
    private String filename = "";

    public CSVDownloader(String filename, Context context) {
        this.filename = filename;
        setPdialog(context);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setPdialog(Context context) {
        progressBar = ((Activity) context).findViewById(R.id.progressBar1);
    }
    //TODO:initialize pDialog and make modifications on progress bar

    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Set up progressBar visibility and details
/*        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);
        progressBar.setScrollBarStyle(ProgressBar.SCROLLBARS_OUTSIDE_INSET);
        progressBar.setMax(100);
        progressBar.setVisibility(View.VISIBLE);*/
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

            System.out.println("fetched url: " + url.toString());
            // this will be useful so that you can show a typical 0-100%
            // progress bar
            long lengthOfFile = getActualSize(connection);
            System.out.println("length: " + lengthOfFile);

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8121);
            //System.out.println("input: " + input);
            System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
            System.out.println(Arrays.toString(Environment.getExternalStorageDirectory().listFiles()));

            // Output stream
            OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Download/"
                    + filename);

            //System.out.println("Actual Length: "+getActualSize(f_url));
            byte[] data = new byte[1024];  //Decide bytes read per time

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //System.out.println("lengthofFile:" + lengthOfFile);
                publishProgress("" + (int) ((total * 100) / lengthOfFile));

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
        System.out.println("Progress: " + Integer.parseInt(progress[0]));
//        progressBar.setProgress(Integer.parseInt(progress[0]));
        //progressBar.setProgress(Integer.parseInt(progress[0])); this is where I'd update my progress bar
        //                                                          if I had the numbers for it
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {
        System.out.println("DONE Downloading");
        // dismiss the dialog after the file was downloaded
//        progressBar.setVisibility(View.GONE);
    }

    public long getActualSize(URLConnection connection) {
        String range = connection.getHeaderField("Content-Range");
        String[] dummy_string = range.split("\\/");
        long actualLength = 0;
        actualLength = Long.parseLong(dummy_string[1]);
        return actualLength;
    }
}

