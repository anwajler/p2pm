/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNUnknownAttributes extends STUNAttribute implements
		TLVAttribute {

	public STUNUnknownAttributes(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.UNKNOWN_ATTRIBUTES, valueEBS, messagePosition);
	}
	
	public STUNUnknownAttributes(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.UNKNOWN_ATTRIBUTES, messagePosition);
	}
	
	public String toString(){
		String str = "";
		for (int i = 0; i<this.getLength()*8 - 16; i = i + 16){
			try {
				str += STUNAttributeType.valueOf(attribute.get(32 + i,32 + i + 16)).toString();
			} catch(NullPointerException e) {
				
			}
		}
		return str;
	}
}
