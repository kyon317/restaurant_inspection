package ca.sfu.cmpt_276_project.UI;

public class DummyViolations {

    private String shortDetail;
    private String longDetail;
    private int violationICon;
    private int violationLevelIcon;

    private String isCritical;

    public DummyViolations(String shortDetail, String longDetail, int violationICon, int violationLevelIcon, String isCritical) {
        this.shortDetail = shortDetail;
        this.longDetail = longDetail;
        this.violationICon = violationICon;
        this.violationLevelIcon = violationLevelIcon;
        this.isCritical = isCritical;
    }

    public String getShortDetail() {
        return shortDetail;
    }

    public void setShortDetail(String shortDetail) {
        this.shortDetail = shortDetail;
    }


    public String getLongDetail() {
        return longDetail;
    }

    public void setLongDetail(String longDetail) {
        this.longDetail = longDetail;
    }


    public int getViolationICon() {
        return violationICon;
    }

    public void setViolationICon(int violationICon) {
        this.violationICon = violationICon;
    }

    public int getViolationLevelIcon() {
        return violationLevelIcon;
    }

    public void setViolationLevelIcon(int violationLevelIcon) {
        this.violationLevelIcon = violationLevelIcon;
    }

    public String getIsCritical() {
        return isCritical;
    }

    public void setIsCritical(String isCritical) {
        this.isCritical = isCritical;
    }


}
