package pl.edu.pjwstk.p2pp;

/**
 * Class with global constants for P2PP project. It was created just for purpose of filling places where this
 * implementation couldn't handle in the first place.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class GlobalConstants {

	/** Address of this peer. FIXME not used anymore */
	public static byte[] peerAddress = new byte[] { 89, 79, (byte) 235, 72 };

	/** Port of this peer. */
	public static int peerPort = 8998;

	/** True if the communication is over reliable transport. */
	public static boolean isOverReliable = false;

	/** True if the communication is encrypted. */
	public static boolean isEncrypted = false;

	/** True if parallel lookups should be used. */
	public static boolean parallelLookupsEnabled = false;

}
