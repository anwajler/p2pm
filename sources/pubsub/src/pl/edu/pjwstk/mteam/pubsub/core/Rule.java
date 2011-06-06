package pl.edu.pjwstk.mteam.pubsub.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Class representing single rule. May be used for creating constraints for
 * user-defined operation.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public abstract class Rule{
	protected static final byte MODIFICATION_ADDUSER = 0;
	protected static final byte MODIFICATION_REMOVEUSER = 1;
	
	/**
	 * Operation this rule is associated with.
	 */
	private Operation operation;
	/**
	 * List of users which are f.e. allowed (or not allowed) to perform 
	 * specified operation.
	 */
	private Hashtable<Byte, Hashtable<String, User>> users;
	
	/**
	 * Creates new rule.
	 * @param o Operation, this rule is associated with.
	 */
	public Rule(Operation o){
		operation = o;
		users = new Hashtable<Byte, Hashtable<String, User>>();
		//Creating user list for every event associated with operation
		Collection<Event> events = operation.getEvents();
		Iterator<Event> it = events.iterator();
		while(it.hasNext()){
			Event e = it.next();
			users.put(e.getType(), new Hashtable<String, User>());
		}
	}
	
	public Rule(byte[] bytes){
		ByteArrayInputStream istr = new ByteArrayInputStream(bytes);
		DataInputStream dtstr = new DataInputStream(istr);
		
		users = new Hashtable<Byte, Hashtable<String, User>>();
		
		try {
			int operationByteLen = dtstr.readInt();
			byte[] encOperation = new byte[operationByteLen];
			dtstr.read(encOperation);
			operation = new Operation(encOperation);
			Collection<Event> events = operation.getEvents();
			for(int i=0; i<events.size(); i++){
				byte eventType = dtstr.readByte();
				users.put(eventType, new Hashtable<String, User>());
				int ulistlen = dtstr.readInt();
				for(int j=0; j<ulistlen; j++){
					int encUserLen = dtstr.readInt();
					byte[] encUser = new byte[encUserLen];
					dtstr.read(encUser);
					addUser(eventType, new Subscriber(encUser));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * There may be events, for which user list should not be modified, or modification 
	 * possibilities are limited. Deciding, whether to allow rule modification is up to 
	 * derivative class. This method is invoked by {@link #addUser(byte, User)}
	 * and {@link #removeUser(byte, User)} before applying modifications. 
	 * @param eventType Type of event, which users list is to be modified.
	 * @param modificationType Type of modification. Accepted values are: <br>
	 * <li>{@link #MODIFICATION_ADDUSER},
	 * <li>{@link #MODIFICATION_REMOVEUSER},
	 * @return Value indicating, whether modification is allowed.
	 */
	protected abstract boolean isModificationAllowed(byte eventType, byte modificationType, User user);
	
	/**
	 * Checks, if specified operation isn't against this rule.
	 * @param o Operation to be checked.
	 * @return Value indicating if specified operation is against this rule.
	 */
	protected abstract boolean matches(Operation o);
	
	/**
	 * Adds new user to user list for event or modifies an existing one (if user with 
	 * specified name already exists).
	 * @param eventType Type of event, which users list is to be modified.
	 * @param user User to be added.
	 * @return Value indicating, whether modification was successfully applied. It may fail either
	 * 		   due to some user-defined restrictions or because specified event is undefined
	 * 		   for specific operation.
	 */
	public boolean addUser(byte eventType, User user){
		boolean result = false;
		Event e = operation.getEvent(eventType);
		if (e != null){
			if(isModificationAllowed(eventType, MODIFICATION_ADDUSER, user)){
				users.get(e.getType()).put(user.getNodeInfo().getName(), user);
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * Removes user from list of users assigned to specified event.
	 * @param eventType Type of event, which users list is to be modified.
	 * @param user User to be added.
	 * @return Value indicating, whether modification was successfully applied. It may fail either
	 * 		   due to some user-defined restrictions or because specified event is undefined
	 * 		   for specific operation.
	 */
	public boolean removeUser(byte eventType, User user){
		boolean result = false;
		Event e = operation.getEvent(eventType);
		if (e != null){
			if(isModificationAllowed(eventType, MODIFICATION_REMOVEUSER, user)){
				User removedUser = users.get(e.getType()).remove(user.getNodeInfo().getName());
				if (removedUser != null)
					result = true;
			}
		}
		return result;
	}
	
	/**
	 * @return Type of operation, this rule is associated with.
	 */
	public byte getType(){
		return operation.getType();
	}
	
	/**
	 * @return Operation, this rule is associated with.
	 */
	public Operation getOperation(){
		return operation;
	}
	
	/**
	 * Gets list of users assigned to specified event.
	 * @param eventType Type of event.
	 * @return Collection of users assigned to event or <code>null</code> if it doesn'
	 * 		   exist.
	 */
	public Collection<User> getUsers(byte eventType){
		Collection<User> result = null;
		try{
			result = users.get(eventType).values();
		}catch(NullPointerException e){}
		return result;
	}
	
	public Hashtable<Byte, Hashtable<String, User>> getUsers(){
		return users;
	}
	
	public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		
		try {
			//writing operation byte length and operation itself
			byte[] encOperation = operation.encode();
			dtstr.writeInt(encOperation.length);
			dtstr.write(encOperation);
			//reading event types and user lists
			Collection<Event> events = operation.getEvents();
			Iterator<Event> it = events.iterator();
			while(it.hasNext()){
				Event e = it.next();
				//writing event type
				dtstr.write(e.getType());
				Collection<User> ulist = users.get(e.getType()).values();
				//writing user list length
				dtstr.writeInt(ulist.size());
				Iterator<User> uit = ulist.iterator();
				while(uit.hasNext()){
					User u = uit.next();
					//encoding user object length
					byte[] encUser = u.encode();
					dtstr.writeInt(encUser.length);
					//writing user
					dtstr.write(encUser);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ostr.toByteArray();
	}
	
	public String toString(){
		String result = "Operation type: "+getType()+"\n";
		Collection<Event> events = operation.getEvents();
		Iterator<Event> it = events.iterator();
		while(it.hasNext()){
			Event e = it.next();
			result += "User list ("+e+"):\n";
			Collection<User> ulist = users.get(e.getType()).values();
			Iterator<User> uit = ulist.iterator();
			while(uit.hasNext()){
				result += uit.next()+"\n";
			}
		}
		return result; 
	}
	
	public boolean equals(Object compareWith){
		Rule r = (Rule)compareWith;
		if(getType() == r.getType())
			return true;
		return false;
	}
}
