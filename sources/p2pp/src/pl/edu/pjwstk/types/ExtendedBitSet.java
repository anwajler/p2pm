/**
 * 
 */
package pl.edu.pjwstk.types;

import java.util.BitSet;

/**
 * @author Robert Strzelecki rstrzele@gmail.com
 * package pl.edu.pjwstk.net
 */
public class ExtendedBitSet extends BitSet {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ExtendedBitSet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 953650824324900576L;

	public static ExtendedBitSet add(ExtendedBitSet bs1, ExtendedBitSet bs2) {
		ExtendedBitSet ebs = new ExtendedBitSet(bs1.getFixedLength()+bs2.getFixedLength());
		int i = 0;
		for(i = 0; i<bs1.getFixedLength(); i++ ){
			ebs.set(i, bs1.get(i));
		}
		for(i = 0; i<bs2.getFixedLength(); i++){
			ebs.set(i+bs1.getFixedLength(),bs2.get(i));
		}		
		return ebs;
	}
	
	/**
	 * The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
	 * @return a bitset containing the values in bytes. 
	 */
	public static ExtendedBitSet fromByteArray(byte[] bytes,boolean canGrow) {
		ExtendedBitSet bits = new ExtendedBitSet(bytes.length * 8,canGrow);
		bits.set(0,bytes);
		return bits;
	}
	private int fixedLength = 0;
	
	private int canGrowToLength = 0;
	
	private boolean canGrow = false;

	/**
	 * 
	 */
	public ExtendedBitSet(){
		super();
	}

	public ExtendedBitSet(BitSet orgBS, int fixedLength) {
		this(fixedLength,true);
		for (int i = 0; i<fixedLength;i++){
			this.set(i,orgBS.get(i));
		}
	}

	/**
	 * @param n
	 */
	public ExtendedBitSet(int n){
		this("",0,false,false);
		this.canGrowToLength = n;
	}

	/**
	 * @param n
	 * @param canGrow
	 */
	public ExtendedBitSet(int n, boolean canGrow){
		this("",0,canGrow,false);
		this.canGrowToLength = n; 
	}

	public ExtendedBitSet(String str){
		this(str, str.length()*8,false,true);
	}
	
	public ExtendedBitSet(String str, boolean canGrow){
		this(str, str.length()*8,canGrow,true);
	}
	
	public ExtendedBitSet(String str, int fixedLength, boolean canGrow, boolean initialize){
		super(str.length());
		this.canGrowToLength = fixedLength;
		this.canGrow = canGrow;
		if (str.length() > fixedLength){
			str = str.substring(0, fixedLength - 1);
			logger.error("String cutted to match given fixedLength (ERROR)");
		}
		while(str.length()<fixedLength && initialize){
			str = "0" + str;
		}
		Initialize(str);

	}
	
	public void add(ExtendedBitSet ebs) {
		try {
			int fl = getFixedLength();
			this.checkLength(fl + ebs.getFixedLength());
			for(int i = 0; i < ebs.getFixedLength();i++){
				this.set(fl + i, ebs.get(i));
			}
		} catch (LengthOverflowException e) {
			logger.warn("Maximum allowed length of ExtendedBitSet = " + this.canGrowToLength + ". Incorrect value = " +
                    (getFixedLength() + ebs.getFixedLength()) ,e);
		}
	}
	
	public void and(ExtendedBitSet set){
		try {
			checkLength(set.getFixedLength());
			super.and(set);
			setGreaterFixedLength(set.getFixedLength());
		} catch (LengthOverflowException e) {
			logger.error("",e);
		}
	}

	public void andNot(ExtendedBitSet set){
		try {
			checkLength(set.getFixedLength());
			super.andNot(set);
			setGreaterFixedLength(set.getFixedLength());
		} catch (LengthOverflowException e) {
			logger.error("",e);
		}
	}
	
	@Override
	public int cardinality() {
		return super.cardinality();
	}

	private void checkLength(int lengthOfBitSet) throws LengthOverflowException{
		if (lengthOfBitSet > this.canGrowToLength){
			if (canGrow) {
				setFixedLength(lengthOfBitSet);
			} else {
				throw new LengthOverflowException();
			}				
		}		
	}

