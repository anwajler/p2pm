package pl.edu.pjwstk.mteam.pubsub.operation;

import java.util.Vector;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;

/**
 * Class representing unsubscribe operation.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class UnsubscribeOperation extends Operation {
	
	/**
	 * Use this only for creating rules, as it does not set subscriber performing 
	 * operation (or invoke <code>setUser</code> method before performing operation). 
	 * @param oid
	 */
	public UnsubscribeOperation(String oid){
		super(PubSubConstants.OPERATION_UNSUBSCRIBE, oid, null, new Vector<Event>());
		addEvent(new Event(PubSubConstants.EVENT_ALL));
	}
	
	/**
	 * Creates new unsubscribe operation marking, it is associated with specified
	 * event. 
	 * @param oid Topic id.
	 * @param user Subscriber performing this operation.
	 * @param e Event, this operation is associated with. For 'unsubscribe' 
	 * 			it will be {@link PubSubConstants#EVENT_ALL} as this operation can't be divided
	 * 		    into several events.
	 */
	public UnsubscribeOperation(String oid, Subscriber user, Event e){
		super(PubSubConstants.OPERATION_UNSUBSCRIBE, oid, user, e);
	}
}
