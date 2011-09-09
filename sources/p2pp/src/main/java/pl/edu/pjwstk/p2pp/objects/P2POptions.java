package pl.edu.pjwstk.p2pp.objects;

import java.security.NoSuchAlgorithmException;

import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

public class P2POptions extends GeneralObject {

	@Override
	public String toString() {
        // TODO StringBuilder
        StringBuilder strb = new StringBuilder("[P2POptions=[hashAlgorithm=");
        strb.append(this.hashAlgorithm).append(", hashAlgorithmLength=").append(this.hashAlgorithmLength).append(", p2pAlgorithm=");
        strb.append(this.p2pAlgorithm).append(", base=").append(this.base).append(", overlayIDLength=").append(this.overlayIDLength);
        strb.append(", overlayID=").append(new String(this.overlayID)).append("]]");
		return strb.toString();
	}

	private static final int P2P_OPTIONS_STATIC_SPECIFIC_DATA_LENGTH = 40;

	/**
	 * An IANA-assigned identifier for the hash algorithm.
	 */
	private byte hashAlgorithm;

	/**
	 * The byte length of the hash algorithm. If set to zero, then no hash algorithm is used.
	 */
	private byte hashAlgorithmLength;

	/**
	 * An IANA-assigned identifier for the P2P algorithm being used.
	 */
	private byte p2pAlgorithm;

	/**
	 * The base for hash algorithms. It is set to zero for unstructured overlays.
	 */
	private byte base;

	/**
	 * The byte length of overlay-ID.
	 */
	private byte overlayIDLength;

	/**
	 * Overlay-ID.
	 */
	private byte[] overlayID;

	/**
	 * Constructor for P2POptions object. Uses constants from P2PPConstants class.
	 * 
	 * @param hashAlgorithm
	 *            An IANA-assigned identifier for the hash algorithm.
	 * @param hashAlgorithmLength
	 *            The byte length of the hash algorithm. If set to zero, then no hash algorithm is used.
	 * @param p2pAlgorithm
	 *            An IANA-assigned identifier for the P2P algorithm being used.
	 * @param base
	 * @param overlayID
	 *            Overlay-ID.
	 */
	public P2POptions(byte hashAlgorithm, byte hashAlgorithmLength, byte p2pAlgorithm, byte base, byte[] overlayID) {
		super(GeneralObject.P2P_OPTIONS_OBJECT_TYPE);

		this.hashAlgorithm = hashAlgorithm;
		this.hashAlgorithmLength = hashAlgorithmLength;
		this.p2pAlgorithm = p2pAlgorithm;
		this.base = base;
		this.overlayIDLength = (byte) overlayID.length;
		this.overlayID = overlayID;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {

		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteToArrayAtBitIndex(hashAlgorithm, bytes, currentIndex);
		currentIndex += 8;
		ByteUtils.addByteToArrayAtBitIndex(hashAlgorithmLength, bytes, currentIndex);
		currentIndex += 8;
		ByteUtils.addByteToArrayAtBitIndex(p2pAlgorithm, bytes, currentIndex);
		currentIndex += 8;
		ByteUtils.addByteToArrayAtBitIndex(base, bytes, currentIndex);
		currentIndex += 8;
		ByteUtils.addByteToArrayAtBitIndex(overlayIDLength, bytes, currentIndex);
		currentIndex += 8;
		ByteUtils.addByteArrayToArrayAtBitIndex(overlayID, bytes, currentIndex);
		// currentIndex += overlayID.length * 8;

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int additionalLength = 0;
		if (overlayID != null)
			additionalLength += overlayID.length * 8;
		return super.getBitsCount() + P2P_OPTIONS_STATIC_SPECIFIC_DATA_LENGTH + additionalLength;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns hash algorithm used by this implementation. Returned value is constant from P2PPConstants class.
	 * 
	 * @return
	 */
	public byte getHashAlgorithm() {
		return hashAlgorithm;
	}

	/**
	 * Returns the byte length of the hash algorithm. If set to zero, then no hash algorithm is used.
	 * 
	 * @return
	 */
	public byte getHashAlgorithmLength() {
		return hashAlgorithmLength;
	}

	/**
	 * Returns name of hash algorithm. Null is returned when there's no hash supported hash algorithm in this object.
	 * 
	 * @return
	 */
	public String getHashAlgorithmName() {
		try {
			return P2PPUtils.convertHashAlgorithmID(hashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	/**
	 * Returns P2P algorithm used by this implementation. Returned value is constant from P2PPConstants class.
	 * 
	 * @return
	 */
	public byte getP2PAlgorithm() {
		return p2pAlgorithm;
	}

	public byte getBase() {
		return base;
	}

	public byte getOverlayIDLength() {
		return overlayIDLength;
	}

	public byte[] getOverlayID() {
		return overlayID;
	}

}
