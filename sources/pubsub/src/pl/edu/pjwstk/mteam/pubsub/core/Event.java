package pl.edu.pjwstk.mteam.pubsub.core;

/**
 * Class representing single event defined for operation. Stores information 
 * about event type and other type-dependent properties.   
 *  
 * @author Paulina Adamska s3529@pjwstk.edu.pl
 */
public class Event{
	/**
	 * Event type. Types used by publish-subscribe are: ALL, MODIFYAC, REMOVETOPIC, CUSTOM.
	 */
	private byte type;
	
	/**
	 * Creates new event of a specified type.
	 * @param eType Event type. Value 0 is reserved for {@link PubSubConstants#EVENT_ALL}.
	 */
	public Event(byte eType){
		type = eType;
	}
	
	public Event(byte[] bytes){
		type = bytes[0];
	}
	
	/**
	 * @return Event type.
	 */
	public byte getType(){
		return type;
	}
	
	public byte[] encode(){
		byte[] bytes = {type};
		return bytes;
	}
	
	public String toString(){
		String result = "event (type "+type+")";
		return result;
	}
	
	public boolean equals(Object compareWith){
		Event e = (Event)compareWith;
		if(type == e.getType())
			return true;
		return false;
	}
}
