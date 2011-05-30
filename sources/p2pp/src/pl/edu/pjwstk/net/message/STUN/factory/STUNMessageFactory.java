package pl.edu.pjwstk.net.message.STUN.factory;

import java.util.Vector;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.factory.ProtocolMessageFactory;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.net.message.STUN.STUNMessageType;
import pl.edu.pjwstk.types.ExtendedBitSet;

public class STUNMessageFactory extends ProtocolMessageFactory{
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNMessageFactory.class);
			
	protected static final int sizeOfHeader = 160;
	protected static final ExtendedBitSet protocolBits = new ExtendedBitSet("00");

	public Vector<ProtocolObject> interpret(byte[] bytes) {

		return null;
	}

	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(byte[] bytes, int fromPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs, int fromPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(byte[] bytes, int fromPosition,
			int toPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ExtendedBitSet ebs, int fromPosition,
			int toPosition) {
		
		// Validate parameters
		
		if ( ebs.getFixedLength() < sizeOfHeader ) {
			if (logger.isDebugEnabled()) logger.debug("Not valid size of header");
			return null;
		}
		
		if (fromPosition < 0) {
			// FIXME Exception?
			if (logger.isDebugEnabled()) logger.debug("fromPosition must be greater or equal zero.");
			return null;
		}
		
		if (toPosition > ebs.getFixedLength()) {
			// FIXME Exception?
			if (logger.isDebugEnabled()) logger.debug("fromPosition must be lower or equal sizeOfHeader.");
			return null;
		}
			//ExtendedBitSet ebs = new ExtendedBitSet(bytes.length * 8);
			//ebs.set(0,bytes);
		
		// Validate EBS
		
		if (logger.isDebugEnabled()) logger.debug("Checking EBS as a STUN Message");
		if (!protocolBits.equals(ebs.get(0, 2))) { 
			if (logger.isDebugEnabled()) logger.debug("Message isn't STUN message.");
			return null;
		}
		
		int messsize = ebs.get(16,32).toInt() * 8; 
		if (messsize != (ebs.getFixedLength() - sizeOfHeader)) {
			//FIXME Exception?
			if (logger.isDebugEnabled()) logger.debug("Message length problem " + (messsize) + " != " + (ebs.getFixedLength() - sizeOfHeader));
			return null;
		}
		STUNMessageType protocolVersion;
		Vector<ProtocolObject> stunMessages = new Vector<ProtocolObject>(); 
		STUNMessage stunMessage = new STUNMessage();

		// Interpret data
		
		if (ebs.get(32,64).equals(STUNMessageType.RFC5389.getProtocolMagicCookie())){
			protocolVersion = STUNMessageType.RFC5389;
		} else {
			protocolVersion = STUNMessageType.RFC3489;
		}
		
		ExtendedBitSet transactionID = new ExtendedBitSet(protocolVersion.getLengthOfTransactionField(),false);
		transactionID.set(0, ebs.get(sizeOfHeader - protocolVersion.getLengthOfTransactionField(),sizeOfHeader));
		stunMessage.setTransactionID(transactionID);
		if (logger.isDebugEnabled()) logger.debug("STUN message received");

		try {
			stunMessage.setMessageClassandMethod(ebs.get(2, 16));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		if (logger.isDebugEnabled()) logger.debug("STUN message Class = " + stunMessage.getSTUNMessageClass().toString());
		if (logger.isDebugEnabled()) logger.debug("STUN message Method = " + stunMessage.getSTUNMessageMethod().toString());

		Vector<ProtocolObject> stunAttrs = new STUNAttributeFactory().interpret(stunMessage, ebs, 160, ebs.getFixedLength());
		for(ProtocolObject stunAttr : stunAttrs) {
			stunMessage.add((STUNAttribute) stunAttr);
		}
			
		stunMessages.add(stunMessage);
		return stunMessages;
		
		}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			ExtendedBitSet ebs) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			byte[] bytes, int fromPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			ExtendedBitSet ebs, int fromPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			byte[] bytes, int fromPosition, int toPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<ProtocolObject> interpret(ProtocolObject referencedObject,
			ExtendedBitSet ebs, int fromPosition, int toPosition) {
		// TODO Auto-generated method stub
		return null;
	}

}
