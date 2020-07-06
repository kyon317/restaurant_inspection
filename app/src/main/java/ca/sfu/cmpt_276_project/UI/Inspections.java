package ca.sfu.cmpt_276_project.UI;

public class Inspections {

    public Inspections( String inspectDate,  String inspectType, int numCritical, int numNonCritical, String hazard, int hazardIcon) {
        this.inspectType = inspectType;
        this.inspectDate = inspectDate;
        this.numCritical = numCritical;
        this.numNonCritical = numNonCritical;
        this.hazard = hazard;
        this.hazardIcon = hazardIcon;
    }

    private String inspectType;
    private String inspectDate;
    private int numCritical;
    private int numNonCritical;

    private String hazard;
    private int hazardIcon;

    public String getInspectType() {
        return inspectType;
    }

    public void setInspectType(String inspectType) {
        this.inspectType = inspectType;
    }

    public String getInspectDate() {
        return inspectDate;
    }

    public void setInspectDate(String inspectDate) {
        this.inspectDate = inspectDate;
    }

    public int getNumCritical() {
        return numCritical;
    }

    public void setNumCritical(int numCritical) {
        this.numCritical = numCritical;
    }

    public int getNumNonCritical() {
        return numNonCritical;
    }

    public void setNumNonCritical(int numNonCritical) {
        this.numNonCritical = numNonCritical;
    }

    public String getHazard() {
        return hazard;
    }

    public void setHazard(String hazard) {
        this.hazard = hazard;
    }

    public int getHazardIcon() {
        return hazardIcon;
    }

    public void setHazardIcon(int hazardIcon) {
        this.hazardIcon = hazardIcon;
    }

}
