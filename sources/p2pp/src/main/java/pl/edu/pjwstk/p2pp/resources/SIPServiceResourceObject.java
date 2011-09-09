package pl.edu.pjwstk.p2pp.resources;

import pl.edu.pjwstk.p2pp.objects.Certificate;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.Signature;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * ResourceObject describing SIP service as defined in P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class SIPServiceResourceObject extends ResourceObject {

	/**
	 * Creates ResourceObject describing SIP service. To be filled later with Owner and ResourceID.
	 * 
	 * @param serviceOwnerPeerInfo
	 */
	public SIPServiceResourceObject(PeerInfo serviceOwnerPeerInfo) {
		super(P2PPUtils.SIP_CONTENT_TYPE, (byte) 0);

		// passes peerInfo byte representation to resource object value
		value.setValue(serviceOwnerPeerInfo.asBytes());
	}

	/**
	 * Creates empty ResourceObject describing SIP service. To be filled later with Owner, ResourceID and value.
	 * 
	 */
	public SIPServiceResourceObject() {
		super(P2PPUtils.SIP_CONTENT_TYPE, (byte) 0);
	}

	/**
	 * Creates ResourceObject describing SIP service.
	 * 
	 * @param serviceOwnerPeerInfo
	 * @param unhashedID
	 * @param owner
	 * @param expires
	 * @param signature
	 * @param certificate
	 */
	public SIPServiceResourceObject(PeerInfo serviceOwnerPeerInfo, byte[] unhashedID, Owner owner, Expires expires,
			Signature signature, Certificate certificate) {
		super(P2PPUtils.SIP_CONTENT_TYPE, (byte) 0, unhashedID, owner, expires, signature, certificate);

		// passes peerInfo byte representation to resource object value
		value.setValue(serviceOwnerPeerInfo.asBytes());
	}

	@Override
	public String getValueAsString() {
		// TODO Auto-generated method stub
		return null;
	}

}
