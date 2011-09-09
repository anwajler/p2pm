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
public class STUNOtherAddress extends STUNAttributeAddress implements TLVAttribute {

	public STUNOtherAddress(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage)stunMessage, STUNAttributeType.OTHER_ADDRESS, valueEBS, messagePosition);
	}
	
	public STUNOtherAddress(STUNMessage stunMessage, int messagePosition) {
		super(stunMessage, STUNAttributeType.OTHER_ADDRESS, messagePosition);
	}

}