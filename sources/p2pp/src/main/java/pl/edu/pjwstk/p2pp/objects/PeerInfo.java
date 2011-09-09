package pl.edu.pjwstk.p2pp.objects;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * Peer-Info object as defined in Peer-to-Peer specification (draft 01). May contain multiple addresses.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public class PeerInfo extends GeneralObject implements Comparable<PeerInfo> {

	/**
	 * PeerID object being part of this PeerInfo.
	 */
	private PeerID peerID;

	/**
	 * Uptime object being part of this PeerInfo.
	 */
	private Uptime uptime;

	/**
	 * Vector of resource types stored as Bytes (one Byte, one type). They're stored this way so that it is possible to
	 * add them to Vector. ObjectElements of this array are the same as in Cont-Type or in Resource-Object. Typically,
	 * only included for the services such as STUN or TURN that the node is currently providing.
	 */
	private Vector<Byte> resourceTypes = new Vector<Byte>();

	/**
	 * AddressInfo objects being part of this PeerInfo.
	 */
	private Vector<AddressInfo> addressInfos = new Vector<AddressInfo>();

	/**
	 * UnhashedID.
	 */
	private UnhashedID unhashedID;

	/**
	 * Empty constuctor.
	 */
	public PeerInfo() {
		super(GeneralObject.PEER_INFO_OBJECT_TYPE);
	}

	/**
	 * Constructor for Peer-Info object.
	 *
	 * @param peerID
	 *            Peer ID.
	 * @param uptime
	 *            Uptime.
	 * @param addressInfo
	 *            AddressInfo object containing info about this objects address.
	 * @param unhashedID
	 *
	 */
	public PeerInfo(PeerID peerID, Uptime uptime, AddressInfo addressInfo, UnhashedID unhashedID) {
		super(GeneralObject.PEER_INFO_OBJECT_TYPE);

		this.peerID = peerID;
		this.uptime = uptime;
		this.unhashedID = unhashedID;
		addressInfos.add(addressInfo);
	}

	/**
	 * Constructor for Peer-Info object.
	 *
	 * @param peerID
	 *            Peer ID.
	 * @param uptime
	 *            Uptime.
	 * @param addressInfos
	 *            Vector of AddressInfo objects containing info about this objects address.
	 * @param unhashedID
	 *
	 */
	public PeerInfo(PeerID peerID, Uptime uptime, Vector<AddressInfo> addressInfos, UnhashedID unhashedID) {
		super(GeneralObject.PEER_INFO_OBJECT_TYPE);

		this.peerID = peerID;
		this.uptime = uptime;
		this.unhashedID = unhashedID;
		this.addressInfos = addressInfos;
	}

	/**
	 * TODO Probably this has to be accomplished by using Resource-List object, but it isn't decided yet. Adds
	 * resource-type to this object.
	 *
	 * @param newResourceType
	 */
	public void addResourceType(byte newResourceType) {
		Byte typeAsByte = new Byte(newResourceType);
		resourceTypes.add(typeAsByte);
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int byteIndex = super.getBitsCount() / 8;

		if (peerID != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(peerID.asBytes(), bytes, byteIndex);
			byteIndex += peerID.getBitsCount() / 8;
		}

		// 0.2 reference implementation doesn't use it
		if (uptime != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(uptime.asBytes(), bytes, byteIndex);
			byteIndex += uptime.getBitsCount() / 8;
		}

		int resourcesCount = resourceTypes.size();
		for (int i = 0; i < resourcesCount; i++) {
			byte currentResourceType = resourceTypes.get(i).byteValue();
			ByteUtils.addByteToArrayAtBitIndex(currentResourceType, bytes, byteIndex * 8);
			byteIndex += 1;
		}

		int size = addressInfos.size();
		for (int i = 0; i < size; i++) {
			AddressInfo currentAddressInfo = addressInfos.get(i);
			ByteUtils.addByteArrayToArrayAtByteIndex(currentAddressInfo.asBytes(), bytes, byteIndex);
			byteIndex += currentAddressInfo.getBitsCount() / 8;
		}

		if (unhashedID != null) {
			ByteUtils.addByteArrayToArrayAtByteIndex(unhashedID.asBytes(), bytes, byteIndex);
			byteIndex += unhashedID.getBitsCount() / 8;
		}

		return bytes;
	}

	@Override
	public int getBitsCount() {
		int additionalLength = 0;
		int size = addressInfos.size();
		for (int i = 0; i < size; i++) {
			AddressInfo currentAddressInfo = addressInfos.get(i);
			additionalLength += currentAddressInfo.getBitsCount();
		}
		if (unhashedID != null) {
			additionalLength += unhashedID.getBitsCount();
		}
		if (uptime != null) {
			additionalLength += uptime.getBitsCount();
		}
		if (peerID != null) {
			additionalLength += peerID.getBitsCount();
		}
		// TODO when resource-type situation is decided, this probably will
		// change
		return super.getBitsCount() + resourceTypes.size() * 8 + additionalLength;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		// TODO probably resource-list object should be handled if it is a GeneralObject.
		if (subobject instanceof PeerID) {
			peerID = (PeerID) subobject;
		} else if (subobject instanceof Uptime) {
			uptime = (Uptime) subobject;
		} else if (subobject instanceof AddressInfo) {
			addressInfos.add((AddressInfo) subobject);
		} else if (subobject instanceof UnhashedID) {
			unhashedID = (UnhashedID) subobject;
		} else {
			Class subobjectClass = subobject.getClass();
			throw new UnsupportedGeneralObjectException("Current implementation can't handle "
					+ subobjectClass.getName() + " as a subobject of PeerInfo object.");
		}
	}

	/**
	 * Returns PeerID object included in this PeerInfo.
	 *
	 * @return
	 */
	public PeerID getPeerID() {
		return peerID;
	}

	public Uptime getUptime() {
		return uptime;
	}

	/**
	 * Returns a vector consisting of AddressInfo objects that describe addresses. May be zero size.
	 *
	 * @return
	 */
	public Vector<AddressInfo> getAddressInfos() {
		return addressInfos;
	}

	public UnhashedID getUnhashedID() {
		return unhashedID;
	}

	/**
	 * Sets newPeerID for this object.
	 *
	 * @param newPeerID
	 */
	public void setPeerID(PeerID newPeerID) {
		peerID = newPeerID;
	}

	/**
	 * Sets UnhashedID object in this peerInfo.
	 *
	 * @param newUnhashedID
	 */
	public void setUnhashedID(UnhashedID newUnhashedID) {
		unhashedID = newUnhashedID;
	}

	/**
	 * Adds address info object to this object.
	 *
	 * @param newAddressInfo
	 */
	public void addAddressInfo(AddressInfo newAddressInfo) {
		if (addressInfos == null) {
			addressInfos = new Vector<AddressInfo>();
		}
		addressInfos.add(newAddressInfo);
	}

	/**
	 * TODO Two PeerInfo objects are considered equal if they're of the same type and if they're
	 * peerIDs are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PeerInfo) {
			PeerInfo otherPeerInfo = (PeerInfo) obj;
            return peerID.equals(otherPeerInfo.getPeerID());
		}
        return false;
	}

    @Override
    public int hashCode() {
        return this.peerID.hashCode(); // TODO Take the rest of the fields into account
    }

    public int compareTo(PeerInfo peerInfo) {

        if (this.equals(peerInfo)) {
            return 0;
        }

        return 1;
    }

	/**
	 * Removes all AddressInfo objects from this object.
	 */
	public void resetAddressInfos() {
		addressInfos.removeAllElements();
	}

	@Override
	public String toString() {
		String resourceTypesString = "";
		for (int i = 0; i < resourceTypes.size(); i++) {
			resourceTypesString += resourceTypes.get(i);
			if (i != resourceTypes.size() - 1) {
				resourceTypesString += ", ";
			}
		}
		StringBuilder addressesBuilder = new StringBuilder();
		for (int i = 0; i < addressInfos.size(); i++) {
			addressesBuilder.append(addressInfos.get(i).toString());
			if (i != addressInfos.size() - 1) {
				addressesBuilder.append(", ");
			}
		}
		String peerIDAsString = null;
		if (peerID != null) {
			peerIDAsString = peerID.toString();
		}
		String uptimeAsString = null;
		if (uptime != null) {
			uptimeAsString = uptime.toString();
		}
		String unhashedAsString = null;
		if (unhashedID != null) {
			unhashedAsString = unhashedID.toString();
		}

        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("PeerInfo=[peerID=[");
        sb.append(peerIDAsString);
        sb.append("], uptime=[");
        sb.append(uptimeAsString);
        sb.append("], resourceTypes=[");
        sb.append(resourceTypesString);
        sb.append("], addresses=[");
        sb.append(addressesBuilder.toString());
        sb.append("], unhashedID=[");
        sb.append(unhashedAsString);
        sb.append("]]");

		return sb.toString();
	}

}
