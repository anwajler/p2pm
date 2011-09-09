package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * The expires TLV object as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class Expires extends GeneralObject {

	/**
	 * Special value used to prevent from republishing of objects that doesn't have to be republished. Sent in
	 * PublishObjectResponse.
	 */
	public static final int EXPIRES_VALUE_NOT_REPUBLISH = 0;

	@Override
	public String toString() {
		return super.toString() + "[Expires=[expiresInSeconds=" + expiresSeconds + "]]";
	}

	private static final int EXPIRES_SPECIFIC_DATA_LENGTH = 4 * 8;

	private int expiresSeconds;

	/**
	 * Constructor of Expires object for a value given as an argument.
	 * 
	 * @param expiresInSeconds
	 *            Number of seconds.
	 */
	public Expires(int expiresInSeconds) {
		super(GeneralObject.EXPIRES_OBJECT_TYPE);

		this.expiresSeconds = expiresInSeconds;
	}

	@Override
	public byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int firstBitIndex = super.getBitsCount();

		ByteUtils.addIntToArrayAtBitIndex(expiresSeconds, bytes, firstBitIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + EXPIRES_SPECIFIC_DATA_LENGTH;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException("GeneralObject of Expires type can't have subobject of "
				+ subobject.getClass().getName() + " type.");
	}

	/**
	 * Returns time (seconds) after which object is considered expired.
	 * 
	 * @return
	 */
	public int getExpiresSeconds() {
		return expiresSeconds;
	}
}
