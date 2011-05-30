package pl.edu.pjwstk.p2pp;

import pl.edu.pjwstk.p2pp.messages.Message;

/**
 * Interface for listeners of outgoing messages.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public interface OutgoingMessagesListener {

	/**
	 * Method invoked when invoker of this method wants to send given message.
	 * 
	 * @param message
	 */
	public void onSend(Message message);

}
