package pl.edu.pjwstk.p2pp.objects;

import pl.edu.pjwstk.p2pp.util.ByteUtils;

public class PLookup extends GeneralObject {

	/** Number of peers to look for. */
	private boolean[] num;

	/**
	 * If set (E=1), then search for a peer whose peer-ID is the same as peer-IDa. Otherwise, return up to Num peers
	 * whose ID is 'closest' to Peer-IDa
	 */
	private boolean eFlag;
	/**
	 * If set (R=1), then it is a range lookup. It is only set, if E is not set.
	 */
	private boolean rFlag;

	private PeerID peerIdA;
	private PeerID peerIdB;

	private static final int P_LOOKUP_SPECIFIC_STATIC_DATA_LENGTH = 8;

	/**
	 * Constructor for PLookup object.
	 * 
	 * @param peerIdA
	 * @param peerIdB
	 * @param numberOfPeers
	 *            Number of peers to look for.
	 */
	public PLookup(PeerID peerIdA, PeerID peerIdB, boolean[] numberOfPeers) {
		super(GeneralObject.P_LOOKUP_OBJECT_TYPE);

		this.peerIdA = peerIdA;
		this.peerIdB = peerIdB;
		this.num = numberOfPeers;
	}

	@Override
	public byte[] asBytes() {
		return asBytes(getBitsCount());
	}

	@Override
	protected byte[] asBytes(int bitsCount) {
		byte[] bytes = super.asBytes(bitsCount);

		int currentIndex = super.getBitsCount();

		ByteUtils.addBooleanArrayToArrayAtIndex(num, bytes, currentIndex);
		currentIndex += 6;

		ByteUtils.addBooleanArrayToArrayAtIndex(new boolean[] { eFlag, rFlag }, bytes, currentIndex);
		currentIndex += 2;

		ByteUtils.addByteArrayToArrayAtBitIndex(peerIdA.asBytes(), bytes, currentIndex);
		currentIndex += peerIdA.getBitsCount();
		ByteUtils.addByteArrayToArrayAtBitIndex(peerIdB.asBytes(), bytes, currentIndex);

		return bytes;
	}

	@Override
	public int getBitsCount() {
		return super.getBitsCount() + P_LOOKUP_SPECIFIC_STATIC_DATA_LENGTH + peerIdA.getBitsCount()
				+ peerIdB.getBitsCount();
	}

	@Override
	public void addSubobject(GeneralObject subobject) throws UnsupportedGeneralObjectException {
		// TODO Auto-generated method stub

	}

}
