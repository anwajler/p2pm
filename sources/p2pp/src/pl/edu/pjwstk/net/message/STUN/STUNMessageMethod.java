package pl.edu.pjwstk.net.message.STUN;

import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN
 * 
 *    0x0001  :  Binding Request
 *    0x0101  :  Binding Response
 *    0x0111  :  Binding Error Response
 *    0x0002  :  Shared Secret Request
 *    0x0102  :  Shared Secret Response
 *    0x0112  :  Shared Secret Error Response
 *       0                             1
 *       0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
 *      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 *      |P |P |M |M |M |M |M |C |M |M |M |C |M |M |M |M |
 *      |0 |1 |11|10|9 |8 |7 |1 |6 |5 |4 |0 |3 |2 |1 |0 |
 *      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 *       0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  1 | Binding Request  (RFC 5389 Request class = 00)
 *       0  0  0  0  0  0  0  1  0  0  0  0  0  0  0  1 | Binding Response (RFC 5389 Response class = 01)
 *       0  0  0  0  0  0  0  1  0  0  0  1  0  0  0  1 | Binding Error Response (RFC 5389 Error class = 11)
 *      
 *      
 *      
 *      
 *      P - protocol bits = STUN always 0 0
 *      M - method bits defined in RFC 5389
 *      C - class bits defined RFC 5389
 */
public enum STUNMessageMethod {
	BINDING("000000000001");
	
	//private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(STUNMessageMethod.class);
	
	private ExtendedBitSet method = new ExtendedBitSet(12);
	
	public ExtendedBitSet getMessageMethod() {
		return method;
	}

	STUNMessageMethod(String bitString){
		try{
			if (bitString.length() != 12) {
				throw new Exception ("The length of stream must equal 12.");
			}
			for(int i=0;i<bitString.length();i++){
				if(bitString.charAt(i) == '0'){
					method.set(i,false);
				} else if (bitString.charAt(i) == '1') {
					method.set(i,true);
				} else {
					throw new Exception ("String isn't stream of bits");
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static STUNMessageMethod valueOf(ExtendedBitSet messMethodBitSet) throws Exception {
		for (STUNMessageMethod messMethod : STUNMessageMethod.values()) {
			if (messMethod.getMessageMethod().equals(messMethodBitSet)) {
				return messMethod;
			}
		}
		throw new Exception("Not known STUNMessageMethod type.");
	}
}
