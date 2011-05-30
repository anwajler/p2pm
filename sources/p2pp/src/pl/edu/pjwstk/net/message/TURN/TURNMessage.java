/**
 * 
 */
package pl.edu.pjwstk.net.message.TURN;

import pl.edu.pjwstk.net.TransportPacket;
import pl.edu.pjwstk.net.message.ProtocolAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.net.message.STUN.STUNMessageType;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.TURN
 */
public class TURNMessage extends STUNMessage {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TURNMessage.class);

	/**
	 * 
	 */
	public TURNMessage() {
		this(STUNMessageType.RFC5389);
	}

	/**
	 * @param protocolVersion
	 */
	public TURNMessage(TURNMessageType protocolVersion) {
		super(STUNMessageType.RFC5389);
	}

	/**
	 * @param protocolVersion
	 */
	public TURNMessage(STUNMessageType protocolVersion) {
		super(protocolVersion);
	}
	
	public void setMessageClassandMethod(TURNMessageClass turnMessageClass, TURNMessageMethod turnMessageMethod){
		protocolMessageType.set(0, turnMessageMethod.getMessageMethod().get(0));
		protocolMessageType.set(1, turnMessageMethod.getMessageMethod().get(1));
		protocolMessageType.set(2, turnMessageMethod.getMessageMethod().get(2));
		protocolMessageType.set(3, turnMessageMethod.getMessageMethod().get(3));
		protocolMessageType.set(4, turnMessageMethod.getMessageMethod().get(4));
		protocolMessageType.set(5, turnMessageClass.getMessageClass().get(0));
		protocolMessageType.set(6, turnMessageMethod.getMessageMethod().get(5));
		protocolMessageType.set(7, turnMessageMethod.getMessageMethod().get(6));
		protocolMessageType.set(8, turnMessageMethod.getMessageMethod().get(7));
		protocolMessageType.set(9, turnMessageClass.getMessageClass().get(1));
		protocolMessageType.set(10, turnMessageMethod.getMessageMethod().get(8));
		protocolMessageType.set(11, turnMessageMethod.getMessageMethod().get(9));
		protocolMessageType.set(12, turnMessageMethod.getMessageMethod().get(10));
		protocolMessageType.set(13, turnMessageMethod.getMessageMethod().get(11));
	}
	
	public TURNMessageClass getTURNMessageClass(){
		try {
			return TURNMessageClass.valueOf(this.getMessageClassEBS());
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public TURNMessageMethod getTURNMessageMethod(){
		try {
			return TURNMessageMethod.valueOf(this.getMessageMethodEBS());
		} catch (Exception e) {
			logger.error(e);
			return null;
		}	
	}
	
	public void add(TURNAttribute attribute){
		messageAttributes.add(attribute);
		if (attribute.getMessagePosition() == 0){
			attribute.setMessagePosition(this.sizeOfHeader + protocolMessageLength.toInt()*8);
			//logger.debug("Attribute " + attribute.getTURNAttributeType().toString() + " position not set - calculated " + attribute.getMessagePosition());
		}
		logger.debug("Added attribute " + attribute.getTURNAttributeType().toString() + " " + attribute.toString() + " at position " + attribute.getMessagePosition());
		super.computeLength();
	}
	
	public ProtocolAttribute getAttribute(TURNAttributeType turnAttributeType) {
		for (ProtocolAttribute turnAttribute : this.messageAttributes) {
			if (turnAttribute instanceof STUNAttribute){
				if (((STUNAttribute)turnAttribute).getSTUNAttributeType() == turnAttributeType.getSTUNAttributeType()) {
					return turnAttribute;
				}
			}
			if (turnAttribute instanceof TURNAttribute){
				if (((TURNAttribute)turnAttribute).getTURNAttributeType() == turnAttributeType) {
					return turnAttribute;
				}
			}
		}
		return null;
	}	
	
	/**
	 * @param transportPacket
	 */
	public TURNMessage(TransportPacket transportPacket) {
		super(transportPacket);
		// TODO Auto-generated constructor stub
	}

}
