package ca.sfu.cmpt_276_project;

import org.junit.Test;

import java.util.Date;
import java.util.List;

import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.Type;
import ca.sfu.cmpt_276_project.Model.Violation;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    //This is not the real test, just for quick testing some basic functionalities during implementation without interfering UI surfaces
    public void dummy_test() {
        Restaurant dummy_restaurant = new Restaurant();
        Violation dummy_violation = new Violation();
        InspectionData dummy_data = new InspectionData();

        dummy_violation.setCritical(true);
        dummy_violation.setDescription("Food poisoned, killed 30 customers and one cat.");
        dummy_violation.setViolationNumber("0001");
        dummy_violation.setRepeat(true);
        System.out.println("test 1: output violation ");
        dummy_violation.Display();

        dummy_data.setTrackingNumber("0001");
        dummy_data.setInspectionDate(new Date(1970 - 01 - 01));
        dummy_data.setInspectionType(Type.ROUTINE);
        dummy_data.setCriticalViolations(0);
        dummy_data.setNonCriticalViolations(0);
        dummy_data.setViolation((List<Violation>) dummy_violation);
        System.out.println("test 2: output inspection data ");
        dummy_data.Display();

        dummy_restaurant.setTrackNumber("0001");

        //dummy_restaurant.setInspectionData(dummy_data);
        dummy_restaurant.Display();
    }


}