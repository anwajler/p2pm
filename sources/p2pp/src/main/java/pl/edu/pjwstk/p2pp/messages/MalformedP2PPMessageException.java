package pl.edu.pjwstk.p2pp.messages;

/**
 * Exception thrown when operation was made on malformed P2PP message.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class MalformedP2PPMessageException extends Throwable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8744831704044623972L;

	public MalformedP2PPMessageException() {
		super();
	}

	public MalformedP2PPMessageException(String message) {
		super(message);
	}

}
