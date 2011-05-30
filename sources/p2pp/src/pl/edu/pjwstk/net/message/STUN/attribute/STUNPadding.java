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
public class STUNPadding extends STUNAttribute implements TLVAttribute {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNSoftware.class);

	private String padding;
	
	public STUNPadding(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.PADDING, messagePosition);
	}
	public STUNPadding(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.PADDING, valueEBS, messagePosition);
		try {
			padding = new String(valueEBS.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
	}
	
	public String toString() {
		return this.padding;
	}
	
	public void setPadding(String padding) {
		this.padding = padding;
		try {
			ExtendedBitSet realmBS = new ExtendedBitSet("",
					this.calculateAttributeSize(padding.getBytes("UTF-8").length),
					false, true);
			realmBS.set(0,padding.getBytes("UTF-8"));
			attribute.set(32, realmBS);
			setLength(padding.getBytes("UTF-8").length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}