package pl.edu.pjwstk.mteam.core;

/**
 * General interface used by TCPReader object for incoming messages handling. 
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public interface MessageListener{
	
	/**
	 * Stops the listener.
	 */
	public void start();
	/**
	 * Starts the listener.
	 */
	public void stop();
	/**
	 * Method invoked, when node receives a message.
	 * @param msg Raw buffer to be parsed.
	 * @return Underlying protocol dependent (f.e. for P2PP insert request indicates, 
	 * 		   whether to insert object into resource table or not). 
	 */
	boolean onDeliverMessage(byte[] msg);
	
	/**
	 * Method invoked before message forwarding.
	 * @param msg Raw buffer to be parsed.
	 * @return Value indicating whether to forward or discard the message.
	 */
	boolean onForwardingMessage(byte[] msg);
}
