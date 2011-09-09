package pl.edu.pjwstk.p2pp.services;

import pl.edu.pjwstk.p2pp.OutgoingMessagesListener;
import pl.edu.pjwstk.p2pp.messages.Message;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;

/**
 * Abstract class describing service of P2PP (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public abstract class Service {

	/**
	 * Listener of outgoing messages. If a service wants to send a message, it should be passed to this object.
	 */
	private OutgoingMessagesListener outgoingListener;

	/**
	 * Content type identifying this service.
	 */
	protected byte contentType;

	/**
	 * Content subtype.
	 */
	protected byte contentSubtype;

	/**
	 * Constructor of service. Has to be used by subclasses.
	 * 
	 * @param contentType
	 *            Content type identifying this service.
	 * @param contentSubtype
	 */
	public Service(byte contentType, byte contentSubtype) {
		this.contentType = contentType;
		this.contentSubtype = contentSubtype;
	}

	/**
	 * Sets new listener that will handle outgoing messages.
	 * 
	 * @param listener
	 *            Listener of outgoing messages passed by this service.
	 * 
	 * @param listener
	 */
	public void setOutgoingListener(OutgoingMessagesListener listener) {
		this.outgoingListener = listener;
	}

	/**
	 * Passes given message to outgoing messages listener.
	 * 
	 * @param message
	 *            Message to be send.
	 */
	protected void fireOnSend(Message message) {
		outgoingListener.onSend(message);
	}

	/**
	 * Returns content type identifying this service.
	 * 
	 * @return
	 */
	public byte getContentType() {
		return contentType;
	}

	/**
	 * Returns content subtype identifying this service.
	 * 
	 * @return
	 */
	public byte getContentSubtype() {
		return contentSubtype;
	}

	/**
	 * Method invoked when this service receives time slot for doing things.
	 */
	public abstract void onTimeSlot();

	/**
	 * Method invoked when a message was received and is not for any P2PP entity. Returns true if given message was
	 * consumed (i.e. this service was interested in this message and done something with it) by this entity. False
	 * otherwise.
	 * 
	 * @param message
	 * @return
	 */
	public abstract boolean onReceive(Message message);

	/**
	 * Returns ResourceObject that describes this service. Returned object will be filled with new ResourceID object
	 * depending on overlay parameters and the way the implementer wanted services to be identified (described in
	 * chapter 5.1 of P2PP specification (draft 01)).
	 * 
	 * @return
	 */
	public abstract ResourceObject getResourceObject();

}
