/**
 *
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import java.io.UnsupportedEncodingException;

import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 *         package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNUsername extends STUNAttribute implements TLVAttribute {
	public STUNUsername(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.USERNAME, messagePosition);
	}
	public void setUsername(String Username){
		try {
			//ExtendedBitSet ebs = ExtendedBitSet.fromByteArray(,false);
			
			ExtendedBitSet usernameBS = new ExtendedBitSet("",
					(this.calculateAttributeSize(Username.getBytes("UTF-8").length)),
					false, true);
			usernameBS.set(0,Username.getBytes("UTF-8"));
			attribute.set(32, usernameBS);
			setLength(Username.getBytes("UTF-8").length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
