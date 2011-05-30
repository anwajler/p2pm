/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNChangedAddress extends STUNAttributeAddress implements TLVAttribute {

	public STUNChangedAddress(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage)stunMessage, STUNAttributeType.CHANGED_ADDRESS, valueEBS, messagePosition);
	}
	
	public STUNChangedAddress(STUNMessage stunMessage, int messagePosition) {
		super(stunMessage, STUNAttributeType.CHANGED_ADDRESS, messagePosition);
	}

}
