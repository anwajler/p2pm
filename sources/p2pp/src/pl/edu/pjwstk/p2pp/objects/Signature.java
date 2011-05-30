package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class Signature extends GeneralObject {

	private byte[] signatureValue;

	/**
	 * Creates signature object.
	 * 
	 * @param value
	 */
	public Signature(byte[] value) {
		super(GeneralObject.X509_CER7_SIGNATURE_OBJECT_TYPE);
		this.signatureValue = value;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {

		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtBitIndex(signatureValue, bytes, currentIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + signatureValue.length * 8;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException("Certificate GeneralObject can't contain "
				+ subobject.getClass().getName() + " as subobject.");
	}

	@Override
	public String toString() {
		return super.toString() + "Signature=[value=[" + new String(signatureValue) + "]]";
	}
}