	@Override
	public void clear() {
		super.clear();
		setGreaterFixedLength(canGrowToLength - 1);
	}

	@Override
	public void clear(int bitIndex) {
		try {
			checkLength(bitIndex);
			super.clear(bitIndex);
			setGreaterFixedLength(bitIndex);
		} catch (LengthOverflowException e) {
			logger.error("",e);
		}
	}

	@Override
	public void clear(int fromIndex, int toIndex) {
		try {
			checkLength(toIndex);
			super.clear(fromIndex, toIndex);
			setGreaterFixedLength(toIndex);
		} catch (LengthOverflowException e) {
			logger.error("",e);
		}
	}

	@Override
	public Object clone() {
		return super.clone();
	}

	@Override
	public boolean equals(Object obj) {
        if (obj instanceof ExtendedBitSet) {
		    return super.equals(obj);
        } else {
            return false;
        }
	}

	@Override
	public void flip(int bitIndex) {
		try {
			checkLength(bitIndex);
			super.flip(bitIndex);
			setGreaterFixedLength(bitIndex);
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}
	}

	@Override
	public void flip(int fromIndex, int toIndex) {
		try {
			checkLength(toIndex);
			super.flip(fromIndex, toIndex);
			setGreaterFixedLength(toIndex);
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}
	}

	@Override
	public boolean get(int bitIndex) {
		return super.get(bitIndex);
	}

	@Override
	public ExtendedBitSet get(int fromIndex, int toIndex) {
		ExtendedBitSet retEBS = new ExtendedBitSet(super.get(fromIndex, toIndex), toIndex - fromIndex);
		return retEBS;
	}

