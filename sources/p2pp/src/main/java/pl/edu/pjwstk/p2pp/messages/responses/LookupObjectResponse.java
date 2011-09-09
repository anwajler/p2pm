package pl.edu.pjwstk.p2pp.messages.responses;

import java.util.List;
import java.util.Vector;

import pl.edu.pjwstk.p2pp.objects.AddressInfo;
import pl.edu.pjwstk.p2pp.objects.GeneralObject;
import pl.edu.pjwstk.p2pp.objects.PeerID;
import pl.edu.pjwstk.p2pp.objects.PeerInfo;
import pl.edu.pjwstk.p2pp.objects.ResourceObject;
import pl.edu.pjwstk.p2pp.objects.UnhashedID;
import pl.edu.pjwstk.p2pp.objects.UnsupportedGeneralObjectException;
import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Lookup object response as defined by P2PP specification (draft 01).
 * 
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 * 
 */
public class LookupObjectResponse extends Response {

	/**
	 * PeerInfo of response originator.
	 */
	private PeerInfo peerInfo;

	/**
	 * Vector of resource objects.
	 */
	private Vector<ResourceObject> resourceObjects = new Vector<ResourceObject>();

	/**
	 * Constructor creating empty LookupObject response. Has to be filled later. To be used in process of creating
	 * objects basing on data received on socket.
	 */
	public LookupObjectResponse() {

	}

	/**
	 * Constructor for LookupObjectResponse object.
	 * 
	 * @param protocolVersion
	 * @param messageType
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param responseCode
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param responseID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param ownPeerInfo
	 * @param resourceObject
	 *            May be null but has to be filled later.
	 */
	public LookupObjectResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
			byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted, PeerInfo ownPeerInfo,
			ResourceObject resourceObject) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				LOOKUP_OBJECT_MESSAGE_TYPE, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);
		this.peerInfo = ownPeerInfo;

		if (resourceObject != null) {
			resourceObjects.add(resourceObject);
		}
	}

	/**
	 * Constructor for LookupObjectResponse object.
	 * 
	 * @param protocolVersion
	 * @param messageType
	 * @param isAcknowledgment
	 * @param isSentByPeer
	 * @param isRecursive
	 * @param responseCode
	 * @param ttl
	 * @param transactionID
	 * @param sourceID
	 * @param responseID
	 * @param isOverReliable
	 * @param isEncrypted
	 * @param ownPeerInfo
	 * @param resourceObjectsList
	 *            May be null but has to be filled later. May contain null elements (they won't be added to this
	 *            message).
	 */
	public LookupObjectResponse(boolean[] protocolVersion, boolean[] messageType, boolean isAcknowledgment,
			boolean isSentByPeer, boolean isRecursive, boolean[] responseCode, byte ttl, byte[] transactionID,
			byte[] sourceID, byte[] responseID, boolean isOverReliable, boolean isEncrypted, PeerInfo ownPeerInfo,
			List<ResourceObject> resourceObjectsList) {
		super(protocolVersion, messageType, isAcknowledgment, isSentByPeer, isRecursive, responseCode,
				LOOKUP_OBJECT_MESSAGE_TYPE, ttl, transactionID, sourceID, responseID, isOverReliable, isEncrypted);
		this.peerInfo = ownPeerInfo;

		if (resourceObjectsList != null) {
			// given array may contain nulls, so only non-null elements are added
			for (ResourceObject currentResource : resourceObjectsList) {
				if (currentResource != null) {
					resourceObjects.add(currentResource);
				}
			}
		}
	}

	@Override
	public void addObject(GeneralObject object) throws UnsupportedGeneralObjectException {
		if (object instanceof PeerInfo) {
			peerInfo = (PeerInfo) object;
		} else if (object instanceof ResourceObject) {
			resourceObjects.add((ResourceObject) object);
		} else {
			throw new UnsupportedGeneralObjectException("LookupObjectResponse " + "can't contain "
					+ object.getClass().getName() + " object.");
		}
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addByteArrayToArrayAtByteIndex(peerInfo.asBytes(), bytes, currentIndex / 8);
		currentIndex += peerInfo.getBitsCount();

		// resource may be null because lookup response may be with code NOT FOUND
		for (ResourceObject currentResource : this.resourceObjects) {
			if (currentResource != null) {
				ByteUtils.addByteArrayToArrayAtByteIndex(currentResource.asBytes(), bytes, currentIndex / 8);
				currentIndex += currentResource.getBitsCount();
			}
		}

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int additionalBits = 0;
		if (resourceObjects != null) {
			for (ResourceObject currentResource : this.resourceObjects) {
				additionalBits += currentResource.getBitsCount();
			}

		}
		return super.getBitsCount() + peerInfo.getBitsCount() + additionalBits;
	}

	/**
	 * Returns PeerInfo object of response originator.
	 * 
	 * @return
	 */
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	/**
	 * Returns vector of ResourceObjects included in this response.
	 * 
	 * @return
	 */
	public Vector<ResourceObject> getResourceObject() {
		return resourceObjects;
	}

	@Override
	public boolean verify() {
		boolean result = true;

		if (peerInfo == null || resourceObjects == null) {

			result = false;
		} else {
			// must contain peerID
			PeerID peerID = peerInfo.getPeerID();
			if (peerID == null) {
				result = false;
			} else {
				if (peerID.getPeerIDBytes() == null) {
					result = false;
				}
			}
			// not checked if peerID isn't here
			if (result) {
				// must contain unshadedID
				UnhashedID unhashedID = peerInfo.getUnhashedID();
				if (unhashedID == null) {
					result = false;
				} else {
					if (unhashedID.getUnhashedIDValue() == null) {
						result = false;
					}
				}
			}
			// not checked if PeerID or UnhashedID isn't here
			if (result) {
				// has to contain at least one AddressInfo
				Vector<AddressInfo> addressInfos = peerInfo.getAddressInfos();
				if (addressInfos == null) {
					result = false;
				} else if (addressInfos.size() <= 0) {
					result = false;
				}
			}

			if (result) {
				// resource objects are verified only if response is with OK code
				if (ByteUtils.booleanArrayToInt(reservedOrResponseCode) == Response.RESPONSE_CODE_OK) {
					if (resourceObjects.size() == 0) {
						result = false;
					} else {
                        for (ResourceObject currentResource : this.resourceObjects) {
							// TODO what about certificate and signature?
							if (currentResource.getOwner() == null || currentResource.getValue() == null) {

								result = false;
							}
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("LookupObjectResponse=[message=[" + super.toString() + "header=["
				+ getHeader() + "], peerInfo=[");
		if (peerInfo != null) {
			builder.append(peerInfo.toString());
		} else {
			builder.append(peerInfo);
		}

		builder.append("], resourceObjects=[");
		for (int i = 0; i < resourceObjects.size(); i++) {
			ResourceObject currentResource = resourceObjects.get(i);
			if (currentResource != null) {
				builder.append(currentResource.toString());
				if (i != resourceObjects.size() - 1) {
					builder.append(", ");
				}
			}
		}
		builder.append("]]");

		return builder.toString();
	}
}
