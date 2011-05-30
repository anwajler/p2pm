package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * The owner of the Resource-Object. Specification (draft 01) says "Format TBD". Currently it contains only a byte array
 * being a value of PeerID of the owner.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class Owner extends GeneralObject {

	/**
	 * PeerID value.
	 */
	private byte[] peerIDValue;

	/**
	 * Creates Owner object. Currently it contains only PeerID value (bytes array).
	 * 
	 * @param peerIDValue
	 */
	public Owner(byte[] peerIDValue) {
		super(GeneralObject.OWNER_OBJECT_TYPE);
		this.peerIDValue = peerIDValue;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();
		ByteUtils.addByteArrayToArrayAtBitIndex(peerIDValue, bytes, currentIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + peerIDValue.length * 8;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException("Owner can't contain " + subobject.getClass().getName()
				+ " as subobject.");
	}

	@Override
	public String toString() {
		return super.toString() + "[Owner=[peerIDValue=" + ByteUtils.byteArrayToHexString(peerIDValue) + "]]";
	}

	public byte[] getPeerIDValue() {
		return peerIDValue;
	}

	public void setPeerIDValue(byte[] peerIDValue) {
		this.peerIDValue = peerIDValue;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = true;
		if (obj instanceof Owner) {
			Owner otherOwner = (Owner) obj;
			byte[] otherPeerIDValue = otherOwner.getPeerIDValue();
			int length = otherPeerIDValue.length;

			// only owner objects with peerIDs of the same length are considered equal
			if (peerIDValue.length == length) {
				// only owner objects with same values of peerIDs
				for (int i = 0; i < length; i++) {
					if (peerIDValue[i] != otherPeerIDValue[i]) {
						result = false;
						break;
					}
				}
			} else {
				result = false;
			}
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode() {
		StringBuilder builder = new StringBuilder();
        for (byte peerIDByte : peerIDValue) {
			builder.append(peerIDByte);
		}
		return builder.toString().hashCode();
	}

}
