/**
 * 
 */
package pl.edu.pjwstk.net.message.TURN;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNAlternateServer;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNChangeRequest;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNChangedAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNErrorCode;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNFingerprint;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNMappedAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNMessageIntegrity;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNNonce;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNPassword;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNRealm;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNReflectedFrom;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNResponseAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNSoftware;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNSourceAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNUnknownAttributes;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNUsername;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNXorMappedAddress;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNBandwidth;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNChannelNumber;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNDontFragment;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNEvenPort;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNLifetime;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNRequestedTransport;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNReservationToken;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNTimerVal;
import pl.edu.pjwstk.net.message.TURN.attribute.TURNXorPeerAddress;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.TURN
 */
/*
 *    This STUN extension defines the following new attributes:

     0x000C: CHANNEL-NUMBER
     0x000D: LIFETIME
     0x0010: Reserved (was BANDWIDTH)
     0x0012: XOR-PEER-ADDRESS
     0x0013: DATA
     0x0016: XOR-RELAYED-ADDRESS
     0x0018: EVEN-PORT
     0x0019: REQUESTED-TRANSPORT
     0x001A: DONT-FRAGMENT
     0x0021: Reserved (was TIMER-VAL)
     0x0022: RESERVATION-TOKEN
 * 
 */
public enum TURNAttributeType {
	MAPPED_ADDRESS(0x0001,false,"MAPPED-ADDRESS",STUNAttributeType.MAPPED_ADDRESS,STUNMappedAddress.class),
	RESPONSE_ADDRESS(0x0002,true,"RESPONSE-ADDRESS",STUNAttributeType.RESPONSE_ADDRESS, STUNResponseAddress.class),
	CHANGE_ADDRESS(0x003,true,"CHANGE-ADDRESS",STUNAttributeType.CHANGE_REQUEST, STUNChangeRequest.class),
	SOURCE_ADDRESS(0x0004,true,"SOURCE-ADDRESS", STUNAttributeType.SOURCE_ADDRESS, STUNSourceAddress.class),
	CHANGED_ADDRESS(0x005,true,"CHANGED-ADDRESS", STUNAttributeType.CHANGED_ADDRESS, STUNChangedAddress.class),
	USERNAME(0x0006,false,"USERNAME", STUNAttributeType.USERNAME, STUNUsername.class),
	PASSWORD(0x0007,true,"PASSWORD", STUNAttributeType.PASSWORD, STUNPassword.class),
	MESSAGE_INTEGRITY(0x0008,false,"MESSAGE-INTEGRITY", STUNAttributeType.MESSAGE_INTEGRITY ,STUNMessageIntegrity.class),
	ERROR_CODE(0x0009,false,"ERROR-CODE", STUNAttributeType.ERROR_CODE, STUNErrorCode.class),
	UNKNOWN_ATTRIBUTES(0x000A,false,"UNKNOWN-ATTRIBUTES", STUNAttributeType.UNKNOWN_ATTRIBUTES, STUNUnknownAttributes.class),
	REFLECTED_FROM(0x000B,true,"REFLECTED-FROM", STUNAttributeType.REFLECTED_FROM, STUNReflectedFrom.class),
	REALM(0x0014,false,"REALM", STUNAttributeType.REALM, STUNRealm.class),
	NONCE(0x0015,false,"NONCE", STUNAttributeType.NONCE, STUNNonce.class),
	XOR_MAPPED_ADDRESS(0x0020,false,"XOR-MAPPED-ADDRESS", STUNAttributeType.XOR_MAPPED_ADDRESS, STUNXorMappedAddress.class),
	SOFTWARE(0x8022,false,"SOFTWARE", STUNAttributeType.SOFTWARE ,STUNSoftware.class),
	ALTERNATE_SERVER(0x8023,false,"ALTERNATE-SERVER", STUNAttributeType.ALTERNATE_SERVER, STUNAlternateServer.class),
	FINGERPRINT(0x8028,false,"FINGERPRINT", STUNAttributeType.FINGERPRINT, STUNFingerprint.class),
	CHANNEL_NUMBER(0x000C, false, "CHANNEL-NUMBER", null, TURNChannelNumber.class),
	LIFETIME(0x000D, false, "LIFETIME", null, TURNLifetime.class),
	BANDWIDTH(0x0010, true, "BANDWIDTH", null, TURNBandwidth.class),
	XOR_PEER_ADDRESS(0x0012, false, "XOR-PEER-ADDRESS", null, TURNXorPeerAddress.class),
	EVEN_PORT(0x0018, false, "EVEN-PORT", null, TURNEvenPort.class),
	REQUESTED_TRANSPORT(0x0019, false, "REQUESTED-TRANSPORT", null, TURNRequestedTransport.class),
	DONT_FRAGMENT(0x001A, false, "DONT-FRAGMENT", null, TURNDontFragment.class),
	TIMER_VAL(0x0021, true, "TIMER-VAL", null, TURNTimerVal.class),
	RESERVATION_TOKEN(0x0022, false, "RESERVATION-TOKEN", null, TURNReservationToken.class);

	private STUNAttributeType stunAttributeType;
	private ExtendedBitSet typeBS;
	private boolean obsolete;
	private String name;
	private Class<?> className;

	TURNAttributeType(int type,boolean obsolete,String name){
		this.typeBS=new ExtendedBitSet(Integer.toBinaryString(type),16,false,true);
		this.obsolete = obsolete;
		this.name = name;
		this.className = null;
	}
	
	TURNAttributeType(int type,boolean obsolete,String name,STUNAttributeType stunAttributeType, Class<? extends TLVAttribute> className){
		this.typeBS=new ExtendedBitSet(Integer.toBinaryString(type),16,false,true);
		this.obsolete = obsolete;
		this.name = name;
		this.className = className;
		this.stunAttributeType = stunAttributeType;
	}
	
	public ExtendedBitSet getType(){
		return typeBS;
	}
	
	public boolean isObsolete(TURNMessageType turnMessageType){
		return this.obsolete;
	}
	
	public String getName(){
		return this.name;
	}
	
	public static TURNAttributeType valueOf(ExtendedBitSet bs){
		for( TURNAttributeType currentAttribute : TURNAttributeType.values()){
			if(bs.get(0,16).equals(currentAttribute.getType())){
				return currentAttribute;
			}
		}
		return null;
	}

	public boolean isAvailable(TURNMessageType turnMessageType) {
		// FIXME Declare, initialize and fix this
		return true;
	}

	public STUNAttributeType getSTUNAttributeType(){
		return this.stunAttributeType;
	}
	
	public static TURNAttribute createAttribute(TURNAttributeType turnAttributeType, ExtendedBitSet valueEBS) {
		TURNAttribute turnAttribute = null;
		try {
			Constructor<?> cons = turnAttributeType.className.getConstructor(ExtendedBitSet.class);
			Object[] args = new Object[] { valueEBS };
			turnAttribute = (TURNAttribute) cons.newInstance(args);
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
		return turnAttribute;
	}

	public static TURNAttribute createAttribute(TURNMessage turnMessage, TURNAttributeType turnAttributeType, ExtendedBitSet valueEBS) {
		TURNAttribute stunAttribute = null;
		try {
			Constructor<?> cons = turnAttributeType.className.getConstructor(ProtocolObject.class, ExtendedBitSet.class);
			Object[] args = new Object[] { turnMessage, valueEBS };
			stunAttribute = (TURNAttribute) cons.newInstance(args);
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
		return stunAttribute;	
	}
}
