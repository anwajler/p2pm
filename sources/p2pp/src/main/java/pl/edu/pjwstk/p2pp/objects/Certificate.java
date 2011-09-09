package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * An X.509 certificate object as defined in P2PP specification (draft 01). TODO add handling isServerSigned field to
 * bytes representation.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class Certificate extends GeneralObject {

	/**
	 * If true, certificate is server singed. Otherwise it is self signed.
	 */
	private boolean serverSigned;

	/**
	 * Value of certificate.
	 */
	byte[] certificateValue;

	/**
	 * Constructor for Certificate. TODO Currently predefined value is used, so certificates can't be used.
	 * 
	 * @param isServerSigned
	 *            Type of signature that is also a content subtype of {@link GeneralObject}.
	 * @param certificateValue
	 *            Value of certificate as bytes.
	 */
	public Certificate(boolean isServerSigned, byte[] certificateValue) {
		super(GeneralObject.X509_CERTIFICATE_OBJECT_TYPE);
		this.serverSigned = isServerSigned;
		this.certificateValue = certificateValue;
	}

	@Override
	public byte[] asBytes() {
		byte[] bytes = super.asBytes(getBitsCount());

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtBitIndex(certificateValue, bytes, currentIndex);

		// TODO add isServerSigned somewhere

		return bytes;

	}

	@Override
	public int getBitsCount() {
		// TODO add isServerSigned somewhere
		return super.getBitsCount() + certificateValue.length * 8;
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		throw new UnsupportedGeneralObjectException("Certificate GeneralObject can't contain "
				+ subobject.getClass().getName() + " as subobject.");
	}

	public boolean isServerSigned() {
		return serverSigned;
	}

	public void setServerSigned(boolean serverSigned) {
		this.serverSigned = serverSigned;
	}

	@Override
	public String toString() {
		return super.toString() + "[Certificate=[value=[" + new String(certificateValue) + "]]]";
	}
}
