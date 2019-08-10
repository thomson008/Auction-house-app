/**
 * 
 */
package auctionhouse;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author pbj
 *
 */
public class MoneyTest {

    @Test    
    public void testAdd() {
        Money val1 = new Money("12.34");
        Money val2 = new Money("0.66");
        Money result = val1.add(val2);
        assertEquals("13.00", result.toString());
    }

    /*
     ***********************************************************************
     * BEGIN MODIFICATION AREA
     ***********************************************************************
     * Add all your JUnit tests for the Money class below.
     */
   @Test
   public void  testSubtract() {
	   Money val1 = new Money("15.45");
	   Money val2 = new Money("12.30");
       Money result = val1.subtract(val2);
       assertEquals("3.15", result.toString());
   }
   
   @Test
   public void testAddPercent() {
	   Money val = new Money("17.95");
	   double percentage = 10;
	   Money result = val.addPercent(percentage);
	   assertEquals("19.75", result.toString());
   }
   
   @Test
   public void testCompareTo() {
	   Money val1 = new Money("15.45");
	   Money val2 = new Money("12.30");
	   int comp1 = val1.compareTo(val2);
	   assertEquals(1, comp1);
	   
	   Money val3 = new Money("6.57");
	   Money val4 = new Money("6.571");
	   int comp2 = val3.compareTo(val4);
	   assertEquals(0, comp2);
	   
	   Money val5 = new Money("12.30");
	   Money val6 = new Money("12.35");
	   int comp3 = val5.compareTo(val6);
	   assertEquals(-1, comp3);
   }
   
   @Test
   public void  testLessEqual() {
	   Money val1 = new Money("15.45");
	   Money val2 = new Money("12.30");
	   boolean comp1 =  val1.lessEqual(val2);
	   assertEquals(false, comp1);
	   
	   Money val3 = new Money("6.57");
	   Money val4 = new Money("6.571");
	   boolean comp2 =  val3.lessEqual(val4);
	   assertEquals(true, comp2);
	   
	   Money val5 = new Money("12.30");
	   Money val6 = new Money("12.35");
	   boolean comp3 =  val5.lessEqual(val6);
	   assertEquals(true, comp3);
   }
   
   @Test
   public void testEquals() {
	   Money val1 = new Money("1256453");
	   String s = "1256453";
	   boolean test1 = val1.equals(s);
	   assertEquals(false, test1);
	   
	   Money val2 = new Money("12532");
	   boolean test2 = val1.equals(val2);
	   assertEquals(false, test2);
	   
	   Money val3 = new Money("1256453");
	   boolean test3 = val1.equals(val3);
	   assertEquals(true, test3);
	   
   }


    /*
     * Put all class modifications above.
     ***********************************************************************
     * END MODIFICATION AREA
     ***********************************************************************
     */


}
