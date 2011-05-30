/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.credential.ProtocolValidator;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.p2pp.util.Arrays;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNFingerprint extends STUNAttribute implements TLVAttribute, ProtocolValidator {
	public STUNFingerprint(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.FINGERPRINT, messagePosition);
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public STUNFingerprint(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.NONCE, valueEBS, messagePosition);
	}
	
	public String toString() {
		return ("0x" + Arrays.byteArrayToHexString(this.getValue().toByteArray()));
	}
}
