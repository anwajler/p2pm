/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN;

import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN
 */
public enum STUNMessageClass {
	REQUEST("00"),
	INDICATION("01"),
	SUCCESS_RESPONSE("10"),
	ERROR_RESPONSE("11");
	
	//private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNMessageClass.class);
	
	private ExtendedBitSet messageClass = new ExtendedBitSet(2);
	
	public ExtendedBitSet getMessageClass() {
		return messageClass;
	}

	STUNMessageClass(String bitString){
		try{
			if (bitString.length() != 2) {
				throw new Exception ("The length of stream must equal 2.");
			}
			for(int i=0;i<bitString.length();i++){
				if(bitString.charAt(i) == '0'){
					messageClass.set(i,false);
				} else if (bitString.charAt(i) == '1') {
					messageClass.set(i,true);					
				} else {
					throw new Exception ("String isn't stream of bits.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static STUNMessageClass valueOf(ExtendedBitSet messClassBitSet) throws Exception {
		if (messClassBitSet.getFixedLength() != 2) {
			throw new Exception ("The size of BitSet must equal 2. (" + messClassBitSet.length() + ")" );
		}		
		for (STUNMessageClass messClass : STUNMessageClass.values()) {
			if (messClass.getMessageClass().equals(messClassBitSet)) {
				return messClass;
			}
		}
		throw new Exception("Not known STUNMessageClass type.");
	}
}