package pl.edu.pjwstk.p2pp.objects;

import java.util.Arrays;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * ResourceID as defined in P2PP specification (draft 01). It is an identifier of the resource. For DHTs, it is a
 * fixed-length output of a hash function. For unstructured networks, its length is variable.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class ResourceID extends GeneralObject {

	private byte[] resourceID;

	/**
	 * Constructor for resourceID.
	 * 
	 * @param resourceID
	 *            ResourceID in bytes.
	 */
	public ResourceID(byte[] resourceID) {
		super(GeneralObject.RESOURCE_ID_OBJECT_TYPE);

		this.resourceID = resourceID;
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int firstBitIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtBitIndex(resourceID, bytes, firstBitIndex);
		// firstBitIndex += resourceID.getBitsCount;

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + resourceID.length * 8;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException("ResourceID object can't " + "contain object of "
				+ subobject.getClass().getName() + " type.");
	}

	/**
	 * ResourceID objects are considered equal if they're of the same type (ResourceID) and resourceID byte arrays are
	 * of equal length and each array element is equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ResourceID)) {
			return false;
		} else {
			ResourceID resID = (ResourceID) obj;
			byte[] objResID = resID.getResourceID();
            return objResID.length == resourceID.length && Arrays.equals(objResID, resourceID);
		}

	}

	@Override
	public int hashCode() {
		StringBuffer buffer = new StringBuffer("");
        for (byte resourceIDByte : resourceID) {
			buffer.append(resourceIDByte);
		}
		return buffer.toString().hashCode();
	}

	public byte[] getResourceID() {
		return resourceID;
	}

	public void setResourceID(byte[] resourceID) {
		this.resourceID = resourceID;
	}

	@Override
	public String toString() {
		return super.toString() + "[ResourceID=" + ByteUtils.byteArrayToHexString(resourceID) + "]";
	}
}
