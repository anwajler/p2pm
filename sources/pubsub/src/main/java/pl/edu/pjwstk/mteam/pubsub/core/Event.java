package pl.edu.pjwstk.mteam.pubsub.core;

import java.io.EOFException;

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
	private short type;
	
	/**
	 * Creates new event of a specified type.
	 * @param eType Event type. Value 0 is reserved for {@link PubSubConstants#EVENT_ALL}.
	 */
	public Event(short eType){
		type = eType;
	}
	
	public Event(byte[] bytes) throws EOFException{
            if(bytes.length==2){
                int x1 = bytes[0] & 0xff;
                int x2 = bytes[1] & 0xff;
                if ((x1 | x2) < 0)
                throw new EOFException();
                type =  (short)((x1 << 8) + (x2 << 0));
            }else type = bytes[0];
	}
	
	/**
	 * @return Event type.
	 */
	public short getType(){
		return type;
	}
	
	public byte[] encode(){
            byte [] result = new byte[2];
		result[0] = (byte)((type >>> 8) & 0xFF);
                result[1] = (byte)((type >>> 0) & 0xFF);
		return result;
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
