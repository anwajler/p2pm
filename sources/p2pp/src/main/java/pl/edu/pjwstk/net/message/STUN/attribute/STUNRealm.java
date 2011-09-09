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
public class STUNRealm extends STUNAttribute implements TLVAttribute {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNRealm.class);

	private String realmName;
	
	public STUNRealm(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.REALM, messagePosition);
	}
	
	public STUNRealm(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.REALM, valueEBS, messagePosition);
		try {
			realmName = new String(valueEBS.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
	}
	
	public String toString() {
		return this.realmName;
	}

	/**
	 * @param realmName the realmName to set
	 */
	public void setRealm(String realmName) {
		this.realmName = realmName;
		try {
			ExtendedBitSet realmBS = new ExtendedBitSet("",
					this.calculateAttributeSize(realmName.length()),
					false,true);
			realmBS.set(0,realmName.getBytes("UTF-8"));
			attribute.set(32, realmBS);
			setLength(realmName.length());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
