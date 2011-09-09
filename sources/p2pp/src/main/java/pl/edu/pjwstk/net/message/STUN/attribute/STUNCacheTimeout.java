package pl.edu.pjwstk.net.message.STUN.attribute;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

public class STUNCacheTimeout extends STUNAttribute implements TLVAttribute {

	private int lifeTime;
	
	public STUNCacheTimeout(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.CACHE_TIMEOUT, messagePosition);
	}
	public STUNCacheTimeout(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.CACHE_TIMEOUT, valueEBS,  messagePosition);
		lifeTime = valueEBS.toInt();
	}
	
	public int getLifeTime(){
		return this.lifeTime;
	}
	public void setLifeTime(int seconds)
	{
		this.lifeTime = seconds;
		ExtendedBitSet realmBS = new ExtendedBitSet(Integer.toBinaryString(this.lifeTime),
				32,
				false,true);
		attribute.set(32, realmBS);
	}
	
	public String toString(){
		return "(" + this.lifeTime + " s )";
	}
}
