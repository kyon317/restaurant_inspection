package ca.sfu.cmpt_276_project.UI;

import java.util.ArrayList;

// Dummy object to work with listRestaurant layout

public class DummyRestaurants {

    private String name;
    private int icon;
    private String hazard;
    private int criticalViolationCount;
    private String mostRecentDate;

    private int hazardIcon;

    public DummyRestaurants(String name, int icon, int hazardIcon, String hazard, int criticalViolationCount, String mostRecentDate) {
        this.name = name;
        this.icon = icon;
        this.hazardIcon = hazardIcon;
        this.hazard = hazard;
        this.criticalViolationCount = criticalViolationCount;
        this.mostRecentDate = mostRecentDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getHazard() {
        return hazard;
    }

    public void setHazard(String hazard) {
        this.hazard = hazard;
    }

    public int getCriticalViolationCount() {
        return criticalViolationCount;
    }

    public void setCriticalViolationCount(int criticalViolationCount) {
        this.criticalViolationCount = criticalViolationCount;
    }

    public String getMostRecentDate() {
        return mostRecentDate;
    }

    public void setMostRecentDate(String mostRecentDate) {
        this.mostRecentDate = mostRecentDate;
    }


    public int getHazardIcon() {
        return hazardIcon;
    }

    public void setHazardIcon(int hazardIcon) {
        this.hazardIcon = hazardIcon;
    }
}
