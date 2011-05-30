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
public class TURNDontFragment extends TURNAttribute {

	public TURNDontFragment(TURNMessage turnMessage,
			ExtendedBitSet valueEBS,
			int messagePosition) {
		super(turnMessage, TURNAttributeType.DONT_FRAGMENT, valueEBS, messagePosition);
	}
	
	public TURNDontFragment(TURNMessage turnMessage, int messagePosition){
		super(turnMessage, TURNAttributeType.DONT_FRAGMENT, messagePosition);
	}

}
