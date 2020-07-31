/*
 * Class: InspectionDataCSVIngester
 *
 * Class Description:
 * 1. The InspectionDataCSVIngester Class contains a .csv reader that fetches all types of violations from inspectionreports_itr1.csv file.
 *
 * Function:
 * 1. readInspectionData(Context context): Given the context, the function fetches data from inspectionreports_itr1.csv and interprets it into a List of InspectionData.[Done]
 * 2. returnInspectionByID(String id): Given the id, the function searches existing inspections in IngestionList and return a list of inspections, otherwise an empty list will be returned.[Done]
 *
 * */
package ca.sfu.cmpt_276_project.CsvIngester;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Type;
import ca.sfu.cmpt_276_project.Model.Violation;
import ca.sfu.cmpt_276_project.R;

public class InspectionDataCSVIngester {

    private List<InspectionData> IngestionList = new ArrayList<>();
    private ViolationTXTIngester violationTXTIngester = new ViolationTXTIngester();

    public static List<String> getText(InputStream inputStream) throws IOException {

        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                StandardCharsets.UTF_8));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            Log.wtf("Reading Activity", "Fatal Error when reading file on line");
            e.printStackTrace();
        }

        return lines;
    }

    //return a list of inspection by tracking number
    public List<InspectionData> returnInspectionByID(String id) {
        List<InspectionData> inspectionData = new ArrayList<>();
        for (InspectionData inspection : IngestionList
        ) {
            if (inspection.getTrackingNumber().equals(id)) {
                inspectionData.add(inspection);
            }
        }
        return inspectionData;
    }

    public void readInspectionData(Context context, String inputFilename, int updateCode) throws IOException, ParseException {
        //initializing violationList
        violationTXTIngester.readViolationData(context);
        InputStream InspectionCSV = context.getResources().openRawResource
                (R.raw.inspectionreports_itr1);
        if (updateCode == 1) {
            InspectionCSV.close();
            InspectionCSV = new FileInputStream(inputFilename);
        }
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

            if (fields.size() == 0) continue;  // Skip gibberish info
            InspectionData temp = new InspectionData();

            temp.setTrackingNumber(fields.get(0));
            temp.setInspectionDate(format.parse(fields.get(1)));

            //conditions for Inspection Type ENUM
            if (fields.get(2).equals("Routine"))
                temp.setInspectionType(Type.ROUTINE);
            else{
                temp.setInspectionType(Type.FOLLOW_UP);
                if (Locale.getDefault().getLanguage().equals("fr"))
                    temp.setInspectionType(Type.SUIVRE);
            }


            temp.setCriticalViolations(Integer.parseInt(fields.get(3)));
            temp.setNonCriticalViolations(Integer.parseInt(fields.get(4)));

            if (updateCode == 1) {
                List<Violation> dummy_violations = new ArrayList<>();
                Violation dummy_violation = new Violation();
                if (fields.size() == 5) {
                    temp.setHazard(Hazard.LOW);
                    temp.setViolation(dummy_violations);
                } else if (fields.size() > 5) {
                    String[] violationLump = fields.get(5).split("\\|");
                    for (String singleViolation : violationLump
                    ) {
                        String[] dataCache = singleViolation.split(",");
                        //System.out.println("ID: "+dataCache[0]);
                        dummy_violation = violationTXTIngester.returnViolationByID(dataCache[0]);
                        //dummyViolation.Display();
                        dummy_violations.add(dummy_violation);
                    }
                    temp.setViolation(dummy_violations);
                    if (fields.size() == 7) {
                        //conditions for Hazard ENUM
                        if (fields.get(6).equals("Low"))
                            temp.setHazard(Hazard.LOW);
                        else if (fields.get(6).equals("Moderate"))
                            temp.setHazard(Hazard.MEDIUM);
                        else
                            temp.setHazard(Hazard.HIGH);
                    } else {
                        if (temp.getCriticalViolations() >= 2) temp.setHazard(Hazard.HIGH);
                        else if (temp.getCriticalViolations() + temp.getNonCriticalViolations() >= 4)
                            temp.setHazard(Hazard.MEDIUM);
                        else temp.setHazard(Hazard.LOW);
                    }
                }
            } else {
                //conditions for Hazard ENUM
                if (fields.get(5).equals("Low"))
                    temp.setHazard(Hazard.LOW);
                else if (fields.get(5).equals("Moderate"))
                    temp.setHazard(Hazard.MEDIUM);
                else
                    temp.setHazard(Hazard.HIGH);

                //Violation parser
                List<Violation> dummy_violations = new ArrayList<>();
                Violation dummy_violation = new Violation();
                if (fields.size() == 6) {                          //skip if there's no violation
                    dummy_violations.add(dummy_violation);
                    temp.setViolation(dummy_violations);

                } else if (fields.get(6) != null) {
                    String[] violationLump = fields.get(6).split("\\|");
                    for (String singleViolation : violationLump
                    ) {
                        String[] dataCache = singleViolation.split(",");
                        //System.out.println("ID: "+dataCache[0]);
                        dummy_violation = violationTXTIngester.returnViolationByID(dataCache[0]);
                        //dummyViolation.Display();
                        dummy_violations.add(dummy_violation);
                    }
                    temp.setViolation(dummy_violations);
                }
            }

            IngestionList.add(temp);
        }//end of scan loop

    }
}
