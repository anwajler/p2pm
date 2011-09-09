/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pl.edu.pjwstk.net.AddressFamily;
import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNXorMappedAddress extends STUNAttributeAddress implements
		TLVAttribute {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNXorMappedAddress.class);
	
	public STUNXorMappedAddress(ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage)null, STUNAttributeType.XOR_MAPPED_ADDRESS, valueEBS, messagePosition);
	}
	
	public STUNXorMappedAddress(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.XOR_MAPPED_ADDRESS, valueEBS, messagePosition);
	}
	
	public STUNXorMappedAddress(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.XOR_MAPPED_ADDRESS, messagePosition);
	}
	
	/*
	 * X-Port is computed by taking the mapped port in host byte order,
	 * XOR'ing it with the most significant 16 bits of the magic cookie, and
	 * then the converting the result to network byte order.  If the IP
	 * address family is IPv4, X-Address is computed by taking the mapped IP
	 * address in host byte order, XOR'ing it with the magic cookie, and
	 * converting the result to network byte order.  If the IP address
	 * family is IPv6, X-Address is computed by taking the mapped IP address
	 * in host byte order, XOR'ing it with the concatenation of the magic
	 * cookie and the 96-bit transaction ID, and converting the result to
	 * network byte order.
	 */
	
	/*
	 * (non-Javadoc)
	 * @see pl.edu.pjwstk.net.message.STUN.attribute.STUNAttributeAddress#getHostAddress()
	 */
	public InetAddress getHostAddress() throws UnknownHostException{
		if (stunMessage != null){
			ExtendedBitSet hostAddressXOR = new ExtendedBitSet(attribute.get(64,96),32);
			hostAddressXOR.xor(stunMessage.getMagicCookie());
			return InetAddress.getByAddress(hostAddressXOR.toByteArray());
		}
		else {
			logger.debug("STUNMessage is null - cannot XOR.");
			return InetAddress.getByAddress(attribute.get(64,96).toByteArray());
		}		
	}

	public int getHostPort() throws UnknownHostException{
		if (stunMessage != null){
			ExtendedBitSet hostPortXOR = new ExtendedBitSet(attribute.get(48,64),16);
			hostPortXOR.xor(stunMessage.getMagicCookie().get(0,16));
			return hostPortXOR.toInt();
		}
		else {
			logger.debug("STUNMessage is null - cannot XOR.");
			return attribute.get(48,64).toInt();
		}
	}

	public void setHostAddress(InetAddress hostAddress){
		if (stunMessage != null){
			addressFamily = AddressFamily.getAddressFamily(hostAddress);
			attribute.set(40,addressFamily.getAddressFamilyBitSet());
			attribute.shrink(64);
			attribute.set(64, hostAddress.getAddress());
			setLength(hostAddress.getAddress().length + 4);
		}
		else {
			logger.debug("STUNMessage is null - cannot XOR.");
		}
	}
	
	public void setHostPort(int hostPort){
		if (stunMessage != null){
			attribute.set(48,new ExtendedBitSet(Integer.toBinaryString(hostPort),16,false,true));
		}
		else {
			logger.debug("STUNMessage is null - cannot XOR.");
		}
	}
}
