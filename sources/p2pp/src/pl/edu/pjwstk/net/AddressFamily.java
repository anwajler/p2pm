/**
 * 
 */
package pl.edu.pjwstk.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net
 */
public enum AddressFamily {
	IPV4("00000001"),
	IPV6("00000010");
	
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AddressFamily.class);
	ExtendedBitSet addressFamily = new ExtendedBitSet(8,false);
	
	AddressFamily(String bitString){
		try{
			if (bitString.length() != 8) {
				throw new Exception ("The length of stream must equal 8.");
			}
			for(int i=0;i<bitString.length();i++){
				if(bitString.charAt(i) == '0'){
					addressFamily.set(i,false);
				} else if (bitString.charAt(i) == '1') {
					addressFamily.set(i,true);					
				} else {
					throw new Exception ("String isn't stream of bits at position " + i + ".");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ExtendedBitSet getAddressFamilyBitSet(){
		return addressFamily;
	}
	
	public static AddressFamily getAddressFamily(InetAddress address){
		if (address instanceof Inet4Address){
			if (logger.isDebugEnabled()) logger.debug("Checking familly address: IPv4");
			return AddressFamily.IPV4;
		}
		if (address instanceof Inet6Address){
			if (logger.isDebugEnabled()) logger.debug("Checking familly address: IPv6");
			return AddressFamily.IPV6;
		}
		return null;
	}
}
