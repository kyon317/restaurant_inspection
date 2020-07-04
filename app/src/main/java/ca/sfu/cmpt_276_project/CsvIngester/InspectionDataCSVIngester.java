package ca.sfu.cmpt_276_project.CsvIngester;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Type;
import ca.sfu.cmpt_276_project.R;

public class InspectionDataCSVIngester {

    private List<InspectionData> IngestionList = new ArrayList<>();

    public void readInspectionData(Context context) throws IOException, ParseException {
        InputStream InspectionCSV = context.getResources().openRawResource
                (R.raw.inspectionreports_itr1);

        List<String> fileContents = getText(InspectionCSV);
        boolean firstLineUnread = true;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        for (String line : fileContents) {
            // Skip over the heading line
            if (firstLineUnread) {
                firstLineUnread = false;
                continue;
            }

            List<String> fields = Arrays.asList(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
            fields = fields
                    .stream()
                    .map(String::trim)
                    .map(s -> s.replace("\"", ""))  // Remove double quotes
                    .collect(Collectors.toList());  // Cool functional stuff.

            InspectionData temp = new InspectionData();

            temp.setTrackingNumber(fields.get(0));
            temp.setInspectionDate(format.parse(fields.get(1)));

            //conditions for Inspection Type ENUM
            if(fields.get(2) == "Routine")
                temp.setInspectionType(Type.ROUTINE);
            else
                temp.setInspectionType(Type.FOLLOW_UP);

            temp.setCriticalViolations(Integer.parseInt(fields.get(3)));
            temp.setNonCriticalViolations(Integer.parseInt(fields.get(4)));

            //conditions for Hazard ENUM

            if(fields.get(5) == "Low")
                temp.setHazard(Hazard.LOW);
            else if(fields.get(5) == "Moderate")
                temp.setHazard(Hazard.MEDIUM);
            else
                temp.setHazard(Hazard.HIGH);

            //TODO: need a Violation parser

            IngestionList.add(temp);
        }//end of scan loop

        for(InspectionData inspectionData : IngestionList){
            inspectionData.Display();
        }
    }

    public static List<String> getText(InputStream inputStream) throws IOException{

        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName("UTF-8")));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            Log.wtf("Reading Activity","Fatal Error when reading file on line");
            e.printStackTrace();
        }

        return lines;
    }
}
