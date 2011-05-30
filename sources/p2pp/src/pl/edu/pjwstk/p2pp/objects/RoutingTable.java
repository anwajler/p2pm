package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * <p>
 * General object holding information about routing. Can be created any time, but can't be used before invocation of
 * {@link #setProperties(int, int)}. Contains information about peers in an overlay.
 * </p>
 * <p>
 * Contains peerID (as bytes) of local peer (set in {@link #setLocalPeerID(byte[])} method). Thanks to this, routing
 * table can determine if given PeerInfo may be added using {@link #addPeerInfo(PeerInfo)} method (only non-local peers
 * may be added).
 * </p>
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 *
 */
public abstract class RoutingTable extends GeneralObject {

	/**
	 * PeerInfo of a node that this routing table is part of.
	 */
    protected PeerInfo localPeerInfo;

	protected byte[] localPeerIDBytes;

	/**
	 * Algorithm base used by an overlay.
	 */
	protected int algorithmBase;

	/**
	 * Length of a key being used by an overlay. Bits.
	 */
	protected int keyLength;

	/**
	 * Constructor of routing table.
	 */
	public RoutingTable() {
		super(GeneralObject.ROUTING_TABLE_OBJECT_TYPE);
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		int numberOfEntries = getNumOfEntries();
		ByteUtils.addByteToArrayAtBitIndex((byte) numberOfEntries, bytes, currentIndex);
		currentIndex += 8;

		return bytes;
	}

	@Override
	public int getBitsCount() {
		// 8 bits for "num of entries" field.
		return super.getBitsCount() + 8;
	}

	/**
	 * Returns number of entries in this table. Protocol-dependent subclasses have to implement this method so that an
	 * abstract {@link #RoutingTable()} class will know the number of entries and will add this to byte representation
	 * of this object.
	 *
	 * @return Number of entries in routing table.
	 */
	protected abstract int getNumOfEntries();

	/**
	 * Returns PeerInfo object describing a node that is a next hop for given nodeID. The exact process is implemented
	 * in subclasses. Won't return local node's PeerInfo. Returned PeerInfo is closer to local peer's ID.
	 *
	 * @param id
	 *            ID of a node or resource for which a next hop is searched.
	 * @return PeerInfo object describing a next hop. Null if there's no proper PeerInfo object in routing table (i.e.
	 *         there's no PeerInfo object other than local one and given one. Moreover, there's no peer closer to given
	 *         one. Therefore, null informs that local peer is best known peer for given ID.
	 */
	public abstract PeerInfo getNextHop(byte[] id);

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

    public PeerInfo getLocalPeerInfo() {
        return this.localPeerInfo;
    }

    public void setLocalPeerInfo(PeerInfo peerInfo) {
        this.localPeerInfo = peerInfo;
    }

	/**
	 * Returns peerID of an owner of routing table.
	 *
	 * @return
	 */
	public byte[] getPeerID() {
		return localPeerIDBytes;
	}

	/**
	 * Sets peerID of a node that this routing table is used in. Has to be set after bootstrapping.
	 *
	 * @param localPeerID
	 */
	public void setLocalPeerID(byte[] localPeerID) {
		this.localPeerIDBytes = localPeerID;
	}

	/**
	 * Sets properties of routing table. If subclass wants to do something when routing table becomes aware of those
	 * properties, it has to override this method (and use super()).
	 *
	 * @param algorithmBase
	 * @param keyLenght
	 *            Key length. (for instance for Kademlia it will be 160, not 20).
	 */
	public void setProperties(int algorithmBase, int keyLenght) {
		this.algorithmBase = algorithmBase;
		this.keyLength = keyLenght;
	}

	/**
	 * Returns PeerInfo object describing peer that is closest known to given id. It might be farther than local peer,
	 * but is closest of peers known to local peer. Returns null if there isn't any PeerInfo object in this table.
	 *
	 * @param id
	 */
	public abstract PeerInfo getClosestTo(byte[] id);

	/**
	 * Gives time slot for routing table to do its stuff.
	 */
	public abstract void onTimeSlot();

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		// TODO shouldn't there be a super()?

	}

	/**
	 * Creates NeighborTable for given peerID. Returned table will contain neigbors determined basing on entries in this
	 * routing table. Not all the protocols must use this method (null is returned then), but null may be returned also
	 * when there are no neighbors for given id.
	 *
	 * @param peerID
	 * @return
	 */
	public abstract NeighborTable createNeighborTableForID(byte[] peerID);

	/**
	 * Returns an array of Strings. Each of them contains a description of one entry in this table. Returns null if
	 * there's no entries in this table.
	 *
	 * @return
	 */
	public abstract String[] getEntriesDescription();

	/**
	 * Adds given PeerInfo object to this routing table.
	 *
	 * @param peerInfo
	 */
	public abstract void addPeerInfo(PeerInfo peerInfo);

    public abstract void removePeerInfo(PeerInfo peerInfo);

	/**
	 * Method that returns true if local peer (initiated using {@link #setLocalPeerID(byte[])} method) is closer to
	 * given id than remoteId is.
	 *
	 * @param id
	 * @param remoteId
	 * @return
	 */
	public abstract boolean isLocalPeerCloser(byte[] id, byte[] remoteId);

	/**
	 * Resets this routing table.
	 */
	public abstract void leaveReset();
}
