package pl.edu.pjwstk.p2pp.messages.responses;

import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;

public class AuthenticateResponse extends Response {

	@Override
	public byte[] asBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		// TODO Auto-generated method stub

	}

	@Override
	public PeerInfo getPeerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verify() {
		// TODO Auto-generated method stub
		return false;
	}

}
