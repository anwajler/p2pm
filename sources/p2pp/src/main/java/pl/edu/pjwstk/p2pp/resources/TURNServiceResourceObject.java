package pl.edu.pjwstk.p2pp.resources;

import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * ResourceObject describing TURN service as defined by P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class TURNServiceResourceObject extends ResourceObject {

	/**
	 * Creates empty TURN service resource object that describes TURN service. Unhashed ID is a constant
	 * P2PPUtils.TURN_SERVICE_ID.
	 * 
	 * @param serviceOwnerPeerInfo
	 */
	public TURNServiceResourceObject(PeerInfo serviceOwnerPeerInfo) {
		super(P2PPUtils.TURN_CONTENT_TYPE, (byte) 0, P2PPUtils.TURN_SERVICE_ID, null, null, null, null);

		unhashedID = P2PPUtils.TURN_SERVICE_ID;

		value.setValue(serviceOwnerPeerInfo.asBytes());
	}

	@Override
	public String getValueAsString() {
		// TODO Auto-generated method stub
		return null;
	}
}
