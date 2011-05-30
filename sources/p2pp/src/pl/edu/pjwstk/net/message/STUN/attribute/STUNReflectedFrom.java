/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNReflectedFrom extends STUNAttributeAddress implements TLVAttribute {

	/**
	 * @param stunMessage
	 */
	public STUNReflectedFrom(STUNMessage stunMessage, int messagePosition) {
		super(stunMessage, STUNAttributeType.REFLECTED_FROM, messagePosition);
	}

}
