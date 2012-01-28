package pl.edu.pjwstk.mteam.pubsub.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Rule;
import pl.edu.pjwstk.mteam.pubsub.core.Subscriber;
import pl.edu.pjwstk.mteam.pubsub.core.User;

/**
 * Class representing access control rule.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class AccessControlRule extends Rule{

	/**
	 * Creates new access control rule
	 * @param o Operation this rule is defined for.
	 */
	public AccessControlRule(Operation o){
		super(o);
	}
	
	/**
	 * Creates new access control rule received f.e. in publish-subscribe message.
	 * @param bytes Encoded AC rule.
	 */
	public AccessControlRule(byte[] bytes){
		super(bytes);
	}
	
	/**
	 * @param eventType
	 * @param modificationType
	 * @param user Must be a Subscriber object instance. Otherwise operation 
	 *             will fail.
	 */
	protected boolean isModificationAllowed(short eventType, byte modificationType, User user){
		boolean result = false;
		if(user instanceof Subscriber){
			result = true;
		}
		return result;
	}

	/**
	 * Checks whether specified operation can be performed.
	 * @param o
	 */
	protected boolean matches(Operation o){
		boolean result = false;
		Operation ruleOperation = getOperation();
		if(ruleOperation.equals(o)){
			/* this is the rule for this operation - processing information
			 * about particular events
			 */
			Collection<User> ulist = getUsers(PubSubConstants.EVENT_ALL);
			if(ulist.isEmpty() || ulist.contains(o.getUser())){
				//check event-specific rule
				ArrayList<Event> elist = new ArrayList<Event>(o.getEvents());
				Collection<User> uelist = getUsers(elist.get(0).getType());
				if(uelist != null){
					if(uelist.isEmpty() || uelist.contains(o.getUser())){
						result = true;
					}
				}
			}
		}
		return result;
	}
	
	public String toString(){
		String result = "Operation type: "+PubSubConstants.STR_OPERATION.get(getType())+"\n";
		Collection<Event> events = getOperation().getEvents();
		Iterator<Event> it = events.iterator();
		while(it.hasNext()){
			Event e = it.next();
			result += "\tUser list ("+PubSubConstants.STR_EVENT.get(e.getType())+"): ";
			Collection<User> ulist = getUsers().get(e.getType()).values();
			Iterator<User> uit = ulist.iterator();
			while(uit.hasNext()){
				result += uit.next()+"  ";
			}
			result += "\n";
		}
		return result; 
	}
}
