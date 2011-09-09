/**
 * 
 */
package pl.edu.pjwstk.net.message.TURN;

import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.TURN
 */
public abstract class TURNAttribute extends STUNAttribute {

	/**
	 * @param stunMessage
	 * @param stunAttributeType
	 * @param valueEBS
	 */
	public TURNAttribute(TURNMessage turnMessage,
			TURNAttributeType turnAttributeType, ExtendedBitSet valueEBS, int messagePosition) {
		super(turnMessage, turnAttributeType.getType(), valueEBS, messagePosition);
	}
	
	/**
	 * @param stunMessage
	 * @param stunAttributeType
	 */
	public TURNAttribute(TURNMessage turnMessage,
			TURNAttributeType turnAttributeType, int messagePosition) {
			super(turnMessage, turnAttributeType.getType(), messagePosition);
	}
	
	public TURNAttributeType getTURNAttributeType(){
		return TURNAttributeType.valueOf(attribute.get(0, 16));
	}

}
