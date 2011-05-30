package pl.edu.pjwstk.net.message;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.types.ExtendedBitSet;

public abstract class ProtocolAttribute extends ProtocolObject implements TLVAttribute{
	protected ExtendedBitSet attribute = new ExtendedBitSet(32,true);
	protected int messagePosition = 0;
	protected boolean valueInitialized = false;

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(byte[] attribute) {
		this.attribute = ExtendedBitSet.fromByteArray(attribute,true);
	}
	
	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(ExtendedBitSet attribute) {
		this.attribute = attribute;
	}
	
	public int getMessagePosition(){
		return messagePosition;
	}
	public void setMessagePosition(int messagePosition) {
		this.messagePosition = messagePosition;
	}
	
	/**
	 * @return the attribute
	 */
	public ExtendedBitSet getAttribute() {
		return attribute;
	}
	
	protected abstract void setLength(int length);
	
	public boolean isValueInitialized(){
		return valueInitialized;
	}
	
	public String toString() {
		return new String(attribute.get(32, attribute.getFixedLength()).toByteArray());
	}
}
