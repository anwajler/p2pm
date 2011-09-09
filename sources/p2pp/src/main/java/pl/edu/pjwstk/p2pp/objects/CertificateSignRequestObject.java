package pl.edu.pjwstk.p2pp.objects;

/**
 * TODO check this carefully. Currently empty
 * 
 * @author szeldon
 * 
 */
public class CertificateSignRequestObject extends GeneralObject {

	public CertificateSignRequestObject(byte type) {
		super(GeneralObject.CERTIFICATE_SIGN_REQUEST_OBJECT_TYPE);
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] asBytes() {
		// TODO Auto-generated method stub
		return null;
	}

}
