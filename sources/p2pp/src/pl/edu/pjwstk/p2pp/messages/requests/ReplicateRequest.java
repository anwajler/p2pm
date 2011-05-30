package pl.edu.pjwstk.p2pp.messages.requests;

import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.RequestOptions;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;

/**
 * Replicate request as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class ReplicateRequest extends Request {

	@Override
	public RequestOptions getRequestOptions() {
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
	public byte[] asBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verify() {
		// TODO Auto-generated method stub
		return false;
	}

}
