/*Download CSV from server
* */
package ca.sfu.cmpt_276_project.WebScraper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;

import ca.sfu.cmpt_276_project.UI.LoadingActivity;

import ca.sfu.cmpt_276_project.R;

public class CSVDownloader extends AsyncTask<String, String, String> {
//    private ProgressDialog progreassDiag;
    private String filename = "";
    private Context context;
    public CSVDownloader(String filename, Context context) {
        this.filename = filename;
        //setPdialog(context);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

//    public void setPdialog(Context context) {
//        progreassDiag = new ProgressDialog(context);
//        this.context = context;
//    }
    public boolean checkFileExistence(String filename){
        File dummyFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/"+filename);
        return dummyFile.exists();
    }

    public void backupFile(String filename){
        File dummyFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/"+filename);
        dummyFile.renameTo(new File(Environment.getExternalStorageDirectory().getPath() + "/Download/" + filename + "(1)"));
    }
    public void restoreFile(String backupFilename){
        if (checkFileExistence(backupFilename)){
            File dummyFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/"+backupFilename);
            dummyFile.renameTo(new File(Environment.getExternalStorageDirectory().getPath() + "/Download/" + filename));
        }
    }
    public void deleteFile(String filename){
        if (checkFileExistence(filename)){
            File dummyFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/"+filename);
            dummyFile.delete();
        }
    }
    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Set up progressBar visibility and details
        System.out.println(filename);
        if (checkFileExistence(filename)){
            backupFile(filename);
        }

//        progreassDiag.setTitle(R.string.Hold_on);
//        progreassDiag.setIndeterminate(false);
//        progreassDiag.setProgress(0);
//        progreassDiag.setCancelable(false);
//        progreassDiag.setMax(100);

//        progreassDiag.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        CSVDownloader.this.cancel(true);
//                        deleteFile(filename);//TODO: correctly delete file
//                        restoreFile(filename+"(1)");
//                        progreassDiag.cancel();
//                        progreassDiag.dismiss();
//                    }
//                });
//        progreassDiag.setCanceledOnTouchOutside(false);
//        progreassDiag.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.Done), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                progreassDiag.dismiss();
//            }
//        });
//        progreassDiag.show();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
        //progreassDiag.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        int count;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            System.out.println("fetched url: " + url.toString());
            // progress bar
            long lengthOfFile = getActualSize(connection);
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

            while ((count = input.read(data)) != -1&&!CSVDownloader.this.isCancelled()) {
                total += count;
                // publishing the progress....
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
        //progreassDiag.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {
//        progreassDiag.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//        progreassDiag.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
        System.out.println("DONE Downloading");
        // dismiss the dialog after the file was downloaded
        //progressBar.setVisibility(View.GONE);
    }

    public long getActualSize(URLConnection connection) {
        String range = connection.getHeaderField("Content-Range");
        String[] dummy_string = range.split("\\/");
        long actualLength = 0;
        actualLength = Long.parseLong(dummy_string[1]);
        return actualLength;
    }
}

