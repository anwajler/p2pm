/**
 * 
 */
package pl.edu.pjwstk.net.message;

import pl.edu.pjwstk.types.ExtendedBitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net.message
 */
public interface TLVAttribute {

	/**
	 * @return
	 */
	public ExtendedBitSet getType();
	
	/**
	 * @return
	 */
	public int getLength();
	
	/**
	 * @return
	 */
	public ExtendedBitSet getValue();
}
