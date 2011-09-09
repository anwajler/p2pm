package pl.edu.pjwstk.p2pp.resources;

import pl.edu.pjwstk.p2pp.objects.Certificate;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.Signature;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * ResourceObject describing ICE service as defined by P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class ICEServiceResourceObject extends ResourceObject {

	/**
	 * Creates empty ResourceObject describing ICE service.
	 * 
	 */
	public ICEServiceResourceObject() {
		super(P2PPUtils.STUN_TURN_ICE_CONTENT_TYPE, (byte) 0);
		unhashedID = P2PPUtils.ICE_SERVICE_ID;
	}

	/**
	 * Creates ResourceObject describing ICE service.
	 * 
	 * @param serviceOwnerPeerInfo
	 */
	public ICEServiceResourceObject(PeerInfo serviceOwnerPeerInfo) {
		super(P2PPUtils.STUN_TURN_ICE_CONTENT_TYPE, (byte) 0);

		unhashedID = P2PPUtils.ICE_SERVICE_ID;

		value.setValue(serviceOwnerPeerInfo.asBytes());
	}

	/**
	 * Creates ResourceObject describing ICE service.
	 * 
	 * @param serviceOwnerPeerInfo
	 * @param unhashedID
	 *            ID that will be used for generating resourceID. If structured network is used, resourceID will be
	 *            hashed basing on this unhashedID. If unstructured network is used, resourceID will have the same value
	 *            as unhashedID.
	 * @param owner
	 * @param expires
	 * @param signature
	 * @param certificate
	 */
	public ICEServiceResourceObject(PeerInfo serviceOwnerPeerInfo, byte[] unhashedID, Owner owner, Expires expires,
			Signature signature, Certificate certificate) {
		super(P2PPUtils.STUN_TURN_ICE_CONTENT_TYPE, (byte) 0, unhashedID, owner, expires, signature, certificate);
		//unhashedID = P2PPUtils.ICE_SERVICE_ID;
		value.setValue(serviceOwnerPeerInfo.asBytes());
	}

	@Override
	public String getValueAsString() {
		// TODO Auto-generated method stub
		return null;
	}
}
