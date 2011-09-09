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
public class STUNSoftware extends STUNAttribute implements TLVAttribute {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNSoftware.class);

	private String softwareName;
	
	public STUNSoftware(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.SOFTWARE, messagePosition);
	}
	public STUNSoftware(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.SOFTWARE, valueEBS, messagePosition);
		try {
			softwareName = new String(valueEBS.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
	}
	
	public String toString() {
		return this.softwareName;
	}

	/**
	 * @param realmName the realmName to set
	 */
	public void setSoftware(String softwareName) {
		this.softwareName = softwareName;
		try {
			ExtendedBitSet realmBS = new ExtendedBitSet("",
					this.calculateAttributeSize(softwareName.getBytes("UTF-8").length),
					false, true);
			realmBS.set(0,softwareName.getBytes("UTF-8"));
			attribute.set(32, realmBS);
			setLength(softwareName.getBytes("UTF-8").length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
