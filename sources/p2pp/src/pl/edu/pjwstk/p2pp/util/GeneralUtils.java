package pl.edu.pjwstk.p2pp.util;

import java.util.GregorianCalendar;

/**
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class GeneralUtils {
	/**
	 * Returns current date as string.
	 * 
	 * @return
	 */
	public static String getCurrentDateAsString() {
		return new GregorianCalendar().getTime().toString();
	}

	/**
	 * Does the same as {@link #getCurrentDateAsString()} but returned string is surrounded by round brackets.
	 * 
	 * @return
	 */
	public static String getCurrentDateAsStringInBrackets() {
		return "(" + getCurrentDateAsString() + ")";
	}
}
