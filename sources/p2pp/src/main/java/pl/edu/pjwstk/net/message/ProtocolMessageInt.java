/**
 * 
 */
package pl.edu.pjwstk.net.message;

import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message
 */
public interface ProtocolMessageInt {
	public ExtendedBitSet getTransactionID();
	public void setTransactionID(ExtendedBitSet transactionID);
	public byte[] toByte();
	public boolean isMagicCookie();
}
