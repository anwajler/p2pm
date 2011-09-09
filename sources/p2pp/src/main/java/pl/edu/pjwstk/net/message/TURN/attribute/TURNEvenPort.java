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
public class TURNEvenPort extends TURNAttribute {

	public TURNEvenPort(TURNMessage turnMessage,
			ExtendedBitSet valueEBS,
			int messagePosition) {
		super(turnMessage, TURNAttributeType.EVEN_PORT, valueEBS, messagePosition);
		// TODO Auto-generated constructor stub
	}


}
