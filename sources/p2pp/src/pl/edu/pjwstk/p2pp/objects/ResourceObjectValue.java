package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * ResourceObjectValue. Isn't defined in P2PP specification (draft 01) but is used in reference implementation. Looks
 * very useful.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class ResourceObjectValue extends GeneralObject {

	@Override
	public String toString() {
		return super.toString() + "ResourceObjectValue=[value=[" + ByteUtils.byteArrayToHexString(value) + "]]";
	}

	/**
	 * Byte array containing a value of this object.
	 */
	private byte[] value;

	/**
	 * Constructor that doesn't set any object as a value of this object.
	 */
	public ResourceObjectValue() {
		super(GeneralObject.RESOURCE_OBJECT_VALUE_OBJECT_TYPE);
	}

	/**
	 * Constructor for resource object value.
	 * 
	 * @param value
	 *            Value of this object.
	 */
	public ResourceObjectValue(byte[] value) {
		super(GeneralObject.RESOURCE_OBJECT_VALUE_OBJECT_TYPE);
		this.value = value;
	}

	/**
	 * Returns value of this object.
	 * 
	 * @return
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * Sets value of this object.
	 * 
	 * @param value
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException(subobject.getClass().getName()
				+ " can't be a subobject for ResourceObjectValue.");

	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] data = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtByteIndex(value, data, currentIndex / 8);
		// currentIndex += value.getBitsCount();

		return data;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + value.length * 8;
	}
}
