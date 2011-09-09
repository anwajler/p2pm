/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import java.net.InetAddress;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 * RFC 5389 (0x0001: MAPPED-ADDRESS)
 */
public class STUNMappedAddress extends STUNAttributeAddress implements
		TLVAttribute {
	
	public STUNMappedAddress(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage)stunMessage, STUNAttributeType.MAPPED_ADDRESS, valueEBS, messagePosition);
	}
	
	public STUNMappedAddress(ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage)null, STUNAttributeType.MAPPED_ADDRESS, valueEBS, messagePosition);
	}
	
	public STUNMappedAddress(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.MAPPED_ADDRESS, messagePosition);
	}

	public STUNMappedAddress(STUNMessage stunMessage, InetAddress hostAddress, int messagePosition){
		super(stunMessage, STUNAttributeType.MAPPED_ADDRESS, messagePosition);
		setHostAddress(hostAddress);
	}
	
	public static boolean isValidType(ExtendedBitSet bs) {	
		return false;
	}

}
