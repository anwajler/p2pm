package pl.edu.pjwstk.p2pp.objects;

import java.util.Vector;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

/**
 * General object containing information about neighbors of node.
 *
 * @author Maciej Skorupka s3874@pjwstk.edu.pl
 */
public abstract class NeighborTable extends GeneralObject {

    /**
     * Returns vector of PeerInfo objects describing neighbors.
     *
     * @return
     */
    public Vector<PeerInfo> getNeighbors() {
        return neighbors;
    }

	public void setNeighbors(Vector<PeerInfo> neighbors) {
		this.neighbors = neighbors;
	}

    /**
     * Vector of PeerInfo objects describing neighbors.
     */
    protected Vector<PeerInfo> neighbors = new Vector<PeerInfo>();

    /**
     * Constructor for NeighborTable object.
     */
    public NeighborTable() {
        super(GeneralObject.NEIGHBOR_TABLE_OBJECT_TYPE);
    }

    @Override
    public byte[] asBytes() {
        return asBytes(getBitsCount());
    }

    @Override
    protected byte[] asBytes(int bitsCount) {
        byte[] bytes = super.asBytes(bitsCount);

        int currentIndex = super.getBitsCount();

        int peerInfosSize = getNumOfEntries();

        ByteUtils.addByteToArrayAtBitIndex((byte) peerInfosSize, bytes, currentIndex);
        currentIndex += 8;

        for (PeerInfo currentNeighbor : this.neighbors) {
            ByteUtils.addByteArrayToArrayAtBitIndex(currentNeighbor.asBytes(), bytes, currentIndex);
            currentIndex += currentNeighbor.getBitsCount();
        }

        return bytes;
    }

    @Override
    public int getBitsCount() {
        // adds "num of entries" size
        return super.getBitsCount() + 8;
    }

    @Override
    public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
        if (subobject instanceof PeerInfo) {
            neighbors.add((PeerInfo) subobject);
        } else {
            throw new UnsupportedGeneralObjectException("Neighbor table can't contain "
                    + subobject.getClass().getName() + " as subobject.");
        }
    }

    /**
     * Returns number of entries in this table. Protocol-dependent subclasses have to implement this method so that an
     * abstract {@link #NeighborTable()} class will know the number of entries and will add this to byte representation
     * of this object.
     *
     * @return Number of entries in neighbor table.
     */
    protected abstract int getNumOfEntries();

    /**
     * Returns next hop.
     *
     * @param nodeID
     */
    public abstract PeerInfo getNextHop(byte[] nodeID);

    /**
     * Method giving time slot for this neighbor table to do its things.
     */
    public abstract void onTimeSlot();

    /**
     * Resets neighbor table.
     */
	public abstract void leaveReset();

}
