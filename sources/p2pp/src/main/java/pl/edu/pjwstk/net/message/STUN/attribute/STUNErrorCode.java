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
public class STUNErrorCode extends STUNAttribute implements TLVAttribute {
	public STUNErrorCode(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.ERROR_CODE, messagePosition);
	}
	
	public STUNErrorCode(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.ERROR_CODE, valueEBS, messagePosition);
	}
	
	public int getErrorCode()
	{
		return this.getValue().get(21, 24).toInt() * 100 + this.getValue().get(24, 32).toInt();
	}
	
	public String getErrorDescription()
	{
		try {
			return new String(this.getValue().get(32,this.getValue().length()).toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String toString(){
		return "ERROR (" + this.getErrorCode() + ") " + this.getErrorDescription();
	}
}
