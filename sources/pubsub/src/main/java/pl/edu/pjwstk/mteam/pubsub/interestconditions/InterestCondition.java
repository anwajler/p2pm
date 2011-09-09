package pl.edu.pjwstk.mteam.pubsub.interestconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import pl.edu.pjwstk.mteam.pubsub.core.Event;
import pl.edu.pjwstk.mteam.pubsub.core.Operation;
import pl.edu.pjwstk.mteam.pubsub.core.PubSubConstants;
import pl.edu.pjwstk.mteam.pubsub.core.Rule;
import pl.edu.pjwstk.mteam.pubsub.core.User;

/**
 * Class representing single interest condition.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class InterestCondition extends Rule {

	/**
	 * Creates new interest condition rule
	 * @param o Operation this rule is defined for.
	 */
	public InterestCondition(Operation o) {
		super(o);
	}

	/**
	 * @param eventType User is not allowed to refuse receiving information about 
	 *                  topic removal or access control rules modification from
	 *                  specified set of other subscribers. If this parameter is
	 *                  {@link PubSubConstants#EVENT_REMOVETOPIC} or {@link PubSubConstants#EVENT_MODIFYAC}, operation will 
	 *                  fail.
	 * @param modificationType
	 * @param user 
	 */
	protected boolean isModificationAllowed(byte eventType, byte modificationType, User user){
		boolean result = true;
		if(eventType != PubSubConstants.EVENT_CUSTOM){
			/* user is not allowed to refuse receiving information about topic removal
			 * or AC rules modification (so user list for ALL event also cannot be 
			 * modified) 
			 */
			result = false;
		}
		return result;
	}

	/**
	 * Checks whether specified operation is interesting.
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
			if(ulist.size() == 0 || ulist.contains(o.getUser())){
				//check event-specific rule
				ArrayList<Event> elist = new ArrayList<Event>(o.getEvents());
				Collection<User> uelist = getUsers(elist.get(0).getType());
				if(uelist != null){
					if(uelist.size() == 0 || uelist.contains(o.getUser())){
						result = true;
					}
				}
			}
		}
		return result;
	}
	
	public String toString(){
		String result = "Operation type: "+PubSubConstants.STR_OPERATION[getType()]+"\n";
		Collection<Event> events = getOperation().getEvents();
		Iterator<Event> it = events.iterator();
		while(it.hasNext()){
			Event e = it.next();
			result += "\tUser list ("+PubSubConstants.STR_EVENT[e.getType()]+"): ";
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
