package pl.edu.pjwstk.p2pp.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pl.edu.pjwstk.p2pp.messages.Message;

/**
 * Class responsible for storing messages. Messages may be of two types, namely, got or sent. Each message is stored
 * with a time stamp containing information about a moment in which this message was received/sent.
 * 
 * @author szeldon
 * 
 */
public class MessageStorage {

	/**
	 * Map of received messages hold against time stamps.
	 */
	private Map<Long, Message> messagesReceived = new HashMap<Long, Message>();

	/**
	 * Map of sent messages hold againsta time stamps.
	 */
	private Map<Long, Message> messagesSent = new HashMap<Long, Message>();

	/**
	 * Adds given received message to this storage.
	 * 
	 * @param message
	 * @param timeStamp
	 */
	public void addReceivedMessage(Message message, long timeStamp) {
		messagesReceived.put(timeStamp, message);
	}

	/**
	 * Adds given sent message to this storage.
	 * 
	 * @param message
	 * @param timeStamp
	 */
	public void addSentMessage(Message message, long timeStamp) {
		messagesSent.put(timeStamp, message);
	}

	/**
	 * Returns a set that contains time stamps for received messages. They may be used for getting messages received at
	 * that time.
	 * 
	 * @return
	 */
	public Set<Long> getSetOfTimeStampsForReceivedMessages() {
		return messagesReceived.keySet();
	}

	/**
	 * Returns a set that contains time stamps for sent messages. They may be used for getting messages sent at that
	 * time.
	 * 
	 * @return
	 */
	public Set<Long> getSetOfTimeStampsForSentMessages() {
		return messagesSent.keySet();
	}

	/**
	 * Returns message received in given moment in time. Those moments may be
	 * 
	 * @param timeStamp
	 * @return
	 */
	public Message getReceivedMessage(long timeStamp) {
		return messagesReceived.get(timeStamp);
	}

	/**
	 * Returns message sent in given moment in time.
	 * 
	 * @param timeStamp
	 * @return
	 */
	public Message getSentMessage(long timeStamp) {
		return messagesSent.get(timeStamp);
	}

}
