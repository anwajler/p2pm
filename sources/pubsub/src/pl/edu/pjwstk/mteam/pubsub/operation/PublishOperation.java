package pl.edu.pjwstk.mteam.pubsub.operation;

import java.util.Vector;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;

/**
 * Class representing publish operation.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class PublishOperation extends Operation{
	 
	/**
	 * Creates new publish operation, adding set of events associated with it.
	 * Use this only for generating rules.
	 * @param oid Topic id.
	 */
	public PublishOperation(String oid){
		super(PubSubConstants.OPERATION_PUBLISH, oid, null, new Vector<Event>());
		addEvent(new Event(PubSubConstants.EVENT_ALL));
		addEvent(new Event(PubSubConstants.EVENT_REMOVETOPIC));
		addEvent(new Event(PubSubConstants.EVENT_MODIFYAC));
		addEvent(new Event(PubSubConstants.EVENT_CUSTOM));
	}
	
	/**
	 * Creates new publish operation marking, it is associated with specified
	 * event.
	 * @param oid Topic id.
	 * @param user Subscriber performing this operation.
	 * @param e Event, this operation is associated with.
	 */
	public PublishOperation(String oid, Subscriber user, Event e){
		super(PubSubConstants.OPERATION_PUBLISH, oid, user, e);
	}
}
