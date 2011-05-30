package pl.edu.pjwstk.p2pp.resources;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.MalformedP2PPMessageException;
import pl.edu.pjwstk.p2pp.messages.responses.BootstrapResponse;
import pl.edu.pjwstk.p2pp.objects.Certificate;
import pl.edu.pjwstk.p2pp.objects.Expires;
import pl.edu.pjwstk.p2pp.objects.Owner;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.Signature;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * ResourceObject describing STUN service as defined by P2PP specification (draft 01). It interprets ResourceObjectValue
 * as PeerInfo object.
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class STUNServiceResourceObject extends ResourceObject {

    private final static Logger LOG = Logger.getLogger(STUNServiceResourceObject.class);

	/**
	 * Creates ResourceObject describing STUN service. Has to be filled later with ResourceID and Owner.
	 * 
	 * @param serviceOwnerPeerInfo
	 */
	public STUNServiceResourceObject(PeerInfo serviceOwnerPeerInfo) {
		super(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0);

		unhashedID = P2PPUtils.STUN_SERVICE_ID;

		value.setValue(serviceOwnerPeerInfo.asBytes());
	}

	/**
	 * Creates empty ResourceObject describing STUN service. Has to be filled later with ResourceID, Owner and value.
	 * 
	 */
	public STUNServiceResourceObject() {
		super(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0);
		unhashedID = P2PPUtils.STUN_SERVICE_ID;
	}

	/**
	 * Creates ResourceObject describing STUN service. Unhashed ID is a constant P2PPUtils.STUN_SERVICE_ID.
	 * 
	 * @param serviceOwnerPeerInfo
	 * @param owner
	 * @param expires
	 * @param signature
	 * @param certificate
	 */
	public STUNServiceResourceObject(PeerInfo serviceOwnerPeerInfo, Owner owner, Expires expires, Signature signature,
			Certificate certificate) {
		super(P2PPUtils.STUN_CONTENT_TYPE, (byte) 0, P2PPUtils.STUN_SERVICE_ID, owner, expires, signature, certificate);
		unhashedID = P2PPUtils.STUN_SERVICE_ID;
		value.setValue(serviceOwnerPeerInfo.asBytes());
	}

	/**
	 * Returns PeerInfo object being a value of this ResourceObject.
	 * 
	 * @return
	 */
	public PeerInfo getServiceOwnerPeerInfo() {
		byte[] valueBytes = value.getValue();

		PeerInfo serviceOwner;

		// TODO at the moment parsing value is made by using a P2PPMessage that will be filled with object
		BootstrapResponse response = new BootstrapResponse();
		P2PPMessageFactory factory = new P2PPMessageFactory();
		try {
			factory.parseGeneralObject(valueBytes, 0, valueBytes.length, null, response);
		} catch (UnsupportedGeneralObjectException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		} catch (MalformedP2PPMessageException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		}
		serviceOwner = response.getPeerInfo();

		return serviceOwner;
	}

	@Override
	public String getValueAsString() {
		return getServiceOwnerPeerInfo().toString();
	}
}
