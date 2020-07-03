package ca.sfu.cmpt_276_project.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadCSV {

    private List<List<String>> values = new ArrayList<>();
    private static final String COMMA_SEPARATOR = ",";

    public ReadCSV(InputStream is) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8")))) {

            String line = "";

            while ((line = reader.readLine()) != null) {

                //Split by ','
                String[] tokens = line.split(",");
                values.add(Arrays.asList(tokens));

            }



        } catch (FileNotFoundException e) {

            Log.wtf("ReadCSV", "Error reading data", e);
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getVal(int row, int col) {
        return values.get(row).get(col);
    }

    public int getRowSize() {
        return values.size();
    }

    public int getColSize(int row) {
        return values.get(row).size();
    }
}
