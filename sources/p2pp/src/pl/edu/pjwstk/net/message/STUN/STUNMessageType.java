/**
 * 
 */
package pl.edu.pjwstk.net.message.STUN;

import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message.STUN
 * 
 * RFC 3489
 *                                          Binding  Shared  Shared  Shared
 *                        Binding  Binding  Error    Secret  Secret  Secret
 *    Att.                Req.     Resp.    Resp.    Req.    Resp.   Error
 *                                                                   Resp.
 *    _____________________________________________________________________
 *    MAPPED-ADDRESS      N/A      M        N/A      N/A     N/A     N/A
 *    RESPONSE-ADDRESS    O        N/A      N/A      N/A     N/A     N/A
 *    CHANGE-REQUEST      O        N/A      N/A      N/A     N/A     N/A
 *    SOURCE-ADDRESS      N/A      M        N/A      N/A     N/A     N/A
 *    CHANGED-ADDRESS     N/A      M        N/A      N/A     N/A     N/A
 *    USERNAME            O        N/A      N/A      N/A     M       N/A
 *    PASSWORD            N/A      N/A      N/A      N/A     M       N/A
 *    MESSAGE-INTEGRITY   O        O        N/A      N/A     N/A     N/A
 *    ERROR-CODE          N/A      N/A      M        N/A     N/A     M
 *    UNKNOWN-ATTRIBUTES  N/A      N/A      C        N/A     N/A     C
 *    REFLECTED-FROM      N/A      C        N/A      N/A     N/A     N/A
 * 
 * RFC 5389
 * 
 * TODO 
 */
public enum STUNMessageType {
	RFC3489(false,128,"",0),
	RFC5389(true,96, Integer.toBinaryString(0x2112A442),32);
	private boolean isMagicCookie;
	private int lengthOfTransactionField;
	private ExtendedBitSet protocolMagicCookie;
	
	private STUNMessageType(boolean isMagicCookie, int lengthOfTransactionField, String binaryMagicCookie, int lengthOfMagicCookie){
		this.isMagicCookie = isMagicCookie;
		this.lengthOfTransactionField = lengthOfTransactionField;
		this.setProtocolMagicCookie(new ExtendedBitSet(binaryMagicCookie,lengthOfMagicCookie,false,true));
	}

	/**
	 * @return the isMagicCookie
	 */
	public boolean isMagicCookie() {
		return isMagicCookie;
	}

	/**
	 * @return the lengthOfTransactionField
	 */
	public int getLengthOfTransactionField() {
		return lengthOfTransactionField;
	}

	/**
	 * @param protocolMagicCookie the protocolMagicCookie to set
	 */
	private void setProtocolMagicCookie(ExtendedBitSet protocolMagicCookie) {
		this.protocolMagicCookie = protocolMagicCookie;
	}

	/**
	 * @return the protocolMagicCookie
	 */
	public ExtendedBitSet getProtocolMagicCookie() {
		return protocolMagicCookie;
	}
}
