package pl.edu.pjwstk.mteam.pubsub.operation;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;

/**
 * Class representing keep-alive operation.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class KeepAliveOperation extends Operation {

	/**
	 * Creates new keep-alive operation. 
	 * @param id Topic id.
	 * @param user Subscriber performing this operation. It is irrelevant when
	 * 			   method is used for creating operation rule.
	 * @param e Event, this operation is associated with. For 'subscribe' 
	 * 			it will be {@link PubSubConstants#EVENT_ALL} as this operation can't be divided
	 * 		    into several events.
	 */
	public KeepAliveOperation(String id, Subscriber user, Event e){
		super(PubSubConstants.OPERATION_KEEPALIVE, id, user, e);
	}
}
