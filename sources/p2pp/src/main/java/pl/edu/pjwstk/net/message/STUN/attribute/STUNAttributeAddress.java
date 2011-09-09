/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pl.edu.pjwstk.net.AddressFamily;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNAttributeAddress extends STUNAttribute implements TLVAttribute {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNAttributeAddress.class);

	protected AddressFamily addressFamily;
	
	/**
	 * @param stunMessage
	 * @param stunAttributeType
	 */
	public STUNAttributeAddress(STUNMessage stunMessage,
			STUNAttributeType stunAttributeType, int messagePosition) {
		super(stunMessage, stunAttributeType, messagePosition);
	}

	public STUNAttributeAddress(STUNMessage stunMessage,
			STUNAttributeType stunAttributeType, ExtendedBitSet valueEBS, int messagePosition) {
		super(stunMessage, stunAttributeType, valueEBS, messagePosition);
	}
	
	public InetAddress getHostAddress() throws UnknownHostException{
		return InetAddress.getByAddress(attribute.get(64,96).toByteArray());
	}

	public int getHostPort() throws UnknownHostException{
		return attribute.get(48,64).toInt();
	}

	public void setHostAddress(InetAddress hostAddress){
		addressFamily = AddressFamily.getAddressFamily(hostAddress);
		attribute.set(40,addressFamily.getAddressFamilyBitSet());
		attribute.shrink(64);
		attribute.set(64, hostAddress.getAddress());
		setLength(hostAddress.getAddress().length + 4);
	}
	
	public void setHostPort(int hostPort){
		attribute.set(48,new ExtendedBitSet(Integer.toBinaryString(hostPort),16,false,true));
	}
	
	public String toString() {
		try {
			return ("IP " + this.getHostAddress().toString() + " PORT " + this.getHostPort());
		} catch (UnknownHostException e) {
			logger.error("", e);
			return null;
		}
	}
}
