package pl.edu.pjwstk.mteam.pubsub.operation;

import java.util.Vector;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;

/**
 * 
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class CreateTopicOperation extends Operation{
	
	public CreateTopicOperation(String oid){
		super(PubSubConstants.OPERATION_CREATETOPIC, oid, null, new Vector<Event>());
		addEvent(new Event(PubSubConstants.EVENT_ALL));
	}
	
	/**
	 * Creates new notify operation marking, it is associated with specified
	 * event.
	 * @param oid Topic id.
	 * @param user Subscriber performing this operation.
	 * @param e Event, this operation is associated with. Acceptable values: 
	 * 			{@link PubSubConstants#EVENT_ALL},
	 *          {@link PubSubConstants#EVENT_NEWTOPIC},
	 *          {@link PubSubConstants#EVENT_TRANSFERTOPIC}.
	 */
	public CreateTopicOperation(String oid, Subscriber user, Event e){
		super(PubSubConstants.OPERATION_CREATETOPIC, oid, user, e);
	}
	
}
