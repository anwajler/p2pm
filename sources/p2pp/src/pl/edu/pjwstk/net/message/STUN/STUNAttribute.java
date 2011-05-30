/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN;

import java.io.InputStream;

import pl.edu.pjwstk.net.message.ProtocolAttribute;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN
 */
public abstract class STUNAttribute extends ProtocolAttribute implements TLVAttribute {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNAttribute.class);
	private STUNAttributeType stunAttributeType;

	protected STUNMessage stunMessage;
	
	protected STUNAttribute(STUNMessage stunMessage, STUNAttributeType stunAttributeType, ExtendedBitSet valueEBS, int messagePosition) {
		this(stunMessage, stunAttributeType, messagePosition);
		attribute.set(32,valueEBS);
		this.setLength(attribute.getFixedLength()/8 - 4);		
	}
	
	protected STUNAttribute(STUNMessage stunMessage, ExtendedBitSet stunAttributeTypeEBS, ExtendedBitSet valueEBS, int messagePosition) {
		this(stunMessage, stunAttributeTypeEBS, messagePosition);
		attribute.set(32,valueEBS);
		this.setLength(attribute.getFixedLength()/8 - 4);
	}
	
	protected STUNAttribute(STUNMessage stunMessage, STUNAttributeType stunAttributeType, int messagePosition){
		this(stunMessage,stunAttributeType.getType(),messagePosition);
		this.stunAttributeType = stunAttributeType;
		// FIXME stunMessage == null
		if (stunMessage != null){
			if(stunAttributeType.isObsolete(stunMessage.getMessageVersion())){
				if (logger.isInfoEnabled()) logger.info("Obsolete attribute created (" + stunAttributeType.toString() + ").");
			}
			if(!stunAttributeType.isAvailable(stunMessage.getMessageVersion())){
				logger.error("Attribute not available for this version of STUN");
			}
		}
	}
	
	protected STUNAttribute(STUNMessage stunMessage, ExtendedBitSet stunAttributeTypeEBS, int messagePosition){
		this.messagePosition = messagePosition;
		attribute.set(0, stunAttributeTypeEBS);
		
		this.stunMessage = stunMessage;
		setLength(0);
	}
	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.message.TLVAttribute#getLength()
	 */
	public int getLength() {
		ExtendedBitSet retEBS = attribute.get(16, 32); 
		return retEBS.toInt();
	}

	/* 
	 *    The value in the length field MUST contain the length of the Value
	 *    part of the attribute, prior to padding, measured in bytes.  Since
	 *    STUN aligns attributes on 32-bit boundaries, attributes whose content
	 *    is not a multiple of 4 bytes are padded with 1, 2, or 3 bytes of
	 *    padding so that its value contains a multiple of 4 bytes.  The
	 *    padding bits are ignored, and may be any value.
	 * @see pl.edu.pjwstk.net.message.TLVAttribute#setLength(int)
	 */
	protected void setLength(int length){
		ExtendedBitSet attlen = new ExtendedBitSet(Integer.toBinaryString(length),16,false,true);
		attribute.set(16, attlen);	
	}
	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.message.TLVAttribute#getType()
	 */
	public ExtendedBitSet getType() {
		return (attribute.get(0, 16));
	}
	
	public STUNAttributeType getSTUNAttributeType(){
		return STUNAttributeType.valueOf(attribute.get(0, 16));
	}

	/* (non-Javadoc)
	 * @see pl.edu.pjwstk.net.message.TLVAttribute#getValue()
	 */
	public ExtendedBitSet getValue() {
		return attribute.get(32,attribute.getFixedLength());
	}
	
	protected int calculateAttributeSize(int sizeinbytes)
	{
		return (int)(Math.ceil(((double)sizeinbytes)/4)*32);
	}
	
	/**
	 * @param bs
	 * @return
	 */
	public static boolean isValidType(ExtendedBitSet bs){
		return false;
	}
	
	public boolean isObsolete(){
		return (stunAttributeType != null ? stunAttributeType.isObsolete(stunMessage.getMessageVersion()) : null);
	}

	public boolean tryParse(int startPosition, ExtendedBitSet ebs) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean tryParse(InputStream inputStream) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean tryParse(byte[] bytes) {
		// TODO Auto-generated method stub
		return false;
	}
}
