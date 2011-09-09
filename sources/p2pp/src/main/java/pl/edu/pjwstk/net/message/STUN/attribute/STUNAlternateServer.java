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
public class STUNAlternateServer extends STUNAttribute implements TLVAttribute {
	public STUNAlternateServer(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.ALTERNATE_SERVER, messagePosition);
	}
}
