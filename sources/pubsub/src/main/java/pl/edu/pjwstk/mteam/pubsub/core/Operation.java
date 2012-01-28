package pl.edu.pjwstk.mteam.pubsub.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Class representing single operation. It stores information about operation type, 
 * user performing it, identifier and events connected with it. It is used to describe 
 * pub-sub operations in access control and interest conditions rules.<p>
 * For example for access control operation may be defined as follows:<p>
 * type = SUBSCRIBE<br>
 * id = topic ID<br>
 * user = subscriber<br>
 * events = ALL (means, that no events defined for this operation - access rules are valid
 *               for every SUBSCRIBE operation)<p><p>
 *      
 * type = PUBLISH<br>
 * id = topic ID<br>
 * user = publisher<br>
 * events = ALL, REMOVETOPIC, MODIFYAC, CUSTOM (there may be different access control rules 
 *                                              for each event)<p><p>
 * 
 * And for interest conditions:<p>
 * 
 * type = NOTIFY<br>
 * id = topic ID<br>
 * user = publisher<br>
 * events = ALL, REMOVETOPIC, CUSTOM (f.e. node will always want to receive REMOVETOPIC
 * 									  notification, but CUSTOM events only from user1)<p><p>
 * 
 * Every operation must contain at least one event. If it can't be divided to events - user 
 * can declare general event of type {@link PubSubConstants#EVENT_ALL} for it.
 * 
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class Operation{
	/**
	 * Operation type
	 */
	protected short type;
	/**
	 * Some user defined identifier
	 */
	protected String id;
	/**
	 * Information about user, performing operation
	 */
	protected User user;
	/**
	 * Events, that can be connected with this operation
	 */
	protected Hashtable<Short, Event> events;
	/**
	 * Operation date
	 */
	protected Date timestamp;
	
	/**
	 * Creates new operation.
	 * @param oType Operation type.
	 * @param oID Operation id. For pub-sub it is topic ID.
	 * @param oUser User performing operation.
	 * @param oEvents list of events defined for this operation.
	 */
	public Operation(short oType, String oID, User oUser, Vector<Event> oEvents){
		type = oType;
		id = oID;
		user = oUser;
		events = new Hashtable<Short, Event>();
		for(int i=0; i<oEvents.size(); i++){
			Event e = oEvents.get(i);
			addEvent(e);
		}
	}
	
	/**
	 * Used only for encoding AC rules, so setting user performing the operation is
	 * unnecessary.
	 * @param bytes Encoded operation.
	 */
	public Operation(byte[] bytes){
		ByteArrayInputStream istr = new ByteArrayInputStream(bytes);
		DataInputStream dtstr = new DataInputStream(istr);
		
		try {
			type = dtstr.readByte();
			//reading topic ID length
			int idlen = dtstr.readInt();
			byte[] tID = new byte[idlen];
			dtstr.read(tID);
			id = new String(tID);
			byte eventNumber = dtstr.readByte();
			events = new Hashtable<Short, Event>();
			for(int i=0; i<eventNumber; i++){
				int eventByteLen = dtstr.readInt();
				byte[] encEvent = new byte[eventByteLen];
				dtstr.read(encEvent);
				Event e = new Event(encEvent);
				addEvent(e);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Creates new operation.
	 * @param oType Operation type.
	 * @param oID Operation id. For pub-sub it is topic ID.
	 * @param oUser User performing operation.
	 * @param oEvent event defined for this operation.
	 */
	public Operation(byte oType, String oID, User oUser, Event oEvent){
		type = oType;
		id = oID;
		user = oUser;
		events = new Hashtable<Short, Event>();
		addEvent(oEvent);
	}
	
	/**
	 * @return Operation type
	 */
	public short getType(){
		return type;
	}
	
	/**
	 * @return Operation ID
	 */
	public String getID(){
		return id;
	}
	
	/**
	 * @return User performing operation or <code>null</code> if it is not set. This
	 *         property is not set if operation is defined only to create access rule
	 *         or interest condition for it. 
	 */
	public User getUser(){
		return user;
	}
	
	/**
	 * @return First event from the list.
	 */
	public Event getEvent(){
		return events.values().iterator().next();
	}
	
	/**
	 * @param eventType Type of requested event
	 * @return Event of specified type or <code>null</code> if such event doesn't exist
	 */
	public Event getEvent(short eventType){
		Event result = null;
		try{
			result = events.get(new Short(eventType));
		}catch(NullPointerException e){
			return result;
		}
		return result;
	}
	
	/**
	 * @return Events associated with this operation.
	 */
	public Collection<Event> getEvents(){
		return events.values();
	}
	
	/**
	 * @return Time when operation was performed. 
	 */
	public Date getTime(){
		return timestamp;
	}
	
	/**
	 * Sets operation type.
	 * @param newType New operation type value.
	 */
	public void setType(byte newType){
		type = newType;
	}
	
	/**
	 * Sets operation ID.
	 * @param newID New ID value.
	 */
	public void setID(String newID){
		id = newID;
	}
	
	/**
	 * Stores information about user performing this operation.
	 * @param newUser User performing this operation.
	 */
	public void setUser(User newUser){
		user = newUser;
	}
	
	/**
	 * Sets date and time, when operation was performed.
	 * @param time Time, when operation was performed.
	 */
	public void setTime(Date time){
		timestamp = time;
	}
	
	/**
	 * Adds new event to operation or replaces existing one (if event of specified type
	 * already is defined for this operation.
	 * @param newEvent Event to be added.
	 * @return Old event of specified type defined for this operation or <code>null</code>
	 *         if it wasn't defined.
	 */
	public Event addEvent(Event newEvent){
		return events.put(newEvent.getType(), newEvent);
	}
	
	/**
	 * Removes event defined for this operation.
	 * @param eventType Type of event to be removed.
	 * @return Removed event of specified type defined for this operation or <code>null</code>
	 *         if it wasn't defined.
	 */
	public Event removeEvent(byte eventType){
		return events.remove(eventType);
	}
	
	public byte[] encode(){
		ByteArrayOutputStream ostr = new ByteArrayOutputStream();
		DataOutputStream dtstr = new DataOutputStream(ostr);
		
		try {
			//writing operation type
			dtstr.write(type);
			//writing operation ID length
			dtstr.writeInt(id.length());
			//writing operation ID
			dtstr.write(id.getBytes());
			//writing number of events
			dtstr.write(events.size());
			Collection<Event> elements = events.values();
			Iterator<Event> it = elements.iterator();
			while(it.hasNext()){
				Event e = it.next();
				byte[] encEvent = e.encode();
				//writing event length and event itself
				dtstr.writeInt(encEvent.length);
				dtstr.write(encEvent);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return ostr.toByteArray();
	}
	
	public String toString(){
		String result = "==============================\n";
		result += "Operation (type: "+type+")\n";
		result += "\tID: "+id+"\n";
		result += "\tPerformed by: "+user+"\n";
		result += "\tEvents:\n";
		Collection<Event> elements = events.values();
		Iterator<Event> it = elements.iterator();
		while(it.hasNext()){
			Event e = it.next();
			result += "\t\t"+e+"\n";
		}
		result += "==============================\n";
		return result;
	}
	
	public boolean equals(Object compareWith){
		Operation o = (Operation)compareWith;
		if(type == o.type)
			return true;
		return false;
	}
}
