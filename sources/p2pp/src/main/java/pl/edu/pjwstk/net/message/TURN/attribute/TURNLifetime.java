/**
 * 
 */
package pl.edu.pjwstk.net.message.TURN.attribute;

import pl.edu.pjwstk.net.message.TURN.TURNAttribute;
import pl.edu.pjwstk.net.message.TURN.TURNAttributeType;
import pl.edu.pjwstk.net.message.TURN.TURNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.TURN.attribute
 */
public class TURNLifetime extends TURNAttribute {

	private int lifeTime;
	public TURNLifetime(TURNMessage turnMessage,
			ExtendedBitSet valueEBS,
			int messagePosition) {
		super(turnMessage, TURNAttributeType.LIFETIME, valueEBS, messagePosition);
		lifeTime = valueEBS.toInt();
	}
	
	public TURNLifetime(TURNMessage turnMessage, int messagePosition){
		super(turnMessage, TURNAttributeType.LIFETIME, messagePosition);
	}
	
	public int getLifeTime(){
		return this.lifeTime;
	}
	public void setLifeTime(int seconds)
	{
		this.lifeTime = seconds;
		ExtendedBitSet lifetimeBS = new ExtendedBitSet(Integer.toBinaryString(this.lifeTime),
				32,
				false,true);
		attribute.set(32, lifetimeBS);
		setLength(lifetimeBS.getFixedLength()/8);
	}
	
	public String toString(){
		return "(" + this.lifeTime + " s )";
	}
}
