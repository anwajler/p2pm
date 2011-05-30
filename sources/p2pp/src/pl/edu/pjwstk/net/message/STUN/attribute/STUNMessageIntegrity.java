/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN.attribute;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
public class STUNMessageIntegrity extends STUNAttribute implements
		TLVAttribute, ProtocolValidator {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNMessageIntegrity.class);
	
	public STUNMessageIntegrity(STUNMessage stunMessage, int messagePosition){
		super(stunMessage, STUNAttributeType.MESSAGE_INTEGRITY, messagePosition);
		ExtendedBitSet zeroEBS = new ExtendedBitSet("",160,false,true);
		attribute.set(32, zeroEBS);
		setLength(zeroEBS.getFixedLength()/8);
		
	}
	
	public STUNMessageIntegrity(ProtocolObject stunMessage, ExtendedBitSet valueEBS, int messagePosition){
		super((STUNMessage) stunMessage, STUNAttributeType.MESSAGE_INTEGRITY, valueEBS, messagePosition);
	}
	
	public String toString() {
		return ("0x" + Arrays.byteArrayToHexString(this.getValue().toByteArray()));
	}
	
	public boolean isValid(){
		return false;
	}
	
	//public boolean isValid(ProtocolObject protocolObject){
	//	return false;
	//}
	
	public boolean computeValue(STUNMessage stunMessage){
        try {
        	
            // get an HMAC-SHA1 key from the raw key bytes 
        	MessageDigest md5;
        	md5 = MessageDigest.getInstance("MD5"); 
        	byte[] key = md5.digest("rstrzele:pjwstk.edu.pl:33600".getBytes());

        	logger.debug(Arrays.byteArrayToHexString(key));
            SecretKeySpec signingKey 
            	= new SecretKeySpec(key, "HmacSHA1"); 
			
            byte[] message = new byte[this.messagePosition/8];
            
            logger.debug("Mesage size = " + stunMessage.toByte().length + " Hmac message size = " + this.messagePosition/8);
            
            System.arraycopy(stunMessage.toByte(), 0, message, 0, this.messagePosition/8); 
            
            // compute HMAC SHA1 of message
            Mac mac = Mac.getInstance("HmacSHA1"); 
            logger.debug(Arrays.byteArrayToHexString(message));
            mac.init(signingKey); 
            byte[] result = mac.doFinal(message); 
            
            if (result.length != 20){
            	logger.debug("Incorrect message integrity length " + result + "(" + result.length + ")");
            	return false;
            }
            
			ExtendedBitSet resultBS = new ExtendedBitSet(
						20*8,
						false);
			resultBS.set(0,result);
			attribute.set(32, resultBS);
			setLength(20);
			logger.debug("Changed MESSAGE-INTEGRITY attribute to " + this.toString());
	        
     	} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
        return true;
	}
}
