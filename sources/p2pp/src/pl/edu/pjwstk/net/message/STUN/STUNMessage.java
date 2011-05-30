package pl.edu.pjwstk.net.message.STUN;

import java.io.InputStream;

import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.net.credential.ProtocolValidator;
import pl.edu.pjwstk.net.message.ProtocolAttribute;
import pl.edu.pjwstk.net.message.ProtocolMessage;
import pl.edu.pjwstk.net.message.ProtocolMessageInt;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN
 * 
 * RFC 3489
 *      0                   1                   2                   3
 *      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |      STUN Message Type        |         Message Length        |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *                               Transaction ID
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *                                                                     |
 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     
 * RFC 5389
 * 
 *      0                   1                   2                   3
 *      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |0 0|     STUN Message Type     |         Message Length        |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                         Magic Cookie                          |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |                     Transaction ID (96 bits)                  |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      
*/

public class STUNMessage extends ProtocolMessage implements ProtocolMessageInt, ProtocolValidator {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNMessage.class);
	
	public final int MAX_MESSAGE_SIZE_IPV4 = 574;
	public final int MAX_MESSAGE_SIZE_IPV6 = 1280;
	
	private STUNMessageType protocolVersion = STUNMessageType.RFC5389;
	
	public STUNMessage(){
		this(STUNMessageType.RFC5389);
	}
	
	public STUNMessage(STUNMessageType protocolVersion){
		super();
		protocolBits.clear(0,1);
		super.computeLength();
		this.protocolVersion = protocolVersion;
		if (this.protocolVersion.isMagicCookie()){
			protocolMagicCookie = this.protocolVersion.getProtocolMagicCookie();
		}
		generateTransactionID(this.protocolVersion.getLengthOfTransactionField());
		if (logger.isDebugEnabled()) logger.debug("Created STUN Message.");
	}
	
	public ExtendedBitSet getMessageClassEBS(){
		ExtendedBitSet messClassBitSet = new ExtendedBitSet(2);
		messClassBitSet.set(0, protocolMessageType.get(5));
		messClassBitSet.set(1, protocolMessageType.get(9));
		return messClassBitSet;
	}
	
	public ExtendedBitSet getMessageMethodEBS(){
		ExtendedBitSet messMethodBitSet = new ExtendedBitSet(12);
		messMethodBitSet.set(0, protocolMessageType.get(0));
		messMethodBitSet.set(1, protocolMessageType.get(1));
		messMethodBitSet.set(2, protocolMessageType.get(2));
		messMethodBitSet.set(3, protocolMessageType.get(3));
		messMethodBitSet.set(4, protocolMessageType.get(4));
		
		messMethodBitSet.set(5, protocolMessageType.get(6));
		messMethodBitSet.set(6, protocolMessageType.get(7));
		messMethodBitSet.set(7, protocolMessageType.get(8));
		
		messMethodBitSet.set(8, protocolMessageType.get(10));
		messMethodBitSet.set(9, protocolMessageType.get(11));
		messMethodBitSet.set(10, protocolMessageType.get(12));
		messMethodBitSet.set(11, protocolMessageType.get(13));
		return messMethodBitSet;
	}
	
	public STUNMessageClass getSTUNMessageClass(){
		try {
			return STUNMessageClass.valueOf(this.getMessageClassEBS());
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public STUNMessageMethod getSTUNMessageMethod(){
		try {
			return STUNMessageMethod.valueOf(this.getMessageMethodEBS());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public ProtocolAttribute getAttribute(STUNAttributeType stunAttributeType) {
		for (ProtocolAttribute stunAttribute : this.messageAttributes) {
			if (!(stunAttribute instanceof STUNAttribute)) continue;
			if (((STUNAttribute)stunAttribute).getSTUNAttributeType() == stunAttributeType) {
				return stunAttribute;
			}
		}
		return null;
	}
	
	public void setMessageClassandMethod(ExtendedBitSet mess) throws Exception {
		if (mess.length() != 14) {
			throw new Exception ("BitSet must be 14 bits length");
		}
		protocolMessageType.set(0, mess);
	}
	public void setMessageClassandMethod(STUNMessageClass stunMessageClass, STUNMessageMethod stunMessageMethod){
		protocolMessageType.set(0, stunMessageMethod.getMessageMethod().get(0));
		protocolMessageType.set(1, stunMessageMethod.getMessageMethod().get(1));
		protocolMessageType.set(2, stunMessageMethod.getMessageMethod().get(2));
		protocolMessageType.set(3, stunMessageMethod.getMessageMethod().get(3));
		protocolMessageType.set(4, stunMessageMethod.getMessageMethod().get(4));
		protocolMessageType.set(5, stunMessageClass.getMessageClass().get(0));
		protocolMessageType.set(6, stunMessageMethod.getMessageMethod().get(5));
		protocolMessageType.set(7, stunMessageMethod.getMessageMethod().get(6));
		protocolMessageType.set(8, stunMessageMethod.getMessageMethod().get(7));
		protocolMessageType.set(9, stunMessageClass.getMessageClass().get(1));
		protocolMessageType.set(10, stunMessageMethod.getMessageMethod().get(8));
		protocolMessageType.set(11, stunMessageMethod.getMessageMethod().get(9));
		protocolMessageType.set(12, stunMessageMethod.getMessageMethod().get(10));
		protocolMessageType.set(13, stunMessageMethod.getMessageMethod().get(11));
	}
	
	public void add(STUNAttribute attribute){
		messageAttributes.add(attribute);
		if (attribute.getMessagePosition() == 0){
			attribute.setMessagePosition(this.sizeOfHeader + protocolMessageLength.toInt()*8);			
		}
		logger.debug("Added attribute " + attribute.getSTUNAttributeType().toString() + " " + attribute.toString() + " at position " + attribute.getMessagePosition());
		super.computeLength();
	}
	
	public STUNMessage(TransportPacket transportPacket){
		super(transportPacket);
		//TODO Validate message size defined by STUN for UDP v4 and v6
	}

	
	public byte[] toByte() {
		return (ExtendedBitSet.add(super.getHeader(),super.getAttributes()).toByteArray());
	}

	@Override
	public void setTransactionID(ExtendedBitSet transactionID) {
		// FIXME validate length
		super.setTransactionID(transactionID);
	}

	public boolean isMagicCookie() {
		return protocolVersion != STUNMessageType.RFC3489;
	}

	public STUNMessageType getMessageVersion() {
		return protocolVersion;
	}

	public boolean tryParse(int fromPosition, ExtendedBitSet ebs) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean tryParse(byte[] bytes) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean tryParse(InputStream inputStream) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		boolean result = true;
		for(ProtocolAttribute stunAttribute : this.messageAttributes){
			if (stunAttribute instanceof ProtocolValidator){
				result = result & ((ProtocolValidator) stunAttribute).isValid();
			}
		}
		return result;
	}	
}
