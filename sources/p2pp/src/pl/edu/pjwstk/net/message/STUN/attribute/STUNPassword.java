/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNPassword extends STUNAttribute implements TLVAttribute {

	/**
	 * @param stunMessage
	 */
	public STUNPassword(STUNMessage stunMessage, int messagePosition) {
		super(stunMessage, STUNAttributeType.PASSWORD, messagePosition);
	}

}