	/**
	 * @return the fixedLength
	 */
	public int getFixedLength() {
		return fixedLength;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	private void Initialize(String str){
		for (int i = 0; i < str.length(); i++){
			this.set(i,(str.charAt(i) == '1' ? true : false));
		}
	}

	@Override
	public boolean intersects(BitSet set) {
		return super.intersects(set);
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public int length() {
		return super.length();
	}

	@Override
	public int nextClearBit(int fromIndex) {
		return super.nextClearBit(fromIndex);
	}

	@Override
	public int nextSetBit(int fromIndex) {
		return super.nextSetBit(fromIndex);
	}

	public void or(ExtendedBitSet set) {
		try {
			checkLength(set.getFixedLength());
			super.or(set);
			setGreaterFixedLength(set.getFixedLength());
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}
	}

	@Override
	public void set(int bitIndex) {
		try {
			checkLength(bitIndex);
			super.set(bitIndex);
			setGreaterFixedLength(bitIndex);
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}
	}

	@Override
	public void set(int bitIndex, boolean value) {
		try {
			checkLength(bitIndex);
			super.set(bitIndex, value);
			setGreaterFixedLength(bitIndex);
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}
	}

	public void set(int fromIndex, byte[] bytes){
		for (int i=0; i<bytes.length*8; i++) {
			if ((bytes[i/8]&(1<<(8 - i%8 - 1))) > 0) {
				this.set(i);
			} else {
				this.clear(i);
			}
		}
	}

	public void set(int fromIndex, ExtendedBitSet values) {
		try {
			checkLength(fromIndex + values.getFixedLength());
			for (int i = 0; i < values.getFixedLength(); i++){
				super.set(fromIndex + i, values.get(i));
			}
			setGreaterFixedLength(fromIndex + values.getFixedLength() - 1);
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}		
	}

	@Override
	public void set(int fromIndex, int toIndex) {
		try {
			checkLength(toIndex);
			super.set(fromIndex, toIndex);
			setGreaterFixedLength(toIndex);
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}		
	}
	
	@Override
	public void set(int fromIndex, int toIndex, boolean value) {
		try {
			checkLength(toIndex);
			super.set(fromIndex, toIndex, value);
			setGreaterFixedLength(toIndex);
		} catch (LengthOverflowException e) {
			logger.debug("",e);
		}		
	}
	
	/**
	 * @param fixedLength the fixedLength to set
	 */
	private void setFixedLength(int fixedLength) {
		this.fixedLength = fixedLength;
		this.canGrowToLength = Math.max(this.canGrowToLength, this.fixedLength);
	}
	
	private void setGreaterFixedLength(int fixedLength) {
		if (((fixedLength + 1 <= this.canGrowToLength) && !this.canGrow) || this.canGrow)  {
			setFixedLength(Math.max(getFixedLength(), fixedLength + 1));
		}
	}
	
	@Override
	public int size() {
		return super.size();
	}

	public byte[] toByteArray() {
	    byte[] bytes = new byte[
	                            (this.getFixedLength() % 8 == 0 ? 
	                            	this.getFixedLength()/8 : 
	                            	this.getFixedLength()/8 +1)];
	    for (int i=0; i<this.getFixedLength(); i++) {
	        if (this.get(i)) {
	            bytes[i/8] |= 1<<(7 - i%8);
	        }
	    }
	    return bytes;
	}
	
	/**
	 * The most significant bit in the result is guaranteed not to be a 1
	 * (since BitSet does not support sign extension).
	 * The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
	 * The bit at index 0 of the bit set is assumed to be the least significant bit.	 * @param bits
	 * @return a byte array of at least length 1.
	 */
	public byte[] toByteArrayBigEndian() {
	    byte[] bytes = new byte[this.getFixedLength()/8];
	    for (int i=0; i<this.getFixedLength(); i++) {
	        if (this.get(i)) {
	            bytes[bytes.length-i/8-1] |= 1<<(i%8);
	        }
	    }
	    return bytes;
	}

	/**
	 * @return Integer representation of the ExtendedBitSet
	 */
	public int toInt() {
		
		if (this.getFixedLength() == 0) return 0; 
		
		//FIXME Exception? or something else...
	    int[] temp = new int[(this.getFixedLength() % 32 == 0 ? 
	    		this.getFixedLength() / 32 : 
	    		this.getFixedLength() / 32 + 1)];

	    for (int i = 0; i < temp.length; i++)
	      for (int j = 0; j < 32; j++)
	        if (this.get(i * 32 + j))
	          temp[i] |= 1 << this.getFixedLength() % 32 - 1 - j;

	    return temp[0];
	  }

	/**
	 * @return ExtendedBitset as a integer array
	 */
	public int[] toIntArray() {
	    int[] temp = new int[this.getFixedLength() / 32];

	    for (int i = 0; i < this.getFixedLength(); i++)
	      for (int j = 0; j < 32; j++)
	        if (this.get(i * 32 + j))
	          temp[i] |= 1 << j;

	    return temp;
	  }
	/**
	 * @return BigEndian representation of the ExtendedBitSet
	 */
	public int toIntBigEndian() {
		//FIXME Exception? or something else...
	    int[] temp = new int[(this.getFixedLength() % 32 == 0 ? 
	    		this.getFixedLength() / 32 : 
	    		this.getFixedLength() / 32 + 1)];

	    for (int i = 0; i < temp.length; i++)
	      for (int j = 0; j < 32; j++)
	        if (this.get(i * 32 + j))
	          temp[i] |= 1 << j;

	    return temp[0];
	  }

	@Override
	public String toString() {
		String str = "";
		for(int i = 0; i < this.getFixedLength(); i++) {
			str += (this.get(i) ? "1" : "0");
		}
		return str;
	}

	public String toStringBytes() {
		String str = "";
		for(int i = 0; i < this.getFixedLength(); i++) {
			str += (i % 32 == 0 || i == 0 ? new String(new byte[]{13,10}) + String.format("%4d",i) + " ": "");
			str += (this.get(i) ? "1" : "0");
			str += ((i + 1) % 8 == 0 && i>0 && i % 32 != 0 ? " " : "");
		}
		return str;
	}

	public void xor(ExtendedBitSet set) {
		try {
			checkLength(set.getFixedLength());
			super.xor(set);
			setGreaterFixedLength(set.getFixedLength() - 1);
		} catch (LengthOverflowException e) {
			logger.error("",e);
		}
	}

	public void shrink(int fromIndex) {
		this.clear(fromIndex, fixedLength);
		setFixedLength(fromIndex);
	}
}
