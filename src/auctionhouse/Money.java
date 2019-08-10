/**
 * 
 */
package auctionhouse;

/**
 * @author pbj
 */

/**
 *	Class used for holding and updating information about money
 *	<p>
 *	The following class has one field and several methods that execute operations on money,
 *	such as adding, comparing, subtracting etc.		 		
  */
public class Money implements Comparable<Money> {
	
   /**
	* Field holding information about the value of Money object.
	* <p>
	* This field is the amount of money in pounds that a given Money
	* object is worth.
	*/
    private double value;
    
    /**
     * Round to nearest pence
     * <p>
     * The method takes a value in pounds(of type double), and returns this value 
     * converted to pence(as a long type); rounded to 0 decimal places, according to rounding convention. 
     * @param pounds the amount in pounds
     * @return 		 rounded value converted to the nearest pence
     */
    private static long getNearestPence(double pounds) {
        return Math.round(pounds * 100.0);
    }
    
    /**
     * Convert back to pounds
     * <p>
     * Takes the value in pence computed by getNearestPence method and converts it back to 
     * bank friendly units (pounds followed by decimal point, followed by two figure pence amount).
     * @param pounds This is the rounded value in pence.
     * @return 		 rounded value in pence converted to pounds (pence are expressed as a fraction of pounds)
     */
    private static double normalise(double pounds) {
        return getNearestPence(pounds)/100.0;
        
    }
    
    /**
     * First (public) constructor of the Money class
     * <p>
     * This constructor creates a Money instance by initialising its value field 
     * with a normalised amount in pounds. 
     * @param pounds passed as a String
     */
    public Money(String pounds) {
        value = normalise(Double.parseDouble(pounds));
    }
    
    /**
     * Another (private) constructor of the class
     * <p>
     * This constructor also creates a Money object and initialises its value variable
     * by setting it to the exact value passed a parameter of type double.
     * @param pounds passed as a double
     */
    private Money(double pounds) {
        value = pounds;
    }
    
    /**
     * Update Money (add)
     * <p>
     * Takes an object of type Money and creates another object of type Money, and instantiates its value field.
     * Value field is set to the sum of the value field of the current object
     * and the value field of the object passed as a parameter.
     * @param m An object of type Money
     * @return 	a new object of type Money
     */
    public Money add(Money m) {
        return new Money(value + m.value);
    }
    
    /**
     * Update Money (subtract)
     * <p>
     * Takes an object of type Money and creates another object of type Money, and instantiates its value field.
     * Value field is set to the difference of the value field of the current object
     * and the value field of the object passed as a parameter.
     * @param m An object of type Money
     * @return 	a new object of type Money
     */
    public Money subtract(Money m) {
        return new Money(value - m.value);
    }
    
    /**
     * Update Money by some percentage
     * <p>
     * Takes a percentage value and creates a new Money object, setting its value field. It is set to the value field
     * of the current Money object increased by the percentage passed as a parameter of the method. The value in that
     * field is also normalised.
     * @param percent percentage expressed as a double
     * @return		  a new object of type Money
     */
    public Money addPercent(double percent) {
        return new Money(normalise(value * (1 + percent/100.0)));
    }
     
    @Override
    public String toString() {
        return String.format("%.2f", value);
        
    /**
     * Comparing two Money objects based on their value fields
     * <p>
     * This method takes an object of type Money as a parameter and compares its value field
     * with the value field of the current object. Then it returns a positive integer if the 
     * value of current object is greater, 0 if they are the same and a negative if a value of
     * object m is greater.
     * @param m an object of type money 
     * @return  an integer signifying which field was greater
     */ 
    }
    public int compareTo(Money m) {
        return Long.compare(getNearestPence(value),  getNearestPence(m.value)); 
    }
    
    /**
     * Checks if current Money object value field is less than or equal to another Money object value
     * <p>
     * The method takes an object of type Money and compares its corresponding value field 
     * to another Money object's value field. Returns a boolean regarding whether or not the 
     * first field is less than or equal to the second.
     * @param m the first Money object
     * @return 	boolean True if first is less than or equal to second; else False.
     */
    public Boolean lessEqual(Money m) {
        return compareTo(m) <= 0;
    }
    
    /**
     * Override of the standard object comparison method
     * <p>
     * Checks if the passed object is a Money object(if not returns false),
     * then it creates a new Money object from the parameter object, and finally compares their value fields.
     * If they are the same it returns True; else returns False.
     * @param o this is of type Object
     * @return 	returns a boolean according to description above
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money)) return false;
        Money oM = (Money) o;
        return compareTo(oM) == 0;       
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(getNearestPence(value));
    }
      

}
