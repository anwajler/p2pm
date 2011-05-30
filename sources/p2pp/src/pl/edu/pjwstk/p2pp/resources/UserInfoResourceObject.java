package pl.edu.pjwstk.p2pp.resources;

import java.util.Vector;

import org.apache.log4j.Logger;
import pl.edu.pjwstk.p2pp.messages.MalformedP2PPMessageException;
import pl.edu.pjwstk.p2pp.messages.requests.PublishObjectRequest;
import pl.edu.pjwstk.p2pp.objects.*;
import pl.edu.pjwstk.p2pp.util.ByteUtils;
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory;
import pl.edu.pjwstk.p2pp.util.P2PPUtils;

/**
 * Describes UserInfo ResourceObject as defined in P2PP specification (draft 01). Contains certificate of publisher of
 * this resource and a vector of this publisher addresses (both as bytes in ResourceObjectValue of resource).
 * 
 * @author Maciej Skorupka s3874@pwjstk.edu.pl
 * 
 */
public class UserInfoResourceObject extends ResourceObject {

    private final static Logger LOG = Logger.getLogger(UserInfoResourceObject.class);

	/**
	 * Creates empty UserInfo resource object.
	 */
	public UserInfoResourceObject() {
		super(P2PPUtils.USER_INFO_CONTENT_TYPE, P2PPUtils.USER_INFO_CONTENT_SUBTYPE);
	}

	/**
	 * 
	 * @param certificate
	 *            Certificate of publisher. Can't be null.
	 * @param addresses
	 *            Addresses of a node (host, server-reflexive, and relay addresses).
	 */
	public UserInfoResourceObject(Certificate certificate, Vector<AddressInfo> addresses) {
		super(P2PPUtils.USER_INFO_CONTENT_TYPE, P2PPUtils.USER_INFO_CONTENT_SUBTYPE);

		byte[] certificateBytes = certificate.asBytes();
		// counts total length of address info objects
		int addressesLength = 0;
		for (AddressInfo addressInfo : addresses) {
			addressesLength += addressInfo.getBitsCount() / 8;
		}

		// creates byte array that will hold value of this object
		byte[] valueBytes = new byte[certificateBytes.length + addressesLength];

		// fills value array with certificate and AddressInfos
		ByteUtils.addByteArrayToArrayAtBitIndex(certificateBytes, valueBytes, 0);
		int index = certificateBytes.length;
		for (AddressInfo addressInfo : addresses) {
			ByteUtils.addByteArrayToArrayAtBitIndex(addressInfo.asBytes(), valueBytes, index);
			index += addressInfo.getBitsCount() / 8;
		}

		// stores byte representation of certificate and addresses in ResourceObjectValue
		value.setValue(valueBytes);

	}

	/**
	 * Creates UserInfo resource object. UnhashedID value will be created basing on Owner object.
	 * 
	 * @param owner
	 *            Owner object holding an unhashedID that will be used for generating resourceID. If structured network
	 *            is used, resourceID will be hashed basing on this unhashedID. If unstructured network is used,
	 *            resourceID will have the same value as unhashedID.
	 * @param expires
	 *            Can't be null.
	 * @param signature
	 *            TODO can be null?
	 * @param certificate
	 *            TODO can be null?
	 * @param addresses
	 *            List of addresses of publisher.
	 */
	public UserInfoResourceObject(Owner owner, Expires expires, Signature signature, Certificate certificate,
			Vector<AddressInfo> addresses) {
		super(P2PPUtils.USER_INFO_CONTENT_TYPE, P2PPUtils.USER_INFO_CONTENT_SUBTYPE, owner.getPeerIDValue(), owner,
				expires, signature, certificate);

		unhashedID = owner.getPeerIDValue();

		// counts total length of address info objects
		int addressesLength = 0;
		for (AddressInfo addressInfo : addresses) {
			addressesLength += addressInfo.getBitsCount() / 8;
		}

		// creates byte array that will hold value of this object
		byte[] valueBytes = new byte[addressesLength];

		// fills value array AddressInfos
		int index = 0;
		for (AddressInfo addressInfo : addresses) {
			ByteUtils.addByteArrayToArrayAtBitIndex(addressInfo.asBytes(), valueBytes, index);
			index += addressInfo.getBitsCount() / 8;
		}

		value.setValue(valueBytes);
	}

    public GeneralObject getValueAsgeneralObject() {
		byte[] valueBytes = value.getValue();

		PeerInfo serviceOwner;

		// TODO at the moment parsing value is made by using a P2PPMessage that will be filled with object
		PublishObjectRequest message = new PublishObjectRequest();
		P2PPMessageFactory factory = new P2PPMessageFactory();
		try {
			factory.parseGeneralObject(valueBytes, 0, valueBytes.length, null, message);
		} catch (UnsupportedGeneralObjectException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		} catch (MalformedP2PPMessageException e) {
			// TODO Auto-generated catch block
			LOG.error(e);
		}
		serviceOwner = message.getPeerInfo();

		return serviceOwner;
	}

	@Override
	public String getValueAsString() {
		return getValueAsgeneralObject().toString();
	}
}
