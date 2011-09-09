/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import java.io.UnsupportedEncodingException;

import pl.edu.pjwstk.net.ProtocolObject;
import pl.edu.pjwstk.net.message.TLVAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttribute;
import pl.edu.pjwstk.net.message.STUN.STUNAttributeType;
import pl.edu.pjwstk.net.message.STUN.STUNMessage;
import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN.attribute
 */
public class STUNNonce extends STUNAttribute implements TLVAttribute {
	public STUNNonce(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.NONCE, messagePosition);
	}
	
	public STUNNonce(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.NONCE, valueEBS, messagePosition);
	}
	
	public String toString() {
		try {
			return (new String(this.getValue().toByteArray(),0,this.getValue().toByteArray().length,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return "UnsupportedEncoding";
		}
	}
}
