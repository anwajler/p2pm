package pl.edu.pjwstk.p2pp.objects;

/**
 * Exception that informs about a fact that this implementation can't handle some kind of GeneralObject.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class UnsupportedGeneralObjectException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5307429172467189356L;

	public UnsupportedGeneralObjectException() {
		super();
	}

	public UnsupportedGeneralObjectException(String info) {
		super(info);
	}
}
