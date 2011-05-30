package pl.edu.pjwstk.p2pp;

/**
 * Interface encapsulating objects passed in errorCallback() method in {@link P2PPInterface}.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 * 
 */
public interface ErrorInterface {

	/**
	 * Returns value of an error object as Object.
	 * 
	 * @return
	 */
	public abstract Object getValue();

	public abstract void setValue(Object value);

}
