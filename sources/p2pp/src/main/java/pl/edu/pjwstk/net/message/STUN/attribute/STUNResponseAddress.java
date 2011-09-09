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
public class STUNResponseAddress extends STUNAttributeAddress implements TLVAttribute {

	protected STUNResponseAddress(STUNMessage stunMessage, int messagePosition) {
		super(stunMessage, STUNAttributeType.RESPONSE_ADDRESS, messagePosition);
	}

}
