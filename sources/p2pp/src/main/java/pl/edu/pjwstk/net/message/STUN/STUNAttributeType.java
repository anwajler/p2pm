package pl.edu.pjwstk.net.message.STUN;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNAlternateServer;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNCacheTimeout;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNChangeRequest;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNChangedAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNErrorCode;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNFingerprint;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNMappedAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNMessageIntegrity;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNNonce;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNOtherAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNPadding;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNPassword;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNRealm;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNReflectedFrom;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNResponseAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNResponseOrgin;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNSoftware;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNSourceAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNUnknownAttributes;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNUsername;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNXorMappedAddress;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNXorReflectedFrom;
import pl.edu.pjwstk.net.message.STUN.attribute.STUNXorResponseTarget;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN
 *  * RFC 5389
 *
 *  Comprehension-required range (0x0000-0x7FFF):
 *    0x0000: (Reserved)
 *    0x0001: MAPPED-ADDRESS
 *    0x0002: (Reserved; was RESPONSE-ADDRESS)
 *    0x0003: (Reserved; was CHANGE-REQUEST)
 *    0x0004: (Reserved; was SOURCE-ADDRESS)
 *    0x0005: (Reserved; was CHANGED-ADDRESS)
 *    0x0006: USERNAME
 *    0x0007: (Reserved; was PASSWORD)
 *    0x0008: MESSAGE-INTEGRITY
 *    0x0009: ERROR-CODE
 *    0x000A: UNKNOWN-ATTRIBUTES
 *    0x000B: (Reserved; was REFLECTED-FROM)
 *    0x0014: REALM
 *    0x0015: NONCE
 *    0x0020: XOR-MAPPED-ADDRESS
 *
 *  Comprehension-optional range (0x8000-0xFFFF)
 *    0x8022: SOFTWARE
 *    0x8023: ALTERNATE-SERVER
 *    0x8028: FINGERPRINT
 *    
 *   NAT Behavior-required range
 *    0x0026: PADDING
 *    0x0027: XOR-RESPONSE-TARGET
 *    0x0028: XOR-REFLECTED-FROM
 *   
 *   NAT Behavior-optional range
 *    0x8027: CACHE-TIMEOUT
 *    0x802b: RESPONSE-ORIGIN
 *    0x802c: OTHER-ADDRESS
 *    
 */
public enum STUNAttributeType {
	MAPPED_ADDRESS(0x0001,false,"MAPPED-ADDRESS",STUNMappedAddress.class),
	RESPONSE_ADDRESS(0x0002,true,"RESPONSE-ADDRESS", STUNResponseAddress.class),
	CHANGE_REQUEST(0x0003,true,"CHANGE-REQUEST", STUNChangeRequest.class),
	SOURCE_ADDRESS(0x0004,true,"SOURCE-ADDRESS", STUNSourceAddress.class),
	CHANGED_ADDRESS(0x005,true,"CHANGED-ADDRESS", STUNChangedAddress.class),
	USERNAME(0x0006,false,"USERNAME", STUNUsername.class),
	PASSWORD(0x0007,true,"PASSWORD", STUNPassword.class),
	MESSAGE_INTEGRITY(0x0008,false,"MESSAGE-INTEGRITY", STUNMessageIntegrity.class),
	ERROR_CODE(0x0009,false,"ERROR-CODE", STUNErrorCode.class),
	UNKNOWN_ATTRIBUTES(0x000A,false,"UNKNOWN-ATTRIBUTES", STUNUnknownAttributes.class),
	REFLECTED_FROM(0x000B,true,"REFLECTED-FROM", STUNReflectedFrom.class),
	REALM(0x0014,false,"REALM", STUNRealm.class),
	NONCE(0x0015,false,"NONCE", STUNNonce.class),
	XOR_MAPPED_ADDRESS(0x0020,false,"XOR-MAPPED-ADDRESS", STUNXorMappedAddress.class),
	SOFTWARE(0x8022,false,"SOFTWARE", STUNSoftware.class),
	ALTERNATE_SERVER(0x8023,false,"ALTERNATE-SERVER", STUNAlternateServer.class),
	FINGERPRINT(0x8028,false,"FINGERPRINT", STUNFingerprint.class),
	PADDING(0x0026,false,"PADDING",STUNPadding.class),
	XOR_RESPONSE_TARGET(0x0027,false,"XOR-RESPONSE-TARGET",STUNXorResponseTarget.class),
	XOR_REFLECTED_FROM(0x0028,false,"XOR-REFLECTED-FROM",STUNXorReflectedFrom.class),
	CACHE_TIMEOUT(0x8027, false,"CACHE-TIMEOUT",STUNCacheTimeout.class),
	RESPONSE_ORIGIN(0x802B, false, "RESPONSE-ORGIN", STUNResponseOrgin.class),
	OTHER_ADDRESS(0x802C, false,"OTHER-ADDRESS",STUNOtherAddress.class);

	private ExtendedBitSet typeBS;
	private boolean obsolete;
	private String name;
	private Class<?> className;

	STUNAttributeType(int type,boolean obsolete,String name){
		this.typeBS=new ExtendedBitSet(Integer.toBinaryString(type),16,false,true);
		this.obsolete = obsolete;
		this.name = name;
		this.className = null;
	}
	
	STUNAttributeType(int type,boolean obsolete,String name, Class<? extends TLVAttribute> className){
		this.typeBS=new ExtendedBitSet(Integer.toBinaryString(type),16,false,true);
		this.obsolete = obsolete;
		this.name = name;
		this.className = className;
	}
	
	public ExtendedBitSet getType(){
		return typeBS;
	}
	
	public boolean isObsolete(STUNMessageType stunMessageType){
		return this.obsolete;
	}
	
	public String getName(){
		return this.name;
	}
	
	public static STUNAttributeType valueOf(ExtendedBitSet bs){
		for( STUNAttributeType currentAttribute : STUNAttributeType.values()){
			if(bs.get(0,16).equals(currentAttribute.getType())){
				return currentAttribute;
			}
		}
		return null;
	}

	public boolean isAvailable(STUNMessageType stunMessageType) {
		// FIXME Declare, initialize and fix this
		return true;
	}
	
	public static STUNAttribute createAttribute(STUNAttributeType stunAttributeType, ExtendedBitSet valueEBS, int messagePosition) {
		STUNAttribute stunAttribute = null;
		try {
			Constructor<?> cons = stunAttributeType.className.getConstructor(ExtendedBitSet.class, int.class);
			Object[] args = new Object[] { valueEBS, messagePosition };
			stunAttribute = (STUNAttribute) cons.newInstance(args);
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

	public static STUNAttribute createAttribute(STUNMessage stunMessage, STUNAttributeType stunAttributeType, ExtendedBitSet valueEBS, int messagePosition) {
		STUNAttribute stunAttribute = null;
		try {
			Constructor<?> cons = stunAttributeType.className.getConstructor(ProtocolObject.class, ExtendedBitSet.class, int.class);
			Object[] args = new Object[] { stunMessage, valueEBS, messagePosition };
			stunAttribute = (STUNAttribute) cons.newInstance(args);
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
	
	public String toString() {
		return this.name;
	}
}
