/*
 * Class: InspectionData
 *
 * Data descriptions:
 * 1. trackingNumber: A String Data type indicates its unique tracking number
 * 2. inspectionDate: A Date Data type indicates its inspection date
 * 3. inspectionType: A enum Data type of two types - ROUTINE or FOLLOW_UP
 * 4. criticalViolations: An integer Data type indicates number of critical violations
 * 5. nonCriticalViolations: An integer Data type indicates number of non-critical violations
 * 6. hazard: A enum Data type of three types - LOW/MEDIUM/HIGH
 * 7. violations: A List of Violation Data type that includes information of all violations in a single inspection
 *
 * Functions:
 * 1. Getters
 * 2. Setters
 * 3. Default & Non-default constructor
 * 4. Display
 *
 * */
package ca.sfu.cmpt_276_project.Model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InspectionData implements Comparable<InspectionData> {
    private String trackingNumber;
    private Date inspectionDate;
    private Type inspectionType;
    private int criticalViolations;
    private int nonCriticalViolations;
    private Hazard hazard;
    private List<Violation> violations;


    //Default Constructor
    public InspectionData() {
        this.trackingNumber = null;
        Date dummy_date = new Date(1970 - 01 - 01);
        this.inspectionDate = dummy_date;
        this.inspectionType = Type.ROUTINE;
        this.criticalViolations = 0;
        this.nonCriticalViolations = 0;
        this.hazard = Hazard.LOW;
        this.violations = new ArrayList<>();
    }

    //Non Default Constructor
    public InspectionData(String trackingNumber, Date inspectionDate, Type inspectionType, int criticalViolations, int nonCriticalViolations, Hazard hazard, List<Violation> violations) {
        this.trackingNumber = trackingNumber;
        this.inspectionDate = inspectionDate;
        this.inspectionType = inspectionType;
        this.criticalViolations = criticalViolations;
        this.nonCriticalViolations = nonCriticalViolations;
        this.hazard = hazard;
        this.violations = violations;
    }

    //Getters
    public String getTrackingNumber() {
        return trackingNumber;
    }

    //Setters
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Date getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Date inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public Type getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(Type inspectionType) {
        this.inspectionType = inspectionType;
    }

    public int getCriticalViolations() {
        return criticalViolations;
    }

    public void setCriticalViolations(int criticalViolations) {
        this.criticalViolations = criticalViolations;
    }

    public int getNonCriticalViolations() {
        return nonCriticalViolations;
    }

    public void setNonCriticalViolations(int nonCriticalViolations) {
        this.nonCriticalViolations = nonCriticalViolations;
    }

    public Hazard getHazard() {
        return hazard;
    }

    public void setHazard(Hazard hazard) {
        this.hazard = hazard;
    }

    public List<Violation> getViolation() {
        return violations;
    }

    public void setViolation(List<Violation> violations) {
        this.violations = violations;
    }

    public long timeSinceInspection() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date currentDate = new Date();
        formatter.format(currentDate);
        long diffInMillies = Math.abs(this.inspectionDate.getTime() - currentDate.getTime());
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diffInDays;
    }

    public void Display() {
        System.out.println("Inspection Tracking Number: " + this.getTrackingNumber()
                + "\nInspection Date: " + this.getInspectionDate()
                + "\nInspection Type: " + this.getInspectionType()
                + "\nCritical Violation: " + this.getCriticalViolations()
                + "\nNon Critical Violation: " + this.getNonCriticalViolations()
                + "\nHazard: " + this.getHazard()
                + "\nViolation details: \n*****************************************\n");
        for (Violation violation : violations
        ) {
            violation.Display();
        }
    }

    @Override
    public int compareTo(InspectionData other) {
        return other.inspectionDate.compareTo(inspectionDate);  //return the latest
    }
}
