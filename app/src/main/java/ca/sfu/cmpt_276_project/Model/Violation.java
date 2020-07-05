/*
 * Class: Violation
 *
 * Data descriptions:
 * 1. violationNumber: A String Data type indicates its unique violation number
 * 2. isCritical: An Boolean Data type, which tells if a violation is critical
 * 3. description : A string Data type, which stores all the description of a violation
 * 4. isRepeat: An Boolean Data type, which tells if a violation is repeated
 *
 * Functions:
 * 1. Getters
 * 2. Setters
 * 3. Default & Non-Default constructor
 * 4. Display
 *
 * */
package ca.sfu.cmpt_276_project.Model;

public class Violation {
    private String violationNumber;
    private boolean isCritical;
    private String description;
    private boolean isRepeat;

    //Default Constructor
    public Violation() {
        this.violationNumber = null;
        this.isCritical = false;
        this.description = "";
        this.isRepeat = false;
    }

    //Non Default Constructor
    public Violation(String violationNumber, boolean isCritical, String description, boolean isRepeat) {
        this.violationNumber = violationNumber;
        this.isCritical = isCritical;
        this.description = description;
        this.isRepeat = isRepeat;
    }

    //Getters
    public String getViolationNumber() {
        return violationNumber;
    }
    public boolean isCritical() {
        return isCritical;
    }
    public String getDescription() {
        return description;
    }
    public boolean isRepeat() {
        return isRepeat;
    }

    //Setters
    public void setCritical(boolean critical) {
        isCritical = critical;
    }
    public void setViolationNumber(String violationNumber) {
        this.violationNumber = violationNumber;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    //Display
    //This is just for the use of test
    public void Display(){
        System.out.println("Violation number: "+ this.getViolationNumber()
                            +"\nDescription: "+this.getDescription()
                            +"\nCritical: "+this.isCritical()
                            +"\nRepeat: "+this.isRepeat()
                            +"\n.........................................");
    }
}
